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

package org.librefit.ui.screens.edit

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.librefit.db.Set
import org.librefit.db.Workout
import org.librefit.db.WorkoutDao
import org.librefit.enums.SetMode
import org.librefit.util.ExerciseWithSets
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class EditWorkoutScreenViewModel @Inject constructor(
    private val workoutDao: WorkoutDao
) : ViewModel() {
    val exercises = mutableStateListOf<ExerciseWithSets>()

    fun getExercises(): List<ExerciseWithSets> {
        return exercises.toList()
    }

    fun addExerciseWithSets(exerciseWithSets: ExerciseWithSets) {
        val newExerciseWithSets = exerciseWithSets.copy(
            id = Random.nextInt(),
            sets = if (exerciseWithSets.sets.isEmpty()) {
                listOf(Set(id = Random.nextInt()))
            } else exerciseWithSets.sets
        )
        exercises.add(newExerciseWithSets)
    }

    fun addSetToExercise(index: Int) {
        val exercise = exercises[index]
        exercises[index] = exercise.copy(sets = exercise.sets + listOf(Set(id = Random.nextInt())))
    }

    /**
     * Updates a specific [Set] within an [ExerciseWithSets] by assigning a new value to one of its
     * attributes based on the specified mode.
     *
     * @param index The index of the [ExerciseWithSets] in the [exercises] list that contains the
     * set to be updated.
     *
     * @param set The [Set] object that needs to be updated.
     * @param value The new value to assign to the specified attribute of the [Set].
     * @param mode An integer that defines which attribute of the [Set] should be updated.
     * The following modes correspond to specific attributes:
     *  - 0: [Set.weight]
     *  - 1: [Set.reps]
     *  - 2: [Set.elapsedTime]
     *  - 3: [Set.completed] (where a [value] of 1 indicates 'true')
     *
     * The method will update the specified attribute of the [Set] if it matches the provided [set] ID.
     * If the [mode] is not recognized, the original [set] will remain unchanged.
     */
    fun updateSet(index: Int, set: Set, value: Float, mode: Int) {
        val exercise = exercises[index]
        exercises[index] = exercise.copy(
            sets = exercise.sets.map {
                if (it.id == set.id) {
                    when (mode) {
                        0 -> set.copy(weight = value)
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
     * Removes a specific set from the sets associated with an exercise in the [exercises] list.
     *
     * This function updates the exercise at the given [index] by filtering out the specified [set]
     * based on its unique identifier. The modified exercise is then saved back to the [exercises] list.
     *
     * @param index The index of the exercise in the [exercises] list from which the set will be deleted.
     * @param set The set to be removed, identified by its unique ID.
     */
    fun deleteSet(index: Int, set: Set) {
        val exercise = exercises[index]
        exercises[index] = exercise.copy(
            sets = exercise.sets.filter { it.id != set.id }
        )
    }

    /**
     * Updates an instance of [ExerciseWithSets] by assigning a [value] to a specified attribute based on the provided [mode].
     *
     * @param index The index of the [ExerciseWithSets] instance in the [exercises] list that needs to be updated.
     * @param value The new value to be assigned to the specified attribute of the [ExerciseWithSets].
     * @param mode An integer that determines which attribute will be updated with the [value].
     * The following modes correspond to specific attributes:
     *  - 0: [ExerciseWithSets.note]
     *  - 1: [ExerciseWithSets.setMode]
     *  - 2: [ExerciseWithSets.restTime]
     *
     * Note: When updating [ExerciseWithSets.setMode], the [value] should be one of the following string representations:
     *  - [SetMode.WEIGHT].name
     *  - [SetMode.TIME].name
     *  - [SetMode.REPS].name;
     * If an invalid string is provided, the default value [SetMode.WEIGHT] will be assigned.
     */
    fun updateExercise(index: Int, value: String, mode: Int) {
        val exercise = exercises[index]
        exercises[index] = when (mode) {
            0 -> exercise.copy(note = value.toString())
            1 -> exercise.copy(
                setMode = when (value) {
                    SetMode.WEIGHT.name -> SetMode.WEIGHT
                    SetMode.TIME.name -> SetMode.TIME
                    SetMode.REPS.name -> SetMode.REPS
                    else -> SetMode.WEIGHT
                }
            )

            2 -> exercise.copy(restTime = Integer.parseInt(value))
            else -> exercise
        }
    }

    fun deleteExercise(index: Int) {
        exercises.removeAt(index)
    }

    fun isListEmpty(): Boolean {
        return exercises.isEmpty()
    }

    private var initialized = false
    private val workout = mutableStateOf(Workout())
    private var routine = Workout()
    private var isRoutine = false


    fun initialize(workout: Workout, newExercises: List<ExerciseWithSets>, routine: Workout) {
        if (!initialized) {
            isRoutine = workout.id == 0 || workout.routine

            this.workout.value = workout.copy(routine = false)

            exercises.addAll(newExercises)

            this.routine = routine

            initialized = true
        }
    }

    fun getWorkout(): Workout {
        return workout.value
    }

    fun getTitle(): String {
        return workout.value.title
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

    fun isTitleAllowed(): Boolean {
        return !isTitleEmpty() && !isTitleTooLong()
    }

    fun updateNotes(string: String) {
        workout.value = workout.value.copy(notes = string)
    }

    fun getNotes(): String {
        return workout.value.notes
    }


    fun saveWorkoutWithExercisesInDB() {
        val list = exercises.toList()
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.addWorkoutWithExercises(workout.value.copy(routine = isRoutine), list)
        }
    }


    /**
     * Returns `null` when a new routine is created, `true` when a routine is edited and `false` when
     * a past workout is edited
     */
    fun getTypeOfEdit(): Boolean? {
        return if (workout.value.id == 0) null else isRoutine
    }
}

