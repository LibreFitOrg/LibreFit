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
import java.time.LocalDateTime

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
class MeasurementRepository(
    private val measurementDao: MeasurementDao,
) {
    val measurements = measurementDao.getAllMeasurements()

    suspend fun upsertMeasurement(measurement: Measurement) {
        measurementDao.upsertMeasurement(measurement)
    }

    suspend fun deleteMeasurement(measurement: Measurement) {
        measurementDao.deleteMeasurement(measurement)
    }

    suspend fun deleteById(id: Long) {
        measurementDao.deleteById(id)
    }

    suspend fun getLastMeasurementByCutoff(cutoff: LocalDateTime): Measurement? {
        return measurementDao.getLastMeasurementByCutoff(cutoff)
    }
}