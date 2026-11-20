/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.librefit.db.dao.MeasurementDao
import org.librefit.db.entity.Measurement
import org.librefit.enums.healthConnect.HealthConnectSyncOption
import org.librefit.health.HealthConnectRepository
import org.librefit.models.Weight

class MeasurementRepositoryTest {
    private lateinit var measurementDao: MeasurementDao
    private lateinit var userPreferences: UserPreferencesRepository
    private lateinit var healthConnectRepository: HealthConnectRepository
    private lateinit var repository: MeasurementRepository

    @BeforeTest
    fun setUp() {
        measurementDao = mockk()
        userPreferences = mockk()
        healthConnectRepository = mockk()

        every { measurementDao.getAllMeasurements() } returns MutableStateFlow(emptyList())
        every { userPreferences.healthConnectEnabled } returns MutableStateFlow(true)
        coEvery { userPreferences.isHealthConnectEnabled() } returns true
        coEvery { userPreferences.isHealthConnectSyncEnabled(any()) } answers {
            firstArg<String>() == HealthConnectSyncOption.WEIGHT_WRITE.preferenceId
        }
        coEvery { healthConnectRepository.exportMeasurements(any(), any()) } returns 1

        repository = MeasurementRepository(
            measurementDao,
            userPreferences,
            healthConnectRepository
        )
    }

    @Test
    fun `new measurement uses generated id for health connect`() = runTest {
        val savedMeasurement = slot<Measurement>()
        coEvery { measurementDao.upsertMeasurement(capture(savedMeasurement)) } returns 42L

        val savedId = repository.upsertMeasurement(
            Measurement(bodyWeight = Weight.kilograms(80.0))
        )

        assertEquals(42L, savedId)
        assertTrue(savedMeasurement.captured.healthConnectRecordVersion > 0L)
        coVerify(exactly = 1) {
            healthConnectRepository.exportMeasurements(
                measurements = match {
                    it.single().id == 42L &&
                        it.single().healthConnectRecordVersion ==
                        savedMeasurement.captured.healthConnectRecordVersion
                },
                options = setOf(HealthConnectSyncOption.WEIGHT_WRITE)
            )
        }
    }

    @Test
    fun `edited measurement always gets a newer health connect version`() = runTest {
        val previousVersion = System.currentTimeMillis() + 10_000
        val existing = Measurement(
            id = 5,
            bodyWeight = Weight.kilograms(80.0),
            healthConnectRecordVersion = previousVersion
        )
        val savedMeasurement = slot<Measurement>()
        coEvery { measurementDao.getMeasurement(5) } returns existing
        coEvery { measurementDao.upsertMeasurement(capture(savedMeasurement)) } returns -1L

        repository.upsertMeasurement(existing.copy(bodyWeight = Weight.kilograms(81.0)))

        assertEquals(previousVersion + 1, savedMeasurement.captured.healthConnectRecordVersion)
    }
}
