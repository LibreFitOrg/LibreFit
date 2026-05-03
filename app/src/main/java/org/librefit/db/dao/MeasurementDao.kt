/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.librefit.db.entity.Measurement
import java.time.LocalDateTime

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

    @Query("SELECT * FROM measurements ORDER BY date DESC")
    suspend fun getAllMeasurementsForBackup(): List<Measurement>

    @Query("SELECT * FROM measurements WHERE date <= :cutoff ORDER BY date DESC")
    suspend fun getLastMeasurementByCutoff(cutoff: LocalDateTime): Measurement?
}