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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.chart.WorkoutChart
import org.librefit.helpers.DataHelper
import org.librefit.ui.components.charts.Point
import org.librefit.ui.models.mappers.toUi
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
    dataHelper: DataHelper
) : ViewModel() {
    val workoutsWithExercises = workoutRepository.getCompletedWorkoutsWithExercisesAndSets()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val workoutsWithExercisesUi = workoutsWithExercises.map { workoutWithExercises ->
        workoutWithExercises.map { it.toUi() }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _workoutChart = MutableStateFlow(WorkoutChart.DURATION)
    val workoutChart = _workoutChart.asStateFlow()


    fun updateChartMode(value: WorkoutChart) {
        _workoutChart.value = value
    }


    val points: StateFlow<List<Point>> = combine(
        workoutsWithExercises,
        workoutChart
    ) { we, wc ->
        dataHelper.fetchPointsForWorkoutsChart(wc, we)
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weekStreak: StateFlow<Int> = workoutsWithExercises
        .map { workouts ->
            if (workouts.isEmpty()) {
                return@map 0
            }

            val mostRecentWorkoutDate = workouts.first().workout.completed

            // If the most recent workout was over a week ago, the streak is 0.
            if (ChronoUnit.DAYS.between(mostRecentWorkoutDate, LocalDateTime.now()) > 7) {
                return@map 0
            }

            //  Find the first "break" in the streak using functional operators
            // `windowed(2)` creates pairs of adjacent workouts: [[w0, w1], [w1, w2], ...]
            // `indexOfFirst` finds the index of the first pair that breaks the streak.
            val breakIndex =
                workouts.windowed(size = 2).indexOfFirst { (newerWorkout, olderWorkout) ->
                    val daysBetween = ChronoUnit.DAYS.between(
                        olderWorkout.workout.completed,
                        newerWorkout.workout.completed
                    )
                    daysBetween > 7
                }

            // Determine the start date of the current streak
            val streakStartDate = if (breakIndex == -1) {
                // No break was found; the streak includes all workouts. The start is the oldest one.
                workouts.last().workout.completed
            } else {
                // A break was found. The valid streak starts with the "newer" workout of the pair that broke the streak.
                // The index of this workout is the same as the index of the window.
                workouts[breakIndex].workout.completed
            }

            //  Calculate the final streak in weeks
            ChronoUnit.WEEKS.between(streakStartDate, LocalDateTime.now()).toInt()
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}