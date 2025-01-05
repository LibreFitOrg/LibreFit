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

package org.librefit.ui.screens.shared

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.librefit.MainApplication
import org.librefit.db.Workout
import org.librefit.enums.Category
import org.librefit.enums.Equipment
import org.librefit.enums.Force
import org.librefit.enums.Level
import org.librefit.enums.Mechanic
import org.librefit.enums.Muscle
import org.librefit.util.ExerciseDC
import org.librefit.util.ExerciseWithSets
import kotlin.random.Random

class SharedViewModel : ViewModel() {
    private val selectedExercisesList = mutableStateListOf<ExerciseDC>()

    fun getSelectedExercisesList(): List<ExerciseDC> {
        val list = selectedExercisesList.toList()
        selectedExercisesList.clear()
        return list
    }

    fun addSelectedExerciseToList(exerciseList: List<ExerciseDC>) {
        resetSelectedExercisesList()
        selectedExercisesList += exerciseList
    }

    fun resetSelectedExercisesList() {
        selectedExercisesList.clear()
    }


    //They are used to filter the exercise list in AddExerciseScreen
    private var levelFilter = mutableStateOf<Level?>(null)
    private var forceFilter = mutableStateOf<Force?>(null)
    private var mechanicFilter = mutableStateOf<Mechanic?>(null)
    private var equipmentFilter = mutableStateOf<Equipment?>(null)
    private var muscleFilter = mutableStateOf<Muscle?>(null)
    private var categoryFilter = mutableStateOf<Category?>(null)


    fun cleanFilter() {
        levelFilter.value = null
        forceFilter.value = null
        mechanicFilter.value = null
        equipmentFilter.value = null
        muscleFilter.value = null
        categoryFilter.value = null
    }

    fun updateFilter(enum: Enum<*>?, mode: Int) {
        when (mode) {
            0 -> levelFilter.value = enum as Level?
            1 -> forceFilter.value = enum as Force?
            2 -> mechanicFilter.value = enum as Mechanic?
            3 -> equipmentFilter.value = enum as Equipment?
            4 -> muscleFilter.value = enum as Muscle?
            5 -> categoryFilter.value = enum as Category?
            else -> null
        }
    }

    fun getFilter(mode: Int): Enum<*>? {
        return when (mode) {
            0 -> levelFilter.value
            1 -> forceFilter.value
            2 -> mechanicFilter.value
            3 -> equipmentFilter.value
            4 -> muscleFilter.value
            5 -> categoryFilter.value
            else -> null
        }
    }

    fun filterExercise(exercise: ExerciseDC): Boolean {
        if (levelFilter.value != null && levelFilter.value != exercise.level) {
            return false
        }
        if (forceFilter.value != null && forceFilter.value != exercise.force) {
            return false
        }
        if (mechanicFilter.value != null && mechanicFilter.value != exercise.mechanic) {
            return false
        }
        if (equipmentFilter.value != null && equipmentFilter.value != exercise.equipment) {
            return false
        }
        if (muscleFilter.value != null && !exercise.primaryMuscles.contains(muscleFilter.value)
            && !exercise.secondaryMuscles.contains(muscleFilter.value)
        ) {
            return false
        }
        if (categoryFilter.value != null && categoryFilter.value != exercise.category) {
            return false
        }
        return true
    }


    private var passedWorkout = Workout()
    private var passedExercises = listOf<ExerciseWithSets>()
    private var passedRoutine = Workout()
    private var workoutId = 0


    fun updateWorkoutId(workoutId: Int) {
        this.workoutId = workoutId
        getDataFromDB()
    }

    fun setPassedData(
        workout: Workout,
        exercises: List<ExerciseWithSets>,
        routine: Workout? = null
    ) {
        passedWorkout = workout
        passedExercises = exercises
        if (routine != null) {
            passedRoutine = routine
        }
    }

    fun getPassedWorkout(): Workout {
        return passedWorkout
    }

    fun getPassedExercises(): List<ExerciseWithSets> {
        return passedExercises
    }

    fun getPassedRoutine(): Workout {
        return passedRoutine
    }


    private val workoutDao = MainApplication.workoutDatabase.getWorkoutDao()

    private fun getDataFromDB() {
        if (workoutId != 0) {
            viewModelScope.launch(Dispatchers.IO) {
                // Retrieves exercises from db and parse them to ExerciseWithSets
                val exercises = workoutDao.getExercisesFromWorkout(workoutId)
                passedExercises = exercises.map { exercise ->
                    ExerciseWithSets(
                        id = Random.nextInt(),
                        exerciseDC = MainApplication.exercisesList.associateBy { it.id }[exercise.exerciseId]!!,
                        exerciseId = exercise.id,
                        note = exercise.notes,
                        sets = workoutDao.getSetsFromExercise(exercise.id),
                        setMode = exercise.setMode,
                        restTime = exercise.restTime
                    )
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                val workout = workoutDao.getWorkout(workoutId)
                if (workout.routine) {
                    passedWorkout = workout.copy(routine = false)
                    passedRoutine = Workout()
                } else {
                    passedWorkout = workout
                    passedRoutine = runCatching { workoutDao.getRoutines().first() }
                        .getOrDefault(emptyList())
                        .find { it.workoutId == workout.workoutId }
                        .takeIf { it?.id != workout.id } ?: Workout()
                }
            }
        } else {
            passedWorkout = Workout()
            passedRoutine = Workout()
            passedExercises = emptyList()
        }

    }
}