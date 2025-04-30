/*
 * Copyright (c) 2025. LibreFit
 *
 * This file is part of LibreFit
 *
 * LibreFit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreFit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreFit.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.librefit.db.repository

import kotlinx.coroutines.flow.Flow
import org.librefit.db.dao.MeasurementDao
import org.librefit.db.entity.Measurement

/**
 * Repository class for managing measurements data.
 *
 * This class serves as a mediator between [MeasurementDao] and the
 * application, providing a clean API for data access.
 *
 * This class is provided by [org.librefit.di.DatabaseModule].
 *
 * @param measurementDao The [MeasurementDao] instance used to access measurements data from the database.
 *
 */
class MeasurementRepository(private val measurementDao: MeasurementDao) {

    suspend fun upsertMeasurement(measurement: Measurement) {
        measurementDao.upsertMeasurement(measurement)
    }

    suspend fun deleteMeasurement(measurement: Measurement) {
        measurementDao.deleteMeasurement(measurement)
    }

    suspend fun deleteById(id: Long) {
        measurementDao.deleteById(id)
    }

    fun getAllMeasurements(): Flow<List<Measurement>> {
        return measurementDao.getAllMeasurements()
    }
}