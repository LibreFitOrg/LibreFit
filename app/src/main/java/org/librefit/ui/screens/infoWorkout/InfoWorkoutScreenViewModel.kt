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

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.librefit.db.entity.Workout
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.chart.WorkoutChart
import org.librefit.helpers.DataHelper
import org.librefit.ui.components.charts.Point
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.mappers.toEntity
import org.librefit.util.Formatter
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class InfoWorkoutScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    dataHelper: DataHelper
) : ViewModel() {
    companion object {
        private const val WORKOUT_ID_KEY = "workoutId"
    }

    private val workoutId = savedStateHandle.get<Long>(WORKOUT_ID_KEY)
        ?: throw IllegalArgumentException("Invalid WORKOUT_ID_KEY")


    private val _volume = MutableStateFlow("")
    val volume = _volume.asStateFlow()

    private val _workout = MutableStateFlow(Workout())
    val workout = _workout.asStateFlow()

    init {
        require(workoutId != 0L) { "workoutId must be not equal to 0" }

        viewModelScope.launch(Dispatchers.IO) {
            val workoutWithExercisesAndSets =
                workoutRepository.getWorkoutWithExercisesAndSets(workoutId)

            _workout.value = workoutWithExercisesAndSets.workout

            _exercises.value = workoutWithExercisesAndSets.exercisesWithSets

            if (isRoutine()) {
                _routine.value = workout.value

                //If the workout is a routine, then retrieve all past completed workout associated with it
                _completedWorkoutsWithExercises.value = workoutRepository
                    .getCompletedWorkoutsWithExercisesAndSetsFromRoutine(routineId = workout.value.routineId)
            } else {
                _routine.value = workoutRepository.getRoutineFromRoutineID(workout.value.routineId)
            }


            // Calculate volume
            val volumeValue = dataHelper.fetchVolumeFromWorkout(
                WorkoutWithExercisesAndSets(workout.value, exercises.value.map { it.toEntity() })
            )

            _volume.value = String.format(Locale.getDefault(), "%.2f", volumeValue)
        }
    }

    fun getDate(): String {
        val date = if (isRoutine()) workout.value.created else workout.value.completed
        return Formatter.getLongDateFromLocalDate(date)
    }

    fun isRoutine(): Boolean {
        return workout.value.routine
    }

    fun deleteWorkout() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.deleteWorkout(workout.value)
        }
    }

    fun detachWorkoutFromRoutine() {
        _workout.value = workout.value.copy(
            routineId = Random.Default.nextLong()
        )

        _routine.value = Workout()

        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.updateWorkout(workout.value)
        }
    }


    private val _routine = MutableStateFlow(Workout())
    val routine = _routine.asStateFlow()


    private val _exercises = MutableStateFlow<List<UiExerciseWithSets>>(emptyList())
    val exercises = _exercises.asStateFlow()



    private val _completedWorkoutsWithExercises =
        MutableStateFlow<List<WorkoutWithExercisesAndSets>>(emptyList())
    private val completedWorkoutsWithExercises = _completedWorkoutsWithExercises.asStateFlow()

    private val _workoutChart = MutableStateFlow(WorkoutChart.DURATION)
    val workoutChart = _workoutChart.asStateFlow()

    val points: StateFlow<List<Point>> = combine(
        workoutChart,
        completedWorkoutsWithExercises
    ) { w, ce ->
        dataHelper.fetchPointsForWorkoutsChart(
            workoutChart = w,
            workoutsWithExercises = ce
        )
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateChartMode(value: WorkoutChart) {
        _workoutChart.value = value
    }
}