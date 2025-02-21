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

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.librefit.db.Workout
import org.librefit.db.WorkoutDao
import org.librefit.enums.ChartMode
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
    private var volume = mutableStateListOf<Float>()
    private var reps = mutableStateListOf<Int>()

    val longFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(
        Locale.getDefault()
    )

    val shortFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(
        Locale.getDefault()
    )

    private var chartMode = mutableStateOf(ChartMode.DURATION)

    fun getYAxisDataChart(): List<Float> {
        return workoutList.mapIndexed { index, it ->
            when (chartMode.value) {
                ChartMode.DURATION -> it.timeElapsed / 60f
                ChartMode.VOLUME -> if (index < volume.lastIndex) volume[index] else 0f
                ChartMode.REPS -> if (index < reps.lastIndex) reps[index].toFloat() else 0f
            }
        }
    }

    fun getXAxisDataChart(): List<String> {
        return workoutList.map { it ->
            it.completed.format(shortFormatter).toString()
        }
    }

    fun updateChartMode(value: ChartMode) {
        chartMode.value = value
    }

    fun getChartMode(): ChartMode {
        return chartMode.value
    }


    fun getWorkoutListFromDB() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.getCompletedWorkouts()
                .distinctUntilChanged()
                .collect { workouts ->
                    workoutList.clear()
                    workoutList.addAll(workouts)

                    volume.clear()
                    reps.clear()
                    workoutList.forEach { workout ->
                        // For each exercise, fetch its sets. Then flatten the nested list of sets into one list.
                        val allSets = workoutDao.getExercisesFromWorkout(workout.id)
                            .flatMap { workoutDao.getSetsFromExercise(it.id) }
                        // Calculate workoutVolume and workoutReps only from the completed sets.
                        val (workoutVolume, workoutReps) = allSets.asSequence()
                            .filter { it.completed }
                            .fold(0f to 0) { (volumeAcc, repsAcc), set ->
                                (volumeAcc + set.weight * set.reps) to (repsAcc + set.reps)
                            }
                        volume.add(workoutVolume)
                        reps.add(workoutReps)
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