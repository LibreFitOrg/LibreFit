/*
 * Copyright (c) 2024-2025. LibreFit
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
 *
 * LibreFit is subject to additional terms covering author attribution and
 * trademark usage, as found in the accompanying ADDITIONAL_TERMS.md file.
 */

package org.librefit.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateTimeConverter::class, ExerciseDCConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val NAME = "librefit_database"
    }

    abstract fun getWorkoutDao(): WorkoutDao

    abstract fun getMeasurementDao(): MeasurementDao

    abstract fun getDatasetDao(): DatasetDao
}