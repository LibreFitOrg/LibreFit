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

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.librefit.data.ExerciseDC
import org.librefit.db.entity.Exercise
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
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    val exercisesWithSets = mutableStateListOf<ExerciseWithSets>()

    fun addExerciseWithSets(exerciseDC: ExerciseDC) {
        exercisesWithSets.add(
            ExerciseWithSets(
                exercise = Exercise(
                    exerciseId = exerciseDC.id,
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
        )
    }

    fun addSetToExercise(index: Int) {
        val exercise = exercisesWithSets[index]
        val newSet = exercise.sets
            .lastOrNull()?.copy(id = Random.Default.nextLong())
            ?: Set(id = Random.Default.nextLong())
        exercisesWithSets[index] = exercise.copy(sets = exercise.sets + newSet)
    }

    /**
     * Updates a specific [Set] within an [ExerciseWithSets] by assigning a new value to one of its
     * attributes based on the specified mode.
     *
     * @param index The index of the [ExerciseWithSets] in the [exercisesWithSets] list that contains the
     * set to be updated.
     * @param set The [Set] object that needs to be updated.
     * @param value The new value to assign to the specified attribute of the [Set].
     * @param mode An integer that defines which attribute of the [Set] should be updated.
     * The following modes correspond to specific attributes:
     *  - 0: [Set.load]
     *  - 1: [Set.reps]
     *  - 2: [Set.elapsedTime]
     *  - 3: [Set.completed] (where a [value] of 1 indicates 'true')
     *
     * The method will update the specified attribute of the [Set] if it matches the provided [set] ID.
     * If the [mode] is not recognized, the original [set] will remain unchanged.
     */
    fun updateSet(index: Int, set: Set, value: Float, mode: Int) {
        val exerciseWithSets = exercisesWithSets[index]
        exercisesWithSets[index] = exerciseWithSets.copy(
            sets = exerciseWithSets.sets.map {
                if (it.id == set.id) {
                    when (mode) {
                        0 -> set.copy(load = value)
                        1 -> set.copy(reps = value.toInt())
                        2 -> set.copy(elapsedTime = value.toInt())
                        3 -> set.copy(completed = value == 1f)
                        else -> set
                    }
                } else it
            }
        )
    }

    /**
     * Removes a specific set from the sets associated with an exercise in the [exercisesWithSets] list.
     *
     * This function updates the exercise at the given [index] by filtering out the specified [set]
     * based on its unique identifier. The modified exercise is then saved back to the [exercisesWithSets] list.
     *
     * @param index The index of the exercise in the [exercisesWithSets] list from which the set will be deleted.
     * @param set The set to be removed, identified by its unique ID.
     */
    fun deleteSet(index: Int, set: Set) {
        val exerciseWithSets = exercisesWithSets[index]
        exercisesWithSets[index] = exerciseWithSets.copy(
            sets = exerciseWithSets.sets.filter { it.id != set.id }
        )
    }

    /**
     * Updates an instance of [ExerciseWithSets] by assigning a [value] to a specified attribute based on the provided [mode].
     *
     * @param index The index of the [ExerciseWithSets] instance in the [exercisesWithSets] list that needs to be updated.
     * @param value The new value to be assigned to the specified attribute of the [ExerciseWithSets].
     * @param mode An integer that determines which attribute will be updated with the [value].
     * The following modes correspond to specific attributes:
     *  - 0: [org.librefit.db.entity.Exercise.notes]
     *  - 1: [org.librefit.db.entity.Exercise.setMode]
     *  - 2: [org.librefit.db.entity.Exercise.restTime]
     *
     * Note: When updating [org.librefit.db.entity.Exercise.setMode], the [value] should be one of the following string representations:
     *  - [SetMode.LOAD].name
     *  - [SetMode.DURATION].name
     *  - [SetMode.BODYWEIGHT].name;
     * If an invalid string is provided, the default value [SetMode.LOAD] will be assigned.
     */
    fun updateExercise(index: Int, value: String, mode: Int) {
        val exerciseWithSets = exercisesWithSets[index]
        exercisesWithSets[index] = when (mode) {
            0 -> exerciseWithSets.copy(exercise = exerciseWithSets.exercise.copy(notes = value))
            1 -> exerciseWithSets.copy(
                exercise = exerciseWithSets.exercise.copy(
                    setMode = when (value) {
                        SetMode.LOAD.name -> SetMode.LOAD
                        SetMode.BODYWEIGHT_WITH_LOAD.name -> SetMode.BODYWEIGHT_WITH_LOAD
                        SetMode.DURATION.name -> SetMode.DURATION
                        SetMode.BODYWEIGHT.name -> SetMode.BODYWEIGHT
                        else -> SetMode.LOAD
                    }
                )
            )

            2 -> exerciseWithSets.copy(
                exercise = exerciseWithSets.exercise.copy(
                    restTime = Integer.parseInt(
                        value
                    )
                )
            )

            else -> exerciseWithSets
        }
    }

    fun deleteExercise(index: Int) {
        exercisesWithSets.removeAt(index)
    }


    private var initialized = false
    private val workout = mutableStateOf(Workout())
    private var routine = Workout()
    private var isRoutine = false


    fun initialize(workout: Workout, newExercises: List<ExerciseWithSets>, routine: Workout) {
        if (!initialized) {
            isRoutine = workout.id == 0L || workout.routine

            this.workout.value = workout.copy(routine = false)

            exercisesWithSets.addAll(newExercises)

            this.routine = routine

            initialized = true
        }
    }

    fun getWorkout(): Workout {
        return workout.value
    }

    fun updateTitle(string: String) {
        workout.value = workout.value.copy(title = string)
    }

    fun isTitleEmpty(): Boolean {
        return workout.value.title.isEmpty()
    }

    fun isTitleTooLong(): Boolean {
        return workout.value.title.length >= 30
    }

    fun updateNotes(string: String) {
        workout.value = workout.value.copy(notes = string)
    }


    fun saveWorkoutWithExercisesInDB() {
        val list = exercisesWithSets.toList()
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.addWorkoutWithExercisesAndSets(
                WorkoutWithExercisesAndSets(workout.value.copy(routine = isRoutine), list)
            )
        }
    }


    /**
     * Returns `null` when a new routine is created, `true` when a routine is edited and `false` when
     * a past workout is edited
     */
    fun getTypeOfEdit(): Boolean? {
        return if (workout.value.id == 0L) null else isRoutine
    }
}

