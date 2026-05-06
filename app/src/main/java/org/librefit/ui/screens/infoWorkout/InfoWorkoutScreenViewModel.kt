/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.infoWorkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.WorkoutState
import org.librefit.enums.chart.WorkoutChart
import org.librefit.helpers.DataHelper
import org.librefit.nav.Route
import org.librefit.ui.components.charts.Point
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.models.mappers.toUi
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

    private val workoutId = savedStateHandle.toRoute<Route.InfoWorkoutScreen>().workoutId

    private val _volume = MutableStateFlow("")
    val volume = _volume.asStateFlow()

    private val _workout = MutableStateFlow(UiWorkout())
    val workout = _workout.asStateFlow()

    init {
        require(workoutId != 0L) { "workoutId must be not equal to 0" }

        viewModelScope.launch(Dispatchers.IO) {
            val workoutWithExercisesAndSets =
                workoutRepository.getWorkoutWithExercisesAndSets(workoutId)

            val workout = workoutWithExercisesAndSets.workout

            _workout.update {
                workout
            }

            _exercises.update {
                workoutWithExercisesAndSets.exercisesWithSets
            }

            if (isRoutine()) {
                _routine.update {
                    workout
                }

                //If the workout is a routine, then retrieve all past completed workout associated with it
                _completedWorkoutsWithExercises.update {
                    workoutRepository
                        .getCompletedWorkoutsWithExercisesAndSetsFromRoutine(routineId = workout.routineId)
                }
            } else {
                _routine.update {
                    workoutRepository.getRoutineFromRoutineID(workout.routineId).toUi()
                }
            }


            // Calculate volume
            val volumeValue = dataHelper.fetchVolumeFromWorkout(
                WorkoutWithExercisesAndSets(
                    workout.toEntity(),
                    exercises.value.map { it.toEntity() })
            )

            _volume.update {
                String.format(Locale.getDefault(), "%.2f", volumeValue)
            }
        }
    }

    fun getDate(): String {
        val date = if (isRoutine()) workout.value.created else workout.value.completed
        return Formatter.getFullDateFromLocalDate(date)
    }

    fun isRoutine(): Boolean {
        return workout.value.state == WorkoutState.ROUTINE
    }

    fun deleteWorkout() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.deleteWorkout(workout.value.toEntity())
        }
    }

    fun detachWorkoutFromRoutine() {
        _workout.update { currentWorkout ->
            currentWorkout.copy(
                routineId = Random.nextLong()
            )
        }

        _routine.update {
            UiWorkout()
        }

        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.updateWorkout(workout.value.toEntity())
        }
    }


    private val _routine = MutableStateFlow(UiWorkout())
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
        _workoutChart.update {
            value
        }
    }
}