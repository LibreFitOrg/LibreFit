/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.health

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.librefit.db.entity.Measurement
import org.librefit.db.repository.MeasurementRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.healthConnect.HealthConnectSyncOption
import org.librefit.models.Weight
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HealthConnectSyncManagerTest {
    private lateinit var userPreferences: UserPreferencesRepository
    private lateinit var measurementRepository: MeasurementRepository
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var healthConnectRepository: HealthConnectRepository
    private lateinit var manager: HealthConnectSyncManager

    private lateinit var healthConnectEnabled: MutableStateFlow<Boolean>
    private lateinit var measurements: MutableStateFlow<List<Measurement>>
    private lateinit var options: Map<HealthConnectSyncOption, MutableStateFlow<Boolean>>

    @Before
    fun setUp() {
        userPreferences = mockk()
        measurementRepository = mockk()
        workoutRepository = mockk()
        healthConnectRepository = mockk()
        healthConnectEnabled = MutableStateFlow(true)
        measurements = MutableStateFlow(emptyList())
        options = HealthConnectSyncOption.entries.associateWith { MutableStateFlow(false) }

        every { userPreferences.healthConnectEnabled } returns healthConnectEnabled
        coEvery { userPreferences.isHealthConnectEnabled() } answers {
            healthConnectEnabled.value
        }
        coEvery { userPreferences.isHealthConnectSyncEnabled(any()) } answers {
            options.getValue(
                HealthConnectSyncOption.entries.first { it.preferenceId == firstArg() }
            ).value
        }
        coEvery { userPreferences.getIgnoredHealthConnectRecordIds() } returns emptySet()
        coEvery { userPreferences.saveHealthConnectSyncEnabled(any(), any()) } answers {
            val option = HealthConnectSyncOption.entries.first {
                it.preferenceId == firstArg<String>()
            }
            options.getValue(option).value = secondArg()
        }
        every { measurementRepository.measurements } returns measurements
        every { workoutRepository.completedWorkoutsWithExercisesAndSets } returns
            MutableStateFlow(emptyList())
        every { healthConnectRepository.isAvailable() } returns true
        every { healthConnectRepository.permissionsFor(any()) } answers {
            setOf(firstArg<HealthConnectSyncOption>().preferenceId)
        }
        coEvery { healthConnectRepository.grantedPermissions() } returns
            HealthConnectSyncOption.entries.mapTo(mutableSetOf()) { it.preferenceId }
        coEvery { healthConnectRepository.readMeasurements(any()) } returns emptyList()
        coEvery { healthConnectRepository.syncEnabledWrites(any(), any(), any()) } returns 0
        coEvery { measurementRepository.upsertMeasurement(any(), any()) } answers {
            firstArg<Measurement>().id.takeIf { it != 0L } ?: 100L
        }
        coEvery { measurementRepository.deleteById(any(), any()) } returns Unit

        manager = HealthConnectSyncManager(
            userPreferences,
            measurementRepository,
            workoutRepository,
            healthConnectRepository
        )
    }

    @Test
    fun `body fat is merged with nearby local weight without changing its origin`() = runTest {
        val measuredAt = LocalDateTime.now()
        val localWeight = Measurement(
            id = 1,
            bodyWeight = Weight.kilograms(80.0),
            date = measuredAt
        )
        val importedBodyFat = Measurement(
            bodyFatPercentage = 22,
            date = measuredAt.plusSeconds(30),
            healthConnectBodyFatRecordId = "body-fat-id"
        )
        measurements.value = listOf(localWeight)
        options.getValue(HealthConnectSyncOption.FAT_READ).value = true
        coEvery {
            healthConnectRepository.readMeasurements(setOf(HealthConnectSyncOption.FAT_READ))
        } returns listOf(importedBodyFat)

        manager.syncEnabledData()

        coVerify(exactly = 1) {
            measurementRepository.upsertMeasurement(
                match {
                    it.id == 1L && it.bodyWeight == Weight.kilograms(80.0) &&
                        it.bodyFatPercentage == 22 &&
                        it.healthConnectWeightRecordId == null &&
                        it.healthConnectBodyFatRecordId == "body-fat-id"
                },
                syncToHealthConnect = false
            )
        }
    }

    @Test
    fun `imported weight is not merged into a local muscle mass measurement`() = runTest {
        val measuredAt = LocalDateTime.now()
        measurements.value = listOf(
            Measurement(id = 1, muscleMassPercentage = 40, date = measuredAt)
        )
        options.getValue(HealthConnectSyncOption.WEIGHT_READ).value = true
        coEvery {
            healthConnectRepository.readMeasurements(setOf(HealthConnectSyncOption.WEIGHT_READ))
        } returns listOf(
            Measurement(
                bodyWeight = Weight.kilograms(80.0),
                date = measuredAt,
                healthConnectWeightRecordId = "weight-id"
            )
        )

        manager.syncEnabledData()

        coVerify(exactly = 1) {
            measurementRepository.upsertMeasurement(
                match {
                    it.id == 0L && it.bodyWeight == Weight.kilograms(80.0) &&
                        it.muscleMassPercentage == 0 &&
                        it.healthConnectWeightRecordId == "weight-id"
                },
                syncToHealthConnect = false
            )
        }
    }

    @Test
    fun `missing weight permission does not block body fat import`() = runTest {
        options.getValue(HealthConnectSyncOption.WEIGHT_READ).value = true
        options.getValue(HealthConnectSyncOption.FAT_READ).value = true
        coEvery { healthConnectRepository.grantedPermissions() } returns
            setOf(HealthConnectSyncOption.FAT_READ.preferenceId)

        manager.syncEnabledData()

        assertThat(options.getValue(HealthConnectSyncOption.WEIGHT_READ).value).isFalse()
        coVerify(exactly = 0) {
            healthConnectRepository.readMeasurements(setOf(HealthConnectSyncOption.WEIGHT_READ))
        }
        coVerify(exactly = 1) {
            healthConnectRepository.readMeasurements(setOf(HealthConnectSyncOption.FAT_READ))
        }
    }

    @Test
    fun `deleted source record removes its empty imported measurement`() = runTest {
        measurements.value = listOf(
            Measurement(
                id = 7,
                bodyFatPercentage = 20,
                date = LocalDateTime.now(),
                healthConnectBodyFatRecordId = "deleted-id"
            )
        )
        options.getValue(HealthConnectSyncOption.FAT_READ).value = true

        manager.syncEnabledData()

        coVerify(exactly = 1) {
            measurementRepository.deleteById(7, syncToHealthConnect = false)
        }
    }

    @Test
    fun `bulk write contains only the last thirty days`() = runTest {
        val recent = Measurement(
            id = 1,
            bodyWeight = Weight.kilograms(80.0),
            date = LocalDateTime.now()
        )
        val old = Measurement(
            id = 2,
            bodyWeight = Weight.kilograms(81.0),
            date = LocalDateTime.now().minusDays(31)
        )
        measurements.value = listOf(recent, old)
        options.getValue(HealthConnectSyncOption.WEIGHT_WRITE).value = true

        manager.syncEnabledData()

        coVerify(exactly = 1) {
            healthConnectRepository.syncEnabledWrites(
                measurements = listOf(recent),
                workouts = emptyList(),
                options = setOf(HealthConnectSyncOption.WEIGHT_WRITE)
            )
        }
    }

    @Test
    fun `switch change stops stale sync before writing`() = runTest {
        val readStarted = CompletableDeferred<Unit>()
        val continueRead = CompletableDeferred<Unit>()
        options.getValue(HealthConnectSyncOption.WEIGHT_READ).value = true
        options.getValue(HealthConnectSyncOption.WEIGHT_WRITE).value = true
        coEvery {
            healthConnectRepository.readMeasurements(setOf(HealthConnectSyncOption.WEIGHT_READ))
        } coAnswers {
            readStarted.complete(Unit)
            continueRead.await()
            emptyList()
        }

        val staleSync = async { manager.syncEnabledData() }
        readStarted.await()
        manager.invalidatePendingSyncs()
        val currentSync = async { manager.syncEnabledData() }
        continueRead.complete(Unit)
        staleSync.await()
        currentSync.await()

        coVerify(exactly = 1) {
            healthConnectRepository.syncEnabledWrites(
                measurements = any(),
                workouts = any(),
                options = setOf(HealthConnectSyncOption.WEIGHT_WRITE)
            )
        }
    }
}
