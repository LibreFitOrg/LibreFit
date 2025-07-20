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

package org.librefit.ui.screens.workout

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.data.DataStoreManager
import org.librefit.data.ExerciseDC
import org.librefit.db.entity.Exercise
import org.librefit.db.entity.Set
import org.librefit.db.entity.Workout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.SetMode
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.services.WorkoutService
import org.librefit.services.WorkoutServiceManager
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class WorkoutScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @param:ApplicationContext private val context: Context,
    userPreferences: DataStoreManager,
    private val workoutServiceManager: WorkoutServiceManager,
    private val workoutRepository: WorkoutRepository,
    private val exercisesList: List<ExerciseDC>
) : ViewModel() {

    private val _idSetWithRunningChronometer = MutableStateFlow(0L)
    val idSetWithRunningChronometer = _idSetWithRunningChronometer.asStateFlow()

    fun updateIdSetWithRunningChronometer(setId: Long) {
        _idSetWithRunningChronometer.value = setId
    }




    companion object {
        private const val WORKOUT_ID_KEY = "workoutId"
    }

    private val workoutId = savedStateHandle.get<Long>(WORKOUT_ID_KEY)
        ?: throw IllegalArgumentException("Invalid WORKOUT_ID_KEY")


    private val _workout = MutableStateFlow(Workout())
    val workout = _workout.asStateFlow()

    private val _exercises = MutableStateFlow<List<ExerciseWithSets>>(emptyList())
    val exercises = _exercises.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (workoutId != 0L) {
                val workoutWithExercisesAndSets =
                    workoutRepository.getWorkoutWithExercisesAndSets(workoutId)

                _workout.value = workoutWithExercisesAndSets.workout.copy(
                    id = 0,
                    routine = false
                )

                _exercises.value = workoutWithExercisesAndSets.exercisesWithSets.map {
                    it.apply {
                        val exDC = exercisesList.find { e -> e.id == it.exercise.exerciseId }!!
                        it.exercise = it.exercise.copy(exerciseId = exDC.id)
                        it.exerciseDC = exDC
                    }
                }
            }
        }
    }

    fun addExerciseWithSets(exerciseDC: ExerciseDC) {
        _exercises.value = exercises.value +
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

    }

    fun addSetToExercise(index: Int) {
        val exerciseWithSets = exercises.value[index]
        val newSet = exerciseWithSets.sets
            .lastOrNull()?.copy(id = Random.Default.nextLong())
            ?: Set()

        _exercises.value = exercises.value.mapIndexed { i, exercise ->
            if (index == i) {
                exercise.copy(sets = exercise.sets + newSet)
            } else {
                exercise
            }
        }
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
     *  - 0: [Set.load]
     *  - 1: [Set.reps]
     *  - 2: [Set.elapsedTime]
     *  - 3: [Set.completed] (where a [value] of 1 indicates 'true')
     *
     * The method will update the specified attribute of the [Set] if it matches the provided [set] ID.
     * If the [mode] is not recognized, the original [set] will remain unchanged.
     */
    fun updateSet(index: Int, set: Set, value: Float, mode: Int) {
        val exerciseWithSets = exercises.value[index]

        _exercises.value = exercises.value.mapIndexed { i, exercise ->
            if (index == i) {
                exercise.copy(
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
            } else {
                exercise
            }
        }
        if (mode == 3 && value == 1f && exerciseWithSets.exercise.restTime != 0) {
            startRestTimer(exerciseWithSets.exercise.restTime + 1)
        }
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
        val exerciseWithSets = exercises.value[index]

        _exercises.value = exercises.value.mapIndexed { i, exercise ->
            if (index == i) {
                exercise.copy(
                    sets = exerciseWithSets.sets.filter { it.id != set.id }
                )
            } else {
                exercise
            }
        }

        // If there's the match, then set has a running chronometer which has to stopped by assign 0
        if (idSetWithRunningChronometer.value == set.id) {
            _idSetWithRunningChronometer.value = 0L
        }
    }

    /**
     * Updates an instance of [ExerciseWithSets] by assigning a [value] to a specified attribute based on the provided [mode].
     *
     * @param index The index of the [ExerciseWithSets] instance in the [exercises] list that needs to be updated.
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
        val exerciseWithSets = exercises.value[index]
        val newExerciseWithSets = when (mode) {
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

        _exercises.value = exercises.value.mapIndexed { i, e ->
            if (i == index) newExerciseWithSets else e
        }
    }

    fun deleteExercise(index: Int) {
        _exercises.value = exercises.value.filterIndexed { i, e -> i != index }
    }


    fun getProgress(): Float {
        val totalSets = if (exercises.value.sumOf { it.sets.size } != 0)
            exercises.value.sumOf { it.sets.size } else 1

        return exercises.value.sumOf { ex -> ex.sets.filter { it.completed }.size }
            .toFloat() / totalSets
    }


    val timeElapsed = WorkoutService.timeElapsed
    val isChronometerPaused = WorkoutService.isChronometerPaused

    private val _restTime = MutableStateFlow(0)
    val restTime = _restTime.asStateFlow()


    private var initialRestTime = 1
    private var isFocused = true


    init {
        startChronometer()
        observeChanges()
    }


    private fun observeChanges() {
        viewModelScope.launch(Dispatchers.Main) {
            WorkoutService.restTime.collect { newRestTime ->
                _restTime.value = newRestTime.coerceAtLeast(0)

                // When timer is over and screen is visible, it plays alert sound
                if (initialRestTime != 1 && restTime.value == 0 && isFocused) {
                    val mediaPlayer = MediaPlayer.create(context, R.raw.alert_notification)
                    mediaPlayer.setOnCompletionListener {
                        it.release()
                    }
                    mediaPlayer.start()
                    initialRestTime = 1
                }
            }
        }
    }

    fun startChronometer() {
        workoutServiceManager.startChronometer()
    }

    fun pauseChronometer() {
        workoutServiceManager.pauseChronometer()
    }

    /**
     * It ensures that [WorkoutService] sends an alert notification only when the app is not focused so
     * so only when [isFocused] is `false`
     */
    fun updateFocus(isFocused: Boolean) {
        this.isFocused = isFocused
        workoutServiceManager.updateFocus(isFocused)
    }


    fun startRestTimer(initialValue: Int) {
        initialRestTime = initialValue
        workoutServiceManager.startRestTimer(initialValue)
    }

    fun modifyRestTime(addTenSeconds: Boolean) {
        workoutServiceManager.modifyRestTime(addTenSeconds)
    }

    fun getRestTimeProgress(): Float {
        return restTime.value.toFloat() / initialRestTime
    }

    fun stopWorkoutService() {
        workoutServiceManager.stopService()
    }


    val keepScreenOn = userPreferences.workoutScreenOn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
}
