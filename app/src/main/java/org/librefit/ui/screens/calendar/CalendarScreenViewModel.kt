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

package org.librefit.ui.screens.calendar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.librefit.db.Workout
import org.librefit.db.WorkoutDao
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CalendarScreenViewModel @Inject constructor(
    private val workoutDao: WorkoutDao
) : ViewModel() {
    val workoutList = mutableStateListOf<Workout>()

    init {
        getWorkoutListFromDB()
    }


    @OptIn(ExperimentalMaterial3Api::class)
    fun getSelectableDatesFromWorkouts(): SelectableDates {
        return object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                return workoutList.map { it.completed.toLocalDate() }.contains(date)
            }
        }
    }

    fun getWorkoutsFromDate(utcTimeMillis: Long?): List<Workout> {
        if (utcTimeMillis == null) return emptyList()

        val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()

        return workoutList.filter { it.completed.toLocalDate() == date }
    }

    fun getTimeFromLocalDateTime(date: LocalDateTime): String {
        return date.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    private fun getWorkoutListFromDB() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.getCompletedWorkouts()
                .distinctUntilChanged()
                .collect { workouts ->
                    workoutList.clear()
                    workoutList.addAll(workouts)
                }
        }
    }
}