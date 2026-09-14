/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.health

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.librefit.db.entity.Measurement
import org.librefit.db.repository.MeasurementRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.healthConnect.HealthConnectAccessMode
import org.librefit.enums.healthConnect.HealthConnectSyncOption
import org.librefit.models.Weight
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class HealthConnectSyncManager @Inject constructor(
    private val userPreferences: UserPreferencesRepository,
    private val measurementRepository: MeasurementRepository,
    private val workoutRepository: WorkoutRepository,
    private val healthConnectRepository: HealthConnectRepository
) {
    private val syncMutex = Mutex()
    private val syncRevision = AtomicLong()

    fun invalidatePendingSyncs() {
        // A changed switch makes results from an older sync request stale.
        syncRevision.incrementAndGet()
    }

    suspend fun syncEnabledData(
        requestedOptions: Set<HealthConnectSyncOption> = emptySet()
    ): Int {
        val requestedRevision = syncRevision.get()
        return syncMutex.withLock {
            if (
                !canContinue(requestedRevision) ||
                !userPreferences.isHealthConnectEnabled()
            ) return@withLock 0

            val configuredOptions = configuredOptions() + requestedOptions
            val grantedPermissions = healthConnectRepository.grantedPermissions()
            val permittedOptions = configuredOptions.filterTo(mutableSetOf()) { option ->
                grantedPermissions.containsAll(healthConnectRepository.permissionsFor(option))
            }
            (configuredOptions - permittedOptions).forEach { option ->
                // One revoked permission must not stop the other data types.
                userPreferences.saveHealthConnectSyncEnabled(option.preferenceId, false)
            }

            val readSnapshots = mutableMapOf<HealthConnectSyncOption, List<Measurement>>()
            val ignoredIds = userPreferences.getIgnoredHealthConnectRecordIds()
            permittedOptions.filter { it.accessMode == HealthConnectAccessMode.READ }
                .forEach { option ->
                    runCatching {
                        healthConnectRepository.readMeasurements(setOf(option))
                    }.onSuccess { measurements ->
                        readSnapshots[option] = measurements.filterNot { measurement ->
                            measurement.healthConnectWeightRecordId?.let { it in ignoredIds } ==
                                true ||
                                measurement.healthConnectBodyFatRecordId?.let {
                                    it in ignoredIds
                                } == true
                        }
                    }
                }
            if (!canContinue(requestedRevision)) return@withLock 0

            val existingMeasurements = measurementRepository.measurements.first().toMutableList()
            var changedRecords = reconcileDeletedRecords(
                existingMeasurements,
                readSnapshots,
                requestedRevision
            )
            if (!canContinue(requestedRevision)) return@withLock changedRecords

            readSnapshots.values.flatten().sortedBy { it.date }.forEach { imported ->
                if (!canContinue(requestedRevision)) return@withLock changedRecords
                changedRecords += mergeImportedMeasurement(existingMeasurements, imported)
            }
            if (!canContinue(requestedRevision)) return@withLock changedRecords

            val recentMeasurements = existingMeasurements.filter {
                it.date >= LocalDateTime.now().minusDays(HealthConnectRepository.SYNC_DAYS)
            }
            val recentWorkouts = workoutRepository.completedWorkoutsWithExercisesAndSets.first()
                .filter {
                    it.workout.completed >=
                        LocalDateTime.now().minusDays(HealthConnectRepository.SYNC_DAYS)
                }
            permittedOptions.filter { it.accessMode == HealthConnectAccessMode.WRITE }
                .forEach { option ->
                    if (!canContinue(requestedRevision)) return@withLock changedRecords
                    changedRecords += runCatching {
                        healthConnectRepository.syncEnabledWrites(
                            measurements = recentMeasurements,
                            workouts = recentWorkouts,
                            options = setOf(option)
                        )
                    }.getOrDefault(0)
                }
            changedRecords
        }
    }

    private suspend fun reconcileDeletedRecords(
        measurements: MutableList<Measurement>,
        snapshots: Map<HealthConnectSyncOption, List<Measurement>>,
        requestedRevision: Long
    ): Int {
        val weightRecordIds = snapshots[HealthConnectSyncOption.WEIGHT_READ]
            ?.mapNotNullTo(mutableSetOf()) { it.healthConnectWeightRecordId }
        val bodyFatRecordIds = snapshots[HealthConnectSyncOption.FAT_READ]
            ?.mapNotNullTo(mutableSetOf()) { it.healthConnectBodyFatRecordId }
        val cutoff = LocalDateTime.now().minusDays(HealthConnectRepository.SYNC_DAYS)
        var changedRecords = 0

        for (index in measurements.lastIndex downTo 0) {
            if (!canContinue(requestedRevision)) break
            val existing = measurements[index]
            if (existing.date < cutoff) continue

            val updated = existing.copy(
                bodyWeight = if (
                    weightRecordIds != null && existing.healthConnectWeightRecordId != null &&
                    existing.healthConnectWeightRecordId !in weightRecordIds
                ) Weight.zero() else existing.bodyWeight,
                bodyFatPercentage = if (
                    bodyFatRecordIds != null && existing.healthConnectBodyFatRecordId != null &&
                    existing.healthConnectBodyFatRecordId !in bodyFatRecordIds
                ) 0 else existing.bodyFatPercentage,
                healthConnectWeightRecordId = existing.healthConnectWeightRecordId?.takeIf {
                    weightRecordIds == null || it in weightRecordIds
                },
                healthConnectBodyFatRecordId = existing.healthConnectBodyFatRecordId?.takeIf {
                    bodyFatRecordIds == null || it in bodyFatRecordIds
                }
            )
            if (updated == existing) continue

            // Keep local values that Health Connect does not synchronize.
            if (
                updated.bodyWeight == Weight.zero() && updated.bodyFatPercentage == 0 &&
                updated.muscleMassPercentage == 0 && updated.notes.isBlank()
            ) {
                measurementRepository.deleteById(updated.id, syncToHealthConnect = false)
                measurements.removeAt(index)
            } else {
                measurementRepository.upsertMeasurement(updated, syncToHealthConnect = false)
                measurements[index] = updated
            }
            changedRecords++
        }
        return changedRecords
    }

    private suspend fun mergeImportedMeasurement(
        measurements: MutableList<Measurement>,
        imported: Measurement
    ): Int {
        val targetIndex = findTargetIndex(measurements, imported)
        if (targetIndex == -1) {
            val insertedId = measurementRepository.upsertMeasurement(
                imported,
                syncToHealthConnect = false
            )
            measurements += imported.copy(id = insertedId)
            return 1
        }

        val existing = measurements[targetIndex]
        val updated = existing.copy(
            bodyWeight = imported.bodyWeight.takeIf { it > Weight.zero() } ?: existing.bodyWeight,
            bodyFatPercentage = imported.bodyFatPercentage.takeIf { it > 0 }
                ?: existing.bodyFatPercentage,
            healthConnectWeightRecordId = imported.healthConnectWeightRecordId
                ?: existing.healthConnectWeightRecordId,
            healthConnectBodyFatRecordId = imported.healthConnectBodyFatRecordId
                ?: existing.healthConnectBodyFatRecordId
        )
        if (updated == existing) return 0

        measurementRepository.upsertMeasurement(updated, syncToHealthConnect = false)
        measurements[targetIndex] = updated
        return 1
    }

    private fun findTargetIndex(
        measurements: List<Measurement>,
        imported: Measurement
    ): Int {
        val recordIndex = measurements.indexOfFirst { existing ->
            imported.healthConnectWeightRecordId != null &&
                imported.healthConnectWeightRecordId == existing.healthConnectWeightRecordId ||
                imported.healthConnectBodyFatRecordId != null &&
                imported.healthConnectBodyFatRecordId == existing.healthConnectBodyFatRecordId
        }
        if (recordIndex != -1) return recordIndex

        return measurements.indices
            .filter { index ->
                val existing = measurements[index]
                val acceptsWeight = imported.healthConnectWeightRecordId != null &&
                    existing.bodyWeight == Weight.zero() &&
                    existing.bodyFatPercentage > 0
                val acceptsBodyFat = imported.healthConnectBodyFatRecordId != null &&
                    existing.bodyFatPercentage == 0 &&
                    existing.bodyWeight > Weight.zero()
                (acceptsWeight || acceptsBodyFat) &&
                    abs(Duration.between(existing.date, imported.date).seconds) <=
                    BODY_COMPOSITION_MATCH_SECONDS
            }
            .minByOrNull { index ->
                abs(Duration.between(measurements[index].date, imported.date).seconds)
            } ?: -1
    }

    private suspend fun configuredOptions(): Set<HealthConnectSyncOption> = buildSet {
        for (option in HealthConnectSyncOption.entries) {
            if (userPreferences.isHealthConnectSyncEnabled(option.preferenceId)) add(option)
        }
    }

    private fun canContinue(requestedRevision: Long): Boolean =
        requestedRevision == syncRevision.get() &&
            healthConnectRepository.isAvailable()

    private companion object {
        // Body composition values from one scale measurement can differ by a few seconds.
        const val BODY_COMPOSITION_MATCH_SECONDS = 120L
    }
}
