/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.repository

import org.librefit.db.dao.MeasurementDao
import org.librefit.db.entity.Measurement
import org.librefit.enums.healthConnect.HealthConnectAccessMode
import org.librefit.enums.healthConnect.HealthConnectSyncOption
import org.librefit.health.HealthConnectRepository
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for managing measurements data.
 *
 * This class serves as a mediator between [MeasurementDao] and the
 * application, providing a clean API for data access.
 *
 *
 * @param measurementDao The [MeasurementDao] instance used to access measurements data from the database.
 *
 */
@Singleton
class MeasurementRepository @Inject constructor(
    private val measurementDao: MeasurementDao,
    private val userPreferences: UserPreferencesRepository,
    private val healthConnectRepository: HealthConnectRepository
) {
    val measurements = measurementDao.getAllMeasurements()

    suspend fun upsertMeasurement(
        measurement: Measurement,
        syncToHealthConnect: Boolean = true
    ): Long {
        val previous = measurement.id.takeIf { it != 0L }
            ?.let { measurementDao.getMeasurement(it) }
        val measurementToSave = if (syncToHealthConnect) {
            val previousVersion = previous?.healthConnectRecordVersion
                ?: measurement.healthConnectRecordVersion
            measurement.copy(
                // Health Connect only accepts updates with a strictly newer version.
                healthConnectRecordVersion = maxOf(
                    Instant.now().toEpochMilli(),
                    previousVersion + 1
                ),
                healthConnectWeightRecordId = previous?.healthConnectWeightRecordId
                    ?: measurement.healthConnectWeightRecordId,
                healthConnectBodyFatRecordId = previous?.healthConnectBodyFatRecordId
                    ?: measurement.healthConnectBodyFatRecordId
            )
        } else {
            measurement
        }
        val insertedId = measurementDao.upsertMeasurement(measurementToSave)
        val savedMeasurement = measurementToSave.copy(
            // A generated database ID keeps every Health Connect client ID unique.
            id = measurementToSave.id.takeIf { it != 0L } ?: insertedId
        )

        if (
            !syncToHealthConnect || !userPreferences.isHealthConnectEnabled()
        ) return savedMeasurement.id

        val writeOptions = buildSet {
            for (option in HealthConnectSyncOption.entries) {
                if (
                    option.accessMode == HealthConnectAccessMode.WRITE &&
                    option != HealthConnectSyncOption.WORKOUT_WRITE &&
                    userPreferences.isHealthConnectSyncEnabled(option.preferenceId)
                ) add(option)
            }
        }
        if (writeOptions.isEmpty()) return savedMeasurement.id

        runCatching {
            healthConnectRepository.exportMeasurements(listOf(savedMeasurement), writeOptions)
        }.onFailure {
            val grantedPermissions = healthConnectRepository.grantedPermissions()
            writeOptions.forEach { option ->
                if (!grantedPermissions.containsAll(healthConnectRepository.permissionsFor(option))) {
                    userPreferences.saveHealthConnectSyncEnabled(option.preferenceId, false)
                }
            }
        }
        return savedMeasurement.id
    }

    suspend fun deleteMeasurement(
        measurement: Measurement,
        syncToHealthConnect: Boolean = true
    ) {
        if (syncToHealthConnect) rememberDeletedImportedRecords(measurement)
        measurementDao.deleteMeasurement(measurement)
        if (syncToHealthConnect) deleteFromHealthConnect(measurement)
    }

    suspend fun deleteById(id: Long, syncToHealthConnect: Boolean = true) {
        val measurement = measurementDao.getMeasurement(id)
        if (syncToHealthConnect) measurement?.let { rememberDeletedImportedRecords(it) }
        measurementDao.deleteById(id)
        if (syncToHealthConnect) measurement?.let { deleteFromHealthConnect(it) }
    }

    suspend fun getLastMeasurementByCutoff(cutoff: LocalDateTime): Measurement? {
        return measurementDao.getLastMeasurementByCutoff(cutoff)
    }

    private suspend fun deleteFromHealthConnect(measurement: Measurement) {
        if (!userPreferences.isHealthConnectEnabled()) return

        val writeOptions = buildSet {
            for (option in HealthConnectSyncOption.entries) {
                if (
                    option.accessMode == HealthConnectAccessMode.WRITE &&
                    option != HealthConnectSyncOption.WORKOUT_WRITE &&
                    userPreferences.isHealthConnectSyncEnabled(option.preferenceId)
                ) add(option)
            }
        }
        if (writeOptions.isEmpty()) return

        runCatching {
            healthConnectRepository.deleteMeasurement(measurement, writeOptions)
        }
    }

    private suspend fun rememberDeletedImportedRecords(measurement: Measurement) {
        userPreferences.ignoreHealthConnectRecordIds(
            setOfNotNull(
                measurement.healthConnectWeightRecordId,
                measurement.healthConnectBodyFatRecordId
            )
        )
    }
}
