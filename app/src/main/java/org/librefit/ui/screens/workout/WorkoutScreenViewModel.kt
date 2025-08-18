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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.SetMode
import org.librefit.enums.WorkoutState
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.services.WorkoutService
import org.librefit.services.WorkoutServiceManager
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.models.mappers.toUi
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class WorkoutScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @param:ApplicationContext private val context: Context,
    userPreferences: UserPreferencesRepository,
    private val workoutServiceManager: WorkoutServiceManager,
    workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _idSetWithRunningChronometer = MutableStateFlow<Long?>(null)
    val idSetWithRunningChronometer = _idSetWithRunningChronometer.asStateFlow()

    fun updateIdSetWithRunningChronometer(setId: Long?) {
        _idSetWithRunningChronometer.update { setId }
    }

    private suspend fun startSetChronometer(set: UiSet) {
        val startTime = System.currentTimeMillis()
        val initialElapsedTime = set.elapsedTime

        // The loop is infinite, but the coroutine will be stopped when its Job is cancelled
        while (true) {
            val currentTime = System.currentTimeMillis()
            val newElapsedTime = initialElapsedTime + ((currentTime - startTime) / 1000)

            updateSetTime(newElapsedTime.toInt(), set.id)

            delay(1000)
        }
    }



    companion object {
        private const val WORKOUT_ID_KEY = "workoutId"
    }

    private val workoutId = savedStateHandle.get<Long>(WORKOUT_ID_KEY)
        ?: error("WORKOUT_ID_KEY does not match `Route.WorkoutScreen` parameter")


    private val _workout = MutableStateFlow(UiWorkout())
    val workout = _workout.asStateFlow()

    private val _exercises = MutableStateFlow<List<UiExerciseWithSets>>(emptyList())
    val exercises = _exercises.asStateFlow()

    // A Job to hold the running set's chronometer coroutine
    private var chronometerJob: Job? = null

    init {
        viewModelScope.launch {
            if (workoutId != 0L) {
                val workoutWithExercisesAndSets =
                    workoutRepository.getWorkoutWithExercisesAndSets(workoutId)

                _workout.update {
                    workoutWithExercisesAndSets.workout.copy(
                        id = 0,
                        state = WorkoutState.RUNNING
                    )
                }

                _exercises.update {
                    workoutWithExercisesAndSets.exercisesWithSets
                }
            }
        }

        viewModelScope.launch {
            idSetWithRunningChronometer.collect { runningSetId ->
                // Whenever the ID changes, cancel any existing timer.
                chronometerJob?.cancel()

                // If the new ID is a valid set ID (not 0), start a new timer.
                if (runningSetId != null) {
                    val set = exercises.value.flatMap { it.sets }.find { it.id == runningSetId }
                    if (set != null) {
                        // Launch a new coroutine for the timer and assign it to the Job.
                        chronometerJob = launch { startSetChronometer(set) }
                    }
                }
            }
        }
    }

    fun addExerciseWithSets(exerciseDC: ExerciseDC) {
        val newExercise = UiExerciseWithSets(
            exercise = UiExercise(
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
            exerciseDC = exerciseDC.toUi()
        )

        _exercises.update { exercises ->
            exercises + newExercise
        }
    }

    fun addSetToExercise(exerciseId: Long) {
        _exercises.update { exercises ->
            exercises.map { exercise ->
                if (exercise.exercise.id == exerciseId) {
                    val newSet = exercise.sets
                        .lastOrNull()?.copy(id = Random.Default.nextLong())
                        ?: UiSet()

                    val newSets = exercise.sets.toMutableList() + newSet
                    exercise.copy(sets = newSets.toImmutableList())
                } else exercise
            }
        }
    }

    fun updateSetTime(time: Int, id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { exercise ->
                if (exercise.sets.any { it.id == id }) {
                    exercise.copy(
                        sets = exercise.sets.map {
                            if (it.id == id) it.copy(elapsedTime = time) else it
                        }.toImmutableList()
                    )
                } else exercise
            }
        }
    }

    fun updateSetReps(reps: Int, id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { exercise ->
                if (exercise.sets.any { it.id == id }) {
                    exercise.copy(
                        sets = exercise.sets.map {
                            if (it.id == id) it.copy(reps = reps) else it
                        }.toImmutableList()
                    )
                } else exercise
            }
        }
    }

    fun updateSetLoad(load: Float, id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { exercise ->
                if (exercise.sets.any { it.id == id }) {
                    exercise.copy(
                        sets = exercise.sets.map {
                            if (it.id == id) it.copy(load = load) else it
                        }.toImmutableList()
                    )
                } else exercise
            }
        }
    }

    fun updateSetCompleted(completed: Boolean, id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { exercise ->
                if (exercise.sets.any { it.id == id }) {
                    exercise.copy(
                        sets = exercise.sets.map {
                            if (it.id == id) it.copy(completed = completed) else it
                        }.toImmutableList()
                    )
                } else exercise
            }
        }
        val exerciseWithSets = exercises.value.find { e -> e.sets.any { it.id == id } }!!
        if (completed && exerciseWithSets.exercise.restTime != 0) {
            startRestTimer(exerciseWithSets.exercise.restTime + 1)
        }
    }

    fun deleteSet(id: Long) {
        // If there's the match, then the set has a running chronometer and it has to be stopped by assign 0
        if (idSetWithRunningChronometer.value == id) {
            _idSetWithRunningChronometer.update { 0L }
        }

        _exercises.update { currentExercises ->
            currentExercises.map { exercise ->
                if (exercise.sets.any { it.id == id }) {
                    exercise.copy(
                        sets = exercise.sets.filter { it.id != id }.toImmutableList()
                    )
                } else exercise
            }
        }
    }

    fun updateExerciseNotes(notes: String, id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { eWs ->
                if (eWs.exercise.id == id) eWs.copy(exercise = eWs.exercise.copy(notes = notes)) else eWs
            }
        }
    }

    fun updateExerciseRestTime(restTime: Int, id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { eWs ->
                if (eWs.exercise.id == id) eWs.copy(exercise = eWs.exercise.copy(restTime = restTime)) else eWs
            }
        }
    }

    fun updateExerciseSetMode(setMode: SetMode, id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { eWs ->
                if (eWs.exercise.id == id) eWs.copy(exercise = eWs.exercise.copy(setMode = setMode)) else eWs
            }
        }
    }

    fun deleteExercise(exerciseId: Long) {
        val exerciseWithSets = exercises.value.find { it.exercise.id == exerciseId }!!
        // If there's the match, then the set has a running chronometer and it has to be stopped by assign 0
        if (exerciseWithSets.sets.any { it.id == idSetWithRunningChronometer.value }) {
            _idSetWithRunningChronometer.update { 0L }
        }
        _exercises.update { currentExercises ->
            currentExercises.filter { it.exercise.id != exerciseId }
        }
    }

    val progress: StateFlow<Float> = exercises
        .map { list ->
            val totalSets = list.sumOf { it.sets.size }
            if (totalSets == 0) return@map 0f

            val completedSets = list.sumOf { it.sets.count { s -> s.completed } }
            completedSets.toFloat() / totalSets
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )


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
                _restTime.update { newRestTime.coerceAtLeast(0) }

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
