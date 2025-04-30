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

package org.librefit.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.librefit.db.entity.Measurement

@Dao
interface MeasurementDao {
    @Upsert
    suspend fun upsertMeasurement(measurement: Measurement)

    @Delete
    suspend fun deleteMeasurement(measurement: Measurement)

    @Query("DELETE FROM measurements WHERE id = :id ")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM measurements ORDER BY date DESC")
    fun getAllMeasurements(): Flow<List<Measurement>>
}