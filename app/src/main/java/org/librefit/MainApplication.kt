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
 */

package org.librefit

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.librefit.data.ExerciseDeserializer
import org.librefit.db.WorkoutDatabase
import org.librefit.helpers.NotificationHelper
import org.librefit.util.ExerciseDC

class MainApplication : Application() {
    companion object{
        lateinit var workoutDatabase: WorkoutDatabase
        lateinit var notificationHelper: NotificationHelper
        lateinit var exercisesList: List<ExerciseDC>
    }

    override fun onCreate() {
        super.onCreate()

        workoutDatabase = Room.databaseBuilder(
            applicationContext,
            WorkoutDatabase::class.java,
            WorkoutDatabase.NAME
        ).build()

        notificationHelper = NotificationHelper(this)

        exercisesList = loadExercisesFromRaw(this)
    }

    private fun loadExercisesFromRaw(context: Context): List<ExerciseDC> {
        val inputStream = context.resources.openRawResource(R.raw.exercises)

        return inputStream.bufferedReader().use { reader ->
            val gson = GsonBuilder()
                .registerTypeAdapter(ExerciseDC::class.java, ExerciseDeserializer())
                .create()
            val listType = object : TypeToken<List<ExerciseDC>>() {}.type

            gson.fromJson(reader, listType)
        }
    }
}