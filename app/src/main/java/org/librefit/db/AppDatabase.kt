/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db

import androidx.room.migration.Migration
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import org.librefit.db.converters.ExerciseDCConverter
import org.librefit.db.converters.LocalDateTimeConverter
import org.librefit.db.dao.DatasetDao
import org.librefit.db.dao.MeasurementDao
import org.librefit.db.dao.WorkoutDao
import org.librefit.db.entity.Exercise
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.entity.Measurement
import org.librefit.db.entity.Set
import org.librefit.db.entity.Workout

@Database(
    entities = [Workout::class, Exercise::class, Set::class, Measurement::class, ExerciseDC::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(LocalDateTimeConverter::class, ExerciseDCConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val NAME = "librefit_database"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    ALTER TABLE exercises
                    ADD COLUMN position INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    UPDATE exercises AS current
                    SET position = (
                        SELECT COUNT(*) - 1
                        FROM exercises AS previous
                        WHERE previous.workoutId = current.workoutId
                          AND previous.id <= current.id
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_exercises_workoutId_position
                    ON exercises(workoutId, position)
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_exercises_idExerciseDC
                    ON exercises(idExerciseDC)
                    """.trimIndent()
                )
            }
        }
    }

    abstract fun getWorkoutDao(): WorkoutDao

    abstract fun getMeasurementDao(): MeasurementDao

    abstract fun getDatasetDao(): DatasetDao
}
