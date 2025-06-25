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

package org.librefit.ui.screens.infoWorkout

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.librefit.data.ChartData
import org.librefit.db.entity.Workout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.SetMode
import org.librefit.enums.chart.WorkoutChart
import org.librefit.helpers.ChartDataHelper
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class InfoWorkoutScreenViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private var initialized = false

    private var _workout = MutableStateFlow<Workout>(Workout())
    val workout = _workout.asStateFlow()

    fun initialize(
        workout: Workout,
        passedRoutine: Workout,
        passedExercises: List<ExerciseWithSets>
    ) {
        if (!initialized) {
            initialized = true
            _workout.value = workout
            if (isRoutine()) {
                fetchCompletedWorkoutsFromDB()
            }

            _routine.value = passedRoutine

            _exercises.value = passedExercises
        }
    }


    fun getDate(): String {
        val date = if (isRoutine()) workout.value.created else workout.value.completed
        return date.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(
                Locale.getDefault()
            )
        )
    }



    fun isRoutine(): Boolean {
        return workout.value.routine
    }


    private var _routine = MutableStateFlow<Workout>(Workout())
    val routine = _routine.asStateFlow()


    private var _exercises = MutableStateFlow<List<ExerciseWithSets>>(emptyList())
    val exercises = _exercises.asStateFlow()

    fun getVolumeExercises(): String {
        val value = exercises.value.sumOf {
            it.sets.sumOf { set ->
                if (it.exercise.setMode == SetMode.LOAD_ONLY) {
                    if (isRoutine()) {
                        (set.load * set.reps).toDouble()
                    } else {
                        if (set.completed) {
                            (set.load * set.reps).toDouble()
                        } else 0.0
                    }
                } else 0.0
            }
        }

        return String.format(Locale.getDefault(), "%.2f", value)
    }


    private var workoutChart = mutableStateOf(WorkoutChart.DURATION)

    private val _listChartData = MutableStateFlow<List<ChartData>>(emptyList())
    val listChartData = _listChartData.asStateFlow()

    @Inject
    lateinit var chartDataHelper: ChartDataHelper

    suspend fun fetchListChartData() = coroutineScope {
        _listChartData.value = chartDataHelper.fetchListChartData(
            workoutChart = workoutChart.value,
            workoutsWithExercises = completedWorkoutsWithExercises
        )
    }

    fun updateChartMode(value: WorkoutChart) {
        workoutChart.value = value
    }

    fun getChartMode(): WorkoutChart {
        return workoutChart.value
    }

    /**
     * All the completed [WorkoutWithExercisesAndSets] linked to [routine] by [Workout.routineId]
     */
    private var completedWorkoutsWithExercises = mutableStateListOf<WorkoutWithExercisesAndSets>()

    fun fetchCompletedWorkoutsFromDB() {
        viewModelScope.launch(Dispatchers.IO) {
            completedWorkoutsWithExercises.addAll(
                workoutRepository.getCompletedWorkoutsWithExercisesAndSetsFromRoutine(workout.value.routineId)
            )
        }
    }

    fun deleteWorkout() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.deleteWorkout(workout.value)
        }
    }

    fun detachWorkoutFromRoutine() {
        _workout.value = workout.value.copy(
            routineId = System.currentTimeMillis()
        )

        _routine.value = Workout()

        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.updateWorkout(workout.value)
        }
    }
}