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

package org.librefit.ui.screens.editWorkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.entity.Exercise
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.entity.Set
import org.librefit.db.entity.Workout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.SetMode
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class EditWorkoutScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    companion object {
        private const val WORKOUT_ID_KEY = "workoutId"
    }

    private val workoutId = savedStateHandle.get<Long>(WORKOUT_ID_KEY)
        ?: throw IllegalArgumentException("Invalid WORKOUT_ID_KEY")


    private val _isRoutine = MutableStateFlow(false)
    private val isRoutine = _isRoutine.asStateFlow()

    private val _workout = MutableStateFlow(Workout())
    val workout = _workout.asStateFlow()

    private val _routine = MutableStateFlow(Workout())
    val routine = _routine.asStateFlow()

    private val _exercises = MutableStateFlow<List<ExerciseWithSets>>(emptyList())
    val exercises = _exercises.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (workoutId != 0L) {
                val workoutWithExercisesAndSets =
                    workoutRepository.getWorkoutWithExercisesAndSets(workoutId)

                val workoutInDb = workoutWithExercisesAndSets.workout

                _isRoutine.value = workoutInDb.routine

                _workout.value = workoutInDb.copy(routine = false)

                _exercises.value = workoutWithExercisesAndSets.exercisesWithSets
            } else {
                _isRoutine.value = true
            }


            if (isRoutine.value) {
                _routine.value = workout.value
            } else {
                _routine.value = workoutRepository.getRoutineFromRoutineID(workout.value.routineId)
            }
        }
    }

    fun addExerciseWithSets(exerciseDC: ExerciseDC) {
        val newExercise = ExerciseWithSets(
            exercise = Exercise(
                idExerciseDC = exerciseDC.id,
                setMode = when (exerciseDC.category) {
                    Category.STRETCHING -> SetMode.DURATION
                    Category.CARDIO -> SetMode.DURATION
                    else -> when (exerciseDC.equipment) {
                        Equipment.BODY_ONLY -> SetMode.BODYWEIGHT
                        Equipment.FOAM_ROLL -> SetMode.BODYWEIGHT
                        Equipment.EXERCISE_BALL -> SetMode.BODYWEIGHT
                        else -> SetMode.LOAD
                    }
                }
            ),
            exerciseDC = exerciseDC
        )

        _exercises.update { exercises ->
            exercises + newExercise
        }
    }

    fun addSetToExercise(exerciseWithSets: ExerciseWithSets) {
        val newSet = exerciseWithSets.sets
            .lastOrNull()?.copy(id = Random.Default.nextLong())
            ?: Set()

        _exercises.update { exercises ->
            exercises.map { exercise ->
                if (exercise == exerciseWithSets) {
                    exercise.copy(sets = exercise.sets + newSet)
                } else {
                    exercise
                }
            }
        }
    }

    fun updateSet(set: Set) {
        _exercises.update { currentExercises ->
            currentExercises.map { exercise ->
                if (set.id in exercise.sets.map { it.id }) {
                    exercise.copy(
                        sets = exercise.sets.map {
                            if (it.id == set.id) set else it
                        }
                    )
                } else {
                    exercise
                }
            }
        }
    }

    fun deleteSet(set: Set) {
        _exercises.update { currentExercises ->
            currentExercises.map { exercise ->
                if (set in exercise.sets) {
                    exercise.copy(
                        sets = exercise.sets.filter { it != set }
                    )
                } else {
                    exercise
                }
            }
        }
    }

    fun updateExercise(exercise: Exercise) {
        _exercises.update { currentExercises ->
            currentExercises.map {
                if (it.exercise.id == exercise.id) it.copy(exercise = exercise) else it
            }
        }
    }

    fun deleteExercise(exerciseWithSets: ExerciseWithSets) {
        _exercises.update { currentExercises ->
            currentExercises.filter { it != exerciseWithSets }
        }
    }


    fun updateTitle(string: String) {
        _workout.update { it.copy(title = string) }
    }

    fun updateNotes(string: String) {
        _workout.update { it.copy(notes = string) }
    }

    fun isTitleEmpty(): Boolean {
        return workout.value.title.isEmpty()
    }

    fun isTitleTooLong(): Boolean {
        return workout.value.title.length >= 30
    }


    fun saveWorkoutWithExercisesInDB() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.addWorkoutWithExercisesAndSets(
                WorkoutWithExercisesAndSets(
                    workout = workout.value.copy(routine = isRoutine.value),
                    exercisesWithSets = exercises.value
                )
            )
        }
    }


    /**
     * Returns `null` when a new routine is created, `true` when a routine is edited and `false` when
     * a past workout is edited
     */
    fun getTypeOfEdit(): Boolean? {
        return if (workout.value.id == 0L) null else isRoutine.value
    }
}

