/*
 * Copyright (c) 2024. LibreFit
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

package org.librefit.ui.screens.infoRoutine

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.librefit.MainApplication
import org.librefit.util.ExerciseDC
import org.librefit.util.ExerciseWithSets
import kotlin.random.Random

class InfoRoutineScreenViewModel(
    workoutId: Int,
    private val list: List<ExerciseDC>
) : ViewModel() {

    val exercises = mutableStateListOf<ExerciseWithSets>()

    fun getExercises(): List<ExerciseWithSets> {
        return exercises.toList()
    }

    fun addExerciseWithSets(exerciseWithSets: ExerciseWithSets) {
        val newExerciseWithSets = exerciseWithSets.copy(
            id = Random.nextInt(),
            sets = if (exerciseWithSets.sets.isEmpty()) {
                listOf(org.librefit.db.Set(id = Random.nextInt()))
            } else exerciseWithSets.sets
        )
        exercises.add(newExerciseWithSets)
    }


    private val workoutDao = MainApplication.workoutDatabase.getWorkoutDao()

    init {
        getExercisesFromWorkout(workoutId)
    }

    private fun getExercisesFromWorkout(workoutId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val exercises = workoutDao.getExercisesFromWorkout(workoutId)
            exercises.forEach { exercise ->
                val exerciseDC = list.associateBy { it.id }[exercise.exerciseId]
                if (exerciseDC != null) {
                    Log.d("InfoRoutine", "Exercise added")
                    addExerciseWithSets(
                        ExerciseWithSets(
                            exerciseDC = exerciseDC,
                            exerciseId = exercise.id,
                            note = exercise.notes,
                            setMode = exercise.setMode,
                            restTime = exercise.restTime
                        )
                    )

                    getSetsFromExercise(exercise.id)
                }
            }
        }
    }

    private fun getSetsFromExercise(exerciseId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val sets = workoutDao.getSetsFromExercise(exerciseId)
            val exercise = exercises.find { it.exerciseId == exerciseId }!!
            val index = exercises.indexOf(exercise)
            exercises[index] = exercise.copy(sets = sets.map { it.copy(id = Random.nextInt()) })
        }
    }

}