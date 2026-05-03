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
import org.librefit.db.entity.ExerciseDC

@Dao
interface DatasetDao {
    @Upsert
    suspend fun setDataset(exercises: List<ExerciseDC>)

    @Query("SELECT * FROM dataset ORDER BY name")
    fun getDataset(): Flow<List<ExerciseDC>>

    @Query("SELECT * FROM dataset ORDER BY name")
    suspend fun getAllExerciseDCs(): List<ExerciseDC>

    @Query("SELECT * FROM dataset WHERE isCustomExercise")
    fun getCustomExercises(): Flow<List<ExerciseDC>>

    @Upsert
    suspend fun upsertExercise(exerciseDC: ExerciseDC)

    @Delete
    suspend fun deleteExercise(exerciseDC: ExerciseDC)

    @Query("SELECT * FROM dataset WHERE id = :id")
    suspend fun getExerciseFromId(id: String): ExerciseDC?

    @Query("SELECT * FROM dataset WHERE id = :id")
    fun getExerciseFlowFromId(id: String): Flow<ExerciseDC?>
}