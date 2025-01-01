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

package org.librefit.ui.screens.workout.beforeSaving

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.librefit.MainApplication
import org.librefit.db.Workout
import org.librefit.enums.SetMode
import org.librefit.util.ExerciseWithSets
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class BeforeSavingScreenViewModel : ViewModel() {

    private var exercises = emptyList<ExerciseWithSets>()

    fun initializeExercises(exercises: List<ExerciseWithSets>) {
        this.exercises = exercises
    }

    fun getExercises(): List<ExerciseWithSets> {
        return exercises
    }

    fun getVolumeExercises(): String {
        return exercises.sumOf {
            it.sets.sumOf { set ->
                if (it.setMode == SetMode.WEIGHT && set.completed) {
                    set.weight.toDouble() * set.reps
                } else 0.toDouble()
            }
        }.toString()
    }

    fun getTotalSets(): Int {
        return exercises.sumOf {
            it.sets.size
        }
    }

    fun getCompletedSets(): Int {
        return exercises.sumOf {
            it.sets.filter { it.completed }.size
        }
    }


    private val workout = mutableStateOf(Workout())


    fun getTimeElapsed(): Int {
        return workout.value.timeElapsed
    }

    fun setTimeElapsed(timeElapsed: Int) {
        workout.value = workout.value.copy(timeElapsed = timeElapsed)
    }

    fun savePassedWorkout(workout: Workout) {
        this.workout.value = workout
        if (workout.id != 0) {
            getRoutineFromDB(workout.id)
        }
    }

    fun updateWorkoutTitle(string: String) {
        workout.value = workout.value.copy(title = string)
    }

    fun getWorkoutTitle(): String {
        return workout.value.title
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

    fun updateWorkoutNotes(newNotes: String) {
        workout.value = workout.value.copy(notes = newNotes)
    }

    fun getWorkoutNotes(): String {
        return workout.value.notes
    }

    fun detachWorkoutFromRoutine() {
        workout.value = workout.value.copy(
            workoutId = System.currentTimeMillis()
        )
        routine.value = Workout()
    }

    fun updateCompletedDate(selectedDateMillis: Long?) {
        if (selectedDateMillis != null) {
            workout.value = workout.value.copy(
                completed = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(selectedDateMillis),
                    ZoneId.systemDefault()
                )
            )
        }
    }

    fun getCompletedDate(): String {
        return workout.value.completed.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(
                Locale.getDefault()
            )
        )
    }


    private var routine = mutableStateOf(Workout())

    fun getRoutineTitle(): String {
        return routine.value.title
    }

    fun getRoutineDate(): String {
        return routine.value.created.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(
                Locale.getDefault()
            )
        )
    }


    private val workoutDao = MainApplication.workoutDatabase.getWorkoutDao()

    fun saveExercisesWithWorkout() {
        val list = this.exercises.map { exercise ->
            exercise.copy(sets = exercise.sets.map {
                // This keeps only relevant data on the actual type of set
                when (exercise.setMode) {
                    SetMode.TIME -> it.copy(reps = 0, weight = 0f)
                    SetMode.REPS -> it.copy(elapsedTime = 0, weight = 0f)
                    SetMode.WEIGHT -> it.copy(elapsedTime = 0)
                }
            })
        }

        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.addWorkoutWithExercises(
                workout = workout.value.copy(id = 0),
                exercises = list
            )
        }
    }

    private fun getRoutineFromDB(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            routine.value = workoutDao.getWorkout(id)


            workout.value = workout.value.copy(
                notes = routine.value.notes,
                workoutId = routine.value.workoutId,
                created = routine.value.created
            )
        }
    }
}