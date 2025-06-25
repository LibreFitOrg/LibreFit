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

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.librefit.db.entity.Workout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.SetMode
import org.librefit.services.WorkoutServiceManager
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BeforeSavingScreenViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val workoutServiceManager: WorkoutServiceManager
) : ViewModel() {

    private var exercises = mutableStateListOf<ExerciseWithSets>()

    fun initializeExercises(exercises: List<ExerciseWithSets>) {
        this.exercises.addAll(exercises)
    }

    fun getExercises(): List<ExerciseWithSets> {
        return exercises
    }

    fun getVolumeExercises(): String {
        return exercises.sumOf {
            it.sets.sumOf { set ->
                // TODO: fix volume here and in InfoWorkoutViewModel
                if (it.exercise.setMode == SetMode.LOAD_ONLY && set.completed) {
                    set.load.toDouble() * set.reps
                } else 0.0
            }
        }.toFloat().toString().format(Locale.getDefault(), "%.2f")
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


    fun initializeWorkout(workout: Workout) {
        this.workout.value = workout
    }

    fun getTimeElapsed(): Int {
        return workout.value.timeElapsed
    }

    fun setTimeElapsed(timeElapsed: Int) {
        workout.value = workout.value.copy(timeElapsed = timeElapsed)
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

    fun updateWorkoutNotes(newNotes: String) {
        workout.value = workout.value.copy(notes = newNotes)
    }

    fun getWorkoutNotes(): String {
        return workout.value.notes
    }

    fun detachWorkoutFromRoutine() {
        workout.value = workout.value.copy(
            routineId = System.currentTimeMillis()
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

    fun initializeRoutine(routine: Workout) {
        this.routine.value = routine
    }

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


    fun saveExercisesWithWorkout() {
        val list = this.exercises.map { exercise ->
            exercise.copy(sets = exercise.sets.map {
                // This keeps only relevant data on the actual type of set
                when (exercise.exercise.setMode) {
                    SetMode.DURATION -> it.copy(reps = 0, load = 0f)
                    SetMode.REPS -> it.copy(elapsedTime = 0, load = 0f)
                    SetMode.LOAD_AND_BODY_WEIGHT -> it.copy(elapsedTime = 0)
                    SetMode.LOAD_ONLY -> it.copy(elapsedTime = 0)
                }
            })
        }

        workoutServiceManager.stopService()

        viewModelScope.launch(Dispatchers.IO) {
            workoutRepository.addWorkoutWithExercisesAndSets(
                WorkoutWithExercisesAndSets(workout.value, list)
            )
        }
    }
}