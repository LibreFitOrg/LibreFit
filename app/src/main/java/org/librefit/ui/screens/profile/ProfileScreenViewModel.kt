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

package org.librefit.ui.screens.profile

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.librefit.db.Workout
import org.librefit.db.WorkoutDao
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val workoutDao: WorkoutDao
) : ViewModel() {
    val workoutList = mutableStateListOf<Workout>()
    private var volume: List<Float> = listOf()
    private var reps: List<Int> = listOf()

    val longFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(
        Locale.getDefault()
    )

    val shortFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(
        Locale.getDefault()
    )

    private var chartMode = mutableIntStateOf(0)

    fun getYAxisDataChart(): List<Float> {
        return workoutList.mapIndexed { index, it ->
            when (chartMode.intValue) {
                0 -> it.timeElapsed / 60f
                1 -> if (index < volume.lastIndex) volume[index] else 0f
                else -> if (index < reps.lastIndex) reps[index].toFloat() else 0f
            }
        }
    }

    fun getXAxisDataChart(): List<String> {
        return workoutList.map { it ->
            it.completed.format(shortFormatter).toString()
        }
    }

    fun updateChartMode(value: Int) {
        chartMode.intValue = value
    }

    fun getChartMode(): Int {
        return chartMode.intValue
    }


    init {
        getWorkoutList()
    }

    private fun getWorkoutList() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.getCompletedWorkouts()
                .distinctUntilChanged()
                .collect { workouts ->
                    workoutList.clear()
                    workoutList.addAll(workouts)

                    volume = emptyList()
                    reps = emptyList()
                    workouts.forEach {
                        var volumeData = 0f
                        var repsData = 0
                        workoutDao.getExercisesFromWorkout(it.id).forEach { exercise ->
                            workoutDao.getSetsFromExercise(exercise.id).forEach { set ->
                                volumeData += if (set.completed) set.weight * set.reps else 0f
                                repsData += if (set.completed) set.reps else 0
                            }
                        }
                        volume += volumeData
                        reps += repsData
                    }
                }
        }
    }


    fun getWeekStreak(): Int {
        if (workoutList.size < 2) return 0

        if (ChronoUnit.DAYS.between(workoutList.first().completed, LocalDateTime.now()) > 7) {
            return 0
        }

        var index = workoutList.lastIndex

        for (i in 0 until workoutList.size - 1) {
            if (ChronoUnit.DAYS.between(
                    workoutList[i + 1].completed,
                    workoutList[i].completed
                ) > 7
            ) {
                index = i
                break
            }
        }

        return ChronoUnit.WEEKS.between(workoutList[index].completed, LocalDateTime.now()).toInt()
    }

}