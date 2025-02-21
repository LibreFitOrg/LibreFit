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

package org.librefit.ui.screens.infoWorkout

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.librefit.db.Workout
import org.librefit.db.WorkoutDao
import org.librefit.enums.ChartMode
import org.librefit.enums.SetMode
import org.librefit.util.ExerciseWithSets
import org.librefit.util.Formatter.formatTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class InfoWorkoutScreenViewModel @Inject constructor(
    private val workoutDao: WorkoutDao
) : ViewModel() {
    private val workout = mutableStateOf(Workout())

    fun initializeWorkout(workout: Workout) {
        this.workout.value = workout
        if (isRoutine()) {
            getAllPastWorkoutsFromDB()
        }
    }

    fun getWorkoutTitle(): String {
        return workout.value.title
    }

    fun getDate(): String {
        val date = if (isRoutine()) workout.value.created else workout.value.completed
        return date.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(
                Locale.getDefault()
            )
        )
    }

    fun getElapsedTime(): String {
        return formatTime(workout.value.timeElapsed)
    }

    fun getNotes(): String {
        return workout.value.notes
    }

    fun isRoutine(): Boolean {
        return workout.value.routine
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


    val exercises = mutableStateListOf<ExerciseWithSets>()

    fun initializeExercises(newExercises: List<ExerciseWithSets>) {
        exercises.addAll(newExercises)
    }

    fun getVolumeExercises(): String {
        val value = exercises.sumOf {
            it.sets.sumOf { set ->
                if (it.setMode == SetMode.WEIGHT) {
                    if (isRoutine()) {
                        (set.weight * set.reps).toDouble()
                    } else {
                        if (set.completed) {
                            (set.weight * set.reps).toDouble()
                        } else 0.0
                    }
                } else 0.0
            }
        }

        return String.format(Locale.getDefault(), "%.2f", value)
    }

    fun getTotalExercises(): String {
        return exercises.size.toString()
    }

    fun getTotalSets(): String {
        return exercises.sumOf { it.sets.size }.toString()
    }

    fun getCompletedSets(): String {
        return exercises.sumOf { it.sets.filter { it.completed == true }.size }.toString()
    }


    val shortFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(
        Locale.getDefault()
    )
    private var chartMode = mutableStateOf(ChartMode.DURATION)

    fun getYAxisDataChart(): List<Float> {
        return pastWorkouts.mapIndexed { index, it ->
            when (chartMode.value) {
                ChartMode.DURATION -> it.timeElapsed / 60f
                ChartMode.VOLUME -> if (index < volume.lastIndex) volume[index] else 0f
                ChartMode.REPS -> if (index < reps.lastIndex) reps[index].toFloat() else 0f
            }
        }.ifEmpty { listOf(0f) }
    }

    fun getXAxisDataChart(): List<String> {
        return pastWorkouts.map { it ->
            it.completed.format(shortFormatter).toString()
        }
    }

    fun updateChartMode(value: ChartMode) {
        chartMode.value = value
    }

    fun getChartMode(): ChartMode {
        return chartMode.value
    }

    /**
     * All the completed workouts linked to this routine by [Workout.workoutId]
     */
    private var pastWorkouts = mutableStateListOf<Workout>()
    private var volume = mutableStateListOf<Float>()
    private var reps = mutableStateListOf<Int>()

    fun getAllPastWorkoutsFromDB() {
        viewModelScope.launch(Dispatchers.IO) {
            if (pastWorkouts.isEmpty()) {
                pastWorkouts.addAll(
                    workoutDao.getAllPastWorkouts(workout.value.workoutId)
                )
                volume.clear()
                reps.clear()
                pastWorkouts.forEach { workout ->
                    // For each exercise, fetch its sets. Then flatten the nested list of sets into one list.
                    val allSets = workoutDao.getExercisesFromWorkout(workout.id)
                        .flatMap { workoutDao.getSetsFromExercise(it.id) }
                    // Calculate workoutVolume and workoutReps only from the completed sets.
                    val (workoutVolume, workoutReps) = allSets.asSequence()
                        .filter { it.completed }
                        .fold(0f to 0) { (volumeAcc, repsAcc), set ->
                            (volumeAcc + set.weight * set.reps) to (repsAcc + set.reps)
                        }
                    volume.add(workoutVolume)
                    reps.add(workoutReps)
                }
            }
        }
    }

    fun deleteWorkout() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.deleteWorkout(workout.value)
        }
    }

    fun detachWorkoutFromRoutine() {
        workout.value = workout.value.copy(
            workoutId = System.currentTimeMillis()
        )

        routine.value = Workout()

        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.updateWorkout(workout.value)
        }
    }
}