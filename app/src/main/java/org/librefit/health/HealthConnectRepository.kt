/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Percentage
import dagger.hilt.android.qualifiers.ApplicationContext
import org.librefit.db.entity.Measurement
import org.librefit.db.entity.Workout
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.enums.healthConnect.HealthConnectSyncOption
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.reflect.KClass

@Singleton
class HealthConnectRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val exerciseDataLayer: ExerciseDataLayer
) {
    companion object {
        const val SYNC_DAYS = 30L
    }

    val allPermissions: Set<String> = HealthConnectSyncOption.entries
        .flatMapTo(mutableSetOf()) { permissionsFor(it) }

    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE
    }

    fun permissionsFor(option: HealthConnectSyncOption): Set<String> = when (option) {
        HealthConnectSyncOption.WEIGHT_READ -> setOf(
            HealthPermission.getReadPermission(WeightRecord::class)
        )
        HealthConnectSyncOption.WEIGHT_WRITE -> setOf(
            HealthPermission.getWritePermission(WeightRecord::class)
        )
        HealthConnectSyncOption.FAT_READ -> setOf(
            HealthPermission.getReadPermission(BodyFatRecord::class)
        )
        HealthConnectSyncOption.FAT_WRITE -> setOf(
            HealthPermission.getWritePermission(BodyFatRecord::class)
        )
        HealthConnectSyncOption.WORKOUT_WRITE -> setOf(
            HealthPermission.getWritePermission(ExerciseSessionRecord::class)
        )
    }

    suspend fun grantedPermissions(): Set<String> {
        if (!isAvailable()) return emptySet()
        return healthConnectClient.permissionController.getGrantedPermissions()
    }

    suspend fun hasPermissions(options: Set<HealthConnectSyncOption>): Boolean =
        grantedPermissions().containsAll(options.flatMapTo(mutableSetOf(), ::permissionsFor))

    suspend fun exportMeasurements(
        measurements: List<Measurement>,
        options: Set<HealthConnectSyncOption> = setOf(
            HealthConnectSyncOption.WEIGHT_WRITE,
            HealthConnectSyncOption.FAT_WRITE
        )
    ): Int {
        return insertRecords(
            measurements.filter(::isWithinSyncWindow).flatMap {
                it.toHealthConnectRecords(options)
            },
            options
        )
    }

    suspend fun exportWorkout(workoutWithExercisesAndSets: WorkoutWithExercisesAndSets): Int {
        if (!isWithinSyncWindow(workoutWithExercisesAndSets)) return 0
        return insertRecords(
            listOfNotNull(exerciseDataLayer.toExerciseSessionRecord(workoutWithExercisesAndSets)),
            setOf(HealthConnectSyncOption.WORKOUT_WRITE)
        )
    }

    suspend fun exportWorkouts(workouts: List<WorkoutWithExercisesAndSets>): Int {
        return insertRecords(
            workouts.filter(::isWithinSyncWindow)
                .mapNotNull(exerciseDataLayer::toExerciseSessionRecord),
            setOf(HealthConnectSyncOption.WORKOUT_WRITE)
        )
    }

    suspend fun syncEnabledWrites(
        measurements: List<Measurement>,
        workouts: List<WorkoutWithExercisesAndSets>,
        options: Set<HealthConnectSyncOption>
    ): Int {
        val records = buildList {
            addAll(
                measurements.filter(::isWithinSyncWindow).flatMap {
                    it.toHealthConnectRecords(options)
                }
            )
            if (HealthConnectSyncOption.WORKOUT_WRITE in options) {
                addAll(
                    workouts.filter(::isWithinSyncWindow)
                        .mapNotNull(exerciseDataLayer::toExerciseSessionRecord)
                )
            }
        }

        return insertRecords(records, options)
    }

    suspend fun readMeasurements(options: Set<HealthConnectSyncOption>): List<Measurement> {
        if (!hasPermissions(options)) {
            throw SecurityException("Missing Health Connect read permissions")
        }

        val timeRange = TimeRangeFilter.after(Instant.now().minus(SYNC_DAYS, ChronoUnit.DAYS))
        val weights = if (HealthConnectSyncOption.WEIGHT_READ in options) {
            readExternalRecords(WeightRecord::class, timeRange)
        } else emptyList()
        val bodyFat = if (HealthConnectSyncOption.FAT_READ in options) {
            readExternalRecords(BodyFatRecord::class, timeRange)
        } else emptyList()
        return buildList {
            weights.forEach { weight ->
                add(
                    Measurement(
                        bodyWeight = weight.weight.inKilograms.roundToTwoDecimals(),
                        date = LocalDateTime.ofInstant(weight.time, ZoneId.systemDefault()),
                        // The source ID lets us update or remove exactly this imported value.
                        healthConnectWeightRecordId = weight.metadata.id
                    )
                )
            }
            bodyFat.forEach { fat ->
                add(
                    Measurement(
                        bodyFatPercentage = fat.percentage.value.roundToInt().coerceIn(0, 100),
                        date = LocalDateTime.ofInstant(fat.time, ZoneId.systemDefault()),
                        healthConnectBodyFatRecordId = fat.metadata.id
                    )
                )
            }
        }
    }

    suspend fun deleteMeasurement(
        measurement: Measurement,
        options: Set<HealthConnectSyncOption>
    ) {
        if (
            HealthConnectSyncOption.WEIGHT_WRITE in options &&
            measurement.healthConnectWeightRecordId == null
        ) {
            deleteRecords(
                WeightRecord::class,
                HealthConnectSyncOption.WEIGHT_WRITE,
                "librefit-measurement-${measurement.id}-body-weight"
            )
        }
        if (
            HealthConnectSyncOption.FAT_WRITE in options &&
            measurement.healthConnectBodyFatRecordId == null
        ) {
            deleteRecords(
                BodyFatRecord::class,
                HealthConnectSyncOption.FAT_WRITE,
                "librefit-measurement-${measurement.id}-body-fat"
            )
        }
    }

    suspend fun deleteWorkout(workout: Workout) {
        deleteRecords(
            ExerciseSessionRecord::class,
            HealthConnectSyncOption.WORKOUT_WRITE,
            "librefit-workout-${workout.id}"
        )
    }

    private fun Double.roundToTwoDecimals(): Double = round(this * 100) / 100

    private suspend fun <T : Record> readExternalRecords(
        recordType: KClass<T>,
        timeRange: TimeRangeFilter
    ): List<T> = buildList {
        var pageToken: String? = null
        do {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = recordType,
                    timeRangeFilter = timeRange,
                    pageToken = pageToken
                )
            )
            addAll(response.records.filterNot(::isWrittenByLibreFit))
            pageToken = response.pageToken
        } while (pageToken != null)
    }

    private fun isWrittenByLibreFit(record: Record): Boolean {
        return record.metadata.dataOrigin.packageName == context.packageName
    }

    private suspend fun insertRecords(
        records: List<Record>,
        options: Set<HealthConnectSyncOption>
    ): Int {
        // Missing source data is a valid no-op and needs no permission check.
        if (records.isEmpty()) return 0

        if (!hasPermissions(options)) {
            throw SecurityException("Missing Health Connect write permissions")
        }

        healthConnectClient.insertRecords(records)
        return records.size
    }

    private fun Measurement.toHealthConnectRecords(options: Set<HealthConnectSyncOption>): List<Record> {
        val zone = ZoneId.systemDefault()
        val zonedDate = date.atZone(zone)
        val instant = zonedDate.toInstant()
        val zoneOffset = zonedDate.offset
        val recordVersion = healthConnectRecordVersion

        return buildList {
            if (
                bodyWeight > 0.0 && healthConnectWeightRecordId == null &&
                HealthConnectSyncOption.WEIGHT_WRITE in options
            ) {
                add(
                    WeightRecord(
                        metadata = Metadata.manualEntry(
                            clientRecordId = "librefit-measurement-$id-body-weight",
                            clientRecordVersion = recordVersion
                        ),
                        time = instant,
                        zoneOffset = zoneOffset,
                        weight = Mass.kilograms(bodyWeight)
                    )
                )
            }

            if (
                bodyFatPercentage > 0 && healthConnectBodyFatRecordId == null &&
                HealthConnectSyncOption.FAT_WRITE in options
            ) {
                add(
                    BodyFatRecord(
                        metadata = Metadata.manualEntry(
                            clientRecordId = "librefit-measurement-$id-body-fat",
                            clientRecordVersion = recordVersion
                        ),
                        time = instant,
                        zoneOffset = zoneOffset,
                        percentage = Percentage(bodyFatPercentage.toDouble())
                    )
                )
            }

        }
    }

    private suspend fun <T : Record> deleteRecords(
        recordType: KClass<T>,
        option: HealthConnectSyncOption,
        clientRecordId: String
    ) {
        if (!hasPermissions(setOf(option))) return
        healthConnectClient.deleteRecords(
            recordType = recordType,
            recordIdsList = emptyList(),
            clientRecordIdsList = listOf(clientRecordId)
        )
    }

    private fun isWithinSyncWindow(measurement: Measurement): Boolean =
        measurement.date >= LocalDateTime.now().minusDays(SYNC_DAYS)

    private fun isWithinSyncWindow(workout: WorkoutWithExercisesAndSets): Boolean =
        workout.workout.completed >= LocalDateTime.now().minusDays(SYNC_DAYS)
}
