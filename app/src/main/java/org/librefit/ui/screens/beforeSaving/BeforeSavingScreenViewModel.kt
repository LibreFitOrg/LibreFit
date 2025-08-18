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

package org.librefit.ui.screens.beforeSaving

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.librefit.db.entity.Workout
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.SetMode
import org.librefit.enums.WorkoutState
import org.librefit.helpers.DataHelper
import org.librefit.services.WorkoutServiceManager
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.models.UiWorkoutWithExercisesAndSets
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.models.mappers.toUi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BeforeSavingScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val workoutServiceManager: WorkoutServiceManager,
    private val dataHelper: DataHelper
) : ViewModel() {

    companion object {
        private const val WORKOUT_WITH_EXERCISES_AND_SET_KEY = "workoutWithExercisesAndSets"
    }

    private val workoutWithExercisesAndSetsJson = savedStateHandle
        .get<String>(WORKOUT_WITH_EXERCISES_AND_SET_KEY)
        ?: error("WORKOUT_WITH_EXERCISES_AND_SET_KEY does not match `Route.BeforeSavingScreen` parameter")


    private val workoutWithExercisesAndSets: UiWorkoutWithExercisesAndSets =
        workoutWithExercisesAndSetsJson
            .let { it ->
                Json.decodeFromString<WorkoutWithExercisesAndSets>(Uri.decode(it)).toUi()
            }

    val exercises = workoutWithExercisesAndSets.exercisesWithSets

    private val _volume = MutableStateFlow("0.00")
    val volume = _volume.asStateFlow()

    init {
        viewModelScope.launch {
            val volume = dataHelper.fetchVolumeFromWorkout(
                WorkoutWithExercisesAndSets(Workout(), exercises.map { it.toEntity() })
            )

            _volume.update {
                String.format(Locale.getDefault(), "%.2f", volume)
            }
        }
    }


    private val _workout = MutableStateFlow(workoutWithExercisesAndSets.workout)
    val workout = _workout.asStateFlow()


    fun setTimeElapsed(timeElapsed: Int) {
        _workout.update { currentWorkout ->
            currentWorkout.copy(timeElapsed = timeElapsed)
        }
    }

    fun updateWorkoutTitle(string: String) {
        _workout.update { currentWorkout ->
            currentWorkout.copy(title = string)
        }
    }

    fun isTitleEmpty(): Boolean {
        return workout.value.title.isEmpty()
    }

    fun isTitleTooLong(): Boolean {
        return workout.value.title.length >= 30
    }

    fun updateWorkoutNotes(newNotes: String) {
        _workout.update { currentWorkout ->
            currentWorkout.copy(notes = newNotes)
        }
    }

    fun detachWorkoutFromRoutine() {
        _workout.update { currentWorkout ->
            currentWorkout.copy(routineId = Random.nextLong())
        }
        _routine.update {
            UiWorkout()
        }
    }

    fun updateCompletedDate(selectedDateMillis: Long?) {
        if (selectedDateMillis != null) {
            _workout.update { currentWorkout ->
                currentWorkout.copy(
                    completed = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(selectedDateMillis),
                        ZoneId.systemDefault()
                    )
                )
            }
        }
    }


    private val _routine = MutableStateFlow(UiWorkout())
    val routine = _routine.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _routine.update {
                    workoutRepository.getRoutineFromRoutineID(workout.value.routineId).toUi()
                }
                delay(100)
            }
        }
    }



    fun saveExercisesWithWorkout() {
        workoutServiceManager.stopService()

        viewModelScope.launch {
            workoutRepository.addWorkoutWithExercisesAndSets(
                WorkoutWithExercisesAndSets(
                    workout = workout.value.copy(state = WorkoutState.COMPLETED).toEntity(),
                    exercisesWithSets = exercises.map { exercise ->
                        exercise.toEntity().copy(
                            sets = exercise.toEntity().sets.map {
                                // This keeps only relevant data on the actual type of set
                                when (exercise.exercise.setMode) {
                                    SetMode.DURATION -> it.copy(reps = 0, load = 0f)
                                    SetMode.BODYWEIGHT -> it.copy(elapsedTime = 0, load = 0f)
                                    SetMode.BODYWEIGHT_WITH_LOAD -> it.copy(elapsedTime = 0)
                                    SetMode.LOAD -> it.copy(elapsedTime = 0)
                                }
                            }
                        )
                    }
                )
            )
        }
    }
}