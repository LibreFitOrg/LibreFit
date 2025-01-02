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

package org.librefit.ui.screens.infoRoutine

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.librefit.MainApplication
import org.librefit.db.Workout
import org.librefit.enums.SetMode
import org.librefit.util.ExerciseWithSets
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.random.Random

class InfoRoutineScreenViewModel(
    private val workoutId: Int
) : ViewModel() {
    private val routine = mutableStateOf(Workout())

    fun getCreatedDate(): String {
        return routine.value.created.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(
                Locale.getDefault()
            )
        )
    }

    fun getNotes(): String {
        return routine.value.notes
    }


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

    fun getVolumeExercises(): String {
        var stringValue = exercises.sumOf {
            it.sets.sumOf { set ->
                if (it.setMode == SetMode.WEIGHT) {
                    (set.weight * set.reps).toDouble()
                } else 0.toDouble()
            }
        }.toString()

        val firstDotIndex = stringValue.indexOf(".")

        if (firstDotIndex != -1) {
            val beforeFirstDot = stringValue.substring(
                0, firstDotIndex + 1
            )

            val afterFirstDot = stringValue
                .substring(firstDotIndex + 1)
                .take(3)

            stringValue = beforeFirstDot + afterFirstDot
        }

        return stringValue
    }

    fun getTotalExercises(): String {
        return exercises.size.toString()
    }

    fun getTotalSets(): String {
        return exercises.sumOf { it.sets.size }.toString()
    }


    private val workoutDao = MainApplication.workoutDatabase.getWorkoutDao()

    init {
        getExercisesFromDB()
        getRoutineFromDB()
    }

    private fun getExercisesFromDB() {
        viewModelScope.launch(Dispatchers.IO) {
            val exercises = workoutDao.getExercisesFromWorkout(workoutId)
            exercises.forEach { exercise ->
                val exerciseDC =
                    MainApplication.exercisesList.associateBy { it.id }[exercise.exerciseId]
                if (exerciseDC != null) {
                    addExerciseWithSets(
                        ExerciseWithSets(
                            exerciseDC = exerciseDC,
                            exerciseId = exercise.id,
                            note = exercise.notes,
                            sets = workoutDao.getSetsFromExercise(exercise.id),
                            setMode = exercise.setMode,
                            restTime = exercise.restTime
                        )
                    )
                }
            }
        }
    }


    private fun getRoutineFromDB() {
        viewModelScope.launch(Dispatchers.IO) {
            routine.value = workoutDao.getWorkout(workoutId)
        }
    }

    fun deleteRoutine() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.deleteWorkout(routine.value)
        }
    }
}