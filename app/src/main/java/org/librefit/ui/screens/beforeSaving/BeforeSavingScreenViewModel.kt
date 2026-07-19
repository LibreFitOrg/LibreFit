/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.screens.beforeSaving

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.entity.Workout
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.di.qualifiers.IoDispatcher
import org.librefit.enums.SetMode
import org.librefit.enums.WarmupMode
import org.librefit.enums.WorkoutState
import org.librefit.helpers.DataHelper
import org.librefit.nav.Route
import org.librefit.services.WorkoutServiceManager
import org.librefit.ui.models.UiExerciseItem
import org.librefit.ui.models.UiWarmupItem
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.models.UiWorkoutItem
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.models.mappers.toUi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BeforeSavingScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val workoutServiceManager: WorkoutServiceManager,
    private val dataHelper: DataHelper,
    userPreferencesRepository: UserPreferencesRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    val useScrollWheelForInput = userPreferencesRepository.useScrollWheelForInput

    val dismissScrollWheelInputAutomatically =
        userPreferencesRepository.dismissScrollWheelInputAutomatically

    private val runningWorkoutId =
        savedStateHandle.toRoute<Route.BeforeSavingScreen>().runningWorkoutId


    private val _workoutItems = MutableStateFlow<List<UiWorkoutItem>>(emptyList())
    val workoutItems = _workoutItems.asStateFlow()

    private val _volume = MutableStateFlow("0.00")
    val volume = _volume.asStateFlow()

    private val _workout = MutableStateFlow(UiWorkout())
    val workout = _workout.asStateFlow()

    init {
        viewModelScope.launch {
            val pendingWorkout = workoutRepository.getPendingWorkout()
            val runningWorkoutWithExercises =
                pendingWorkout ?: workoutRepository.getWorkoutWithExercisesAndSets(runningWorkoutId)

            val e = runningWorkoutWithExercises.exercisesWithSets
            val w = runningWorkoutWithExercises.warmupsWithSets

            _workoutItems.update {
                (runningWorkoutWithExercises.warmupsWithSets.map {
                    UiWarmupItem(
                        warmup = it
                    )
                } + runningWorkoutWithExercises.exercisesWithSets.map { UiExerciseItem(exercise = it) }).sortedBy { it.position }
            }

            _workout.update {
                runningWorkoutWithExercises.workout.copy(completed = LocalDateTime.now())
            }

            val volume = dataHelper.fetchVolumeFromWorkout(
                WorkoutWithExercisesAndSets(
                    Workout(),
                    e.map { it.toEntity() },
                    w.map { it.toEntity() })
            )

            _volume.update {
                String.format(Locale.getDefault(), "%.2f", volume)
            }
        }
    }


    fun setTimeElapsed(timeElapsed: Int) {
        _workout.update { currentWorkout ->
            currentWorkout.copy(timeElapsed = timeElapsed)
        }
    }

    fun updateWorkoutTitle(string: String) {
        _workout.update { currentWorkout ->
            currentWorkout.copy(title = string)
        }
    }

    fun isTitleEmpty(): Boolean {
        return workout.value.title.isEmpty()
    }

    fun isTitleTooLong(): Boolean {
        return workout.value.title.length >= 30
    }

    fun updateWorkoutNotes(newNotes: String) {
        _workout.update { currentWorkout ->
            currentWorkout.copy(notes = newNotes)
        }
    }

    fun detachWorkoutFromRoutine() {
        _workout.update { currentWorkout ->
            currentWorkout.copy(routineId = Random.nextLong())
        }
        _routine.update {
            UiWorkout()
        }
    }

    fun updateCompletedDate(selectedDateMillis: Long?) {
        if (selectedDateMillis != null) {
            _workout.update { currentWorkout ->
                currentWorkout.copy(
                    completed = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(selectedDateMillis),
                        ZoneOffset.UTC
                    )
                )
            }
        }
    }


    private val _routine = MutableStateFlow(UiWorkout())
    val routine = _routine.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _routine.update {
                    workoutRepository.getRoutineFromRoutineID(workout.value.routineId).toUi()
                }
                delay(200)
            }
        }
    }


    fun saveExercisesWithWorkout() {
        workoutServiceManager.stopService()

        viewModelScope.launch(ioDispatcher) {
            workoutRepository.setPendingWorkout(null)
            workoutRepository.addWorkoutWithExercisesAndSets(
                WorkoutWithExercisesAndSets(
                    workout = workout.value.copy(
                        id = runningWorkoutId,
                        state = WorkoutState.COMPLETED
                    ).toEntity(),
                    exercisesWithSets = workoutItems.value.filterIsInstance<UiExerciseItem>()
                        .map { exercise ->
                            exercise.exercise.toEntity().copy(
                                sets = exercise.exercise.toEntity().sets.map {
                                    // This keeps only relevant data on the actual type of set
                                    when (exercise.exercise.exercise.setMode) {
                                        SetMode.DURATION -> it.copy(reps = 0, load = 0.0)
                                        SetMode.BODYWEIGHT -> it.copy(elapsedTime = 0, load = 0.0)
                                        SetMode.BODYWEIGHT_WITH_LOAD -> it.copy(elapsedTime = 0)
                                        SetMode.LOAD -> it.copy(elapsedTime = 0)
                                    }
                                }
                            )
                        },
                    warmupsWithSets = workoutItems.value.filterIsInstance<UiWarmupItem>()
                        .map { warmup ->
                            warmup.warmup.toEntity().copy(
                                sets = warmup.warmup.toEntity().sets.map {
                                    when (warmup.warmup.warmup.warmupMode) {
                                        WarmupMode.DEFAULT -> it.copy(elapsedTime = 0)
                                    }
                                }
                            )
                        }
                )
            )
        }
    }
}