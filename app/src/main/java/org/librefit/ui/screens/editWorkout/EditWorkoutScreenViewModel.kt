/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.screens.editWorkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.di.qualifiers.IoDispatcher
import org.librefit.enums.SetMode
import org.librefit.enums.WarmupMode
import org.librefit.enums.WorkoutState
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.nav.Route
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseItem
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.UiWarmup
import org.librefit.ui.models.UiWarmupItem
import org.librefit.ui.models.UiWarmupWithSets
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.models.UiWorkoutItem
import org.librefit.ui.models.UiWorkoutWithExercisesAndSets
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.models.mappers.toUi
import org.librefit.ui.models.moveExercise
import org.librefit.ui.models.recalcWarmupSets
import org.librefit.ui.models.withNormalizedExercisePositions
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class EditWorkoutScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val useScrollWheelForInput = userPreferencesRepository.useScrollWheelForInput

    val dismissScrollWheelInputAutomatically =
        userPreferencesRepository.dismissScrollWheelInputAutomatically

    private val workoutId = savedStateHandle.toRoute<Route.EditWorkoutScreen>().workoutId


    private val _isRoutine = MutableStateFlow(false)
    private val isRoutine = _isRoutine.asStateFlow()

    private val _workout = MutableStateFlow(UiWorkout())
    val workout = _workout.asStateFlow()

    private val _routine = MutableStateFlow(UiWorkout())
    val routine = _routine.asStateFlow()

    private val _workoutItems = MutableStateFlow<List<UiWorkoutItem>>(emptyList())
    val workoutItems = _workoutItems.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            if (workoutId != 0L) {
                val workoutWithExercisesAndSets =
                    workoutRepository.getWorkoutWithExercisesAndSets(workoutId)

                val workoutInDb = workoutWithExercisesAndSets.workout

                _isRoutine.update {
                    workoutInDb.state == WorkoutState.ROUTINE
                }

                _workout.update {
                    workoutInDb.copy(state = WorkoutState.COMPLETED)
                }

                _workoutItems.update {
                    workoutWithExercisesAndSets.workoutItems
                }
            } else {
                _isRoutine.update {
                    true
                }
            }

            _routine.update {
                if (isRoutine.value) {
                    workout.value
                } else {
                    workoutRepository.getRoutineFromRoutineID(workout.value.routineId).toUi()
                }
            }
        }
    }

    /**
     * Auto-syncs UI changes to the repository. This guarantees the repository is always the 
     * single source of truth, avoiding race conditions during screen transitions.
     */
    private fun syncToRepository() {
        val state = if (isRoutine.value) WorkoutState.ROUTINE else WorkoutState.COMPLETED
        val workoutWithExercises = UiWorkoutWithExercisesAndSets(
            workout = workout.value.copy(state = state),
            exercisesWithSets = _workoutItems.value.filterIsInstance<UiExerciseItem>()
                .map { it.exercise }.toImmutableList(),
            warmupsWithSets = _workoutItems.value.filterIsInstance<UiWarmupItem>().map { it.warmup }
                .toImmutableList(),
        )
        viewModelScope.launch(ioDispatcher) {
            workoutRepository.setPendingWorkout(workoutWithExercises)
        }
    }

    fun addExerciseWithSets(exerciseDC: ExerciseDC) {
        val newExercise = UiExerciseItem(
            UiExerciseWithSets(
                exercise = UiExercise(
                    idExerciseDC = exerciseDC.id,
                    setMode = when (exerciseDC.category) {
                        Category.STRETCHING, Category.CARDIO -> SetMode.DURATION
                        else -> when (exerciseDC.equipment) {
                            Equipment.BODY_ONLY, Equipment.FOAM_ROLL, Equipment.EXERCISE_BALL,
                            Equipment.MEDICINE_BALL, Equipment.BANDS -> SetMode.BODYWEIGHT

                            else -> if (exerciseDC.name.contains("Weighted", true))
                                SetMode.BODYWEIGHT_WITH_LOAD else SetMode.LOAD
                        }
                    }
                ),
                exerciseDC = exerciseDC.toUi()
            )
        )

        _workoutItems.update { workoutItems ->
            (workoutItems + newExercise).withNormalizedExercisePositions()
        }
        syncToRepository()
    }

    fun addSetToExercise(exerciseId: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { workoutItem ->
                if (workoutItem.id == exerciseId) {
                    val newSet = workoutItem.sets
                        .lastOrNull()?.copy(id = Random.nextLong())
                        ?: UiSet()

                    val newSets = workoutItem.sets.toMutableList() + newSet
                    workoutItem.updateSets(newSets.toImmutableList())
                } else workoutItem
            }
        }
        syncToRepository()
    }

    fun updateSetTime(time: Int, id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { workoutItem ->
                if (workoutItem.sets.any { it.id == id }) {
                    workoutItem.updateSets(
                        workoutItem.sets.map {
                            if (it.id == id) it.copy(elapsedTime = time) else it
                        }.toImmutableList()
                    )
                } else workoutItem
            }
        }
        syncToRepository()
    }

    fun updateSetReps(reps: Int, id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { workoutItem ->
                if (workoutItem.sets.any { it.id == id }) {
                    workoutItem.updateSets(
                        workoutItem.sets.map {
                            if (it.id == id) it.copy(reps = reps) else it
                        }.toImmutableList()
                    )
                } else workoutItem
            }
        }
        syncToRepository()
    }

    fun updateSetLoad(load: Double, id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { workoutItem ->
                if (workoutItem.sets.any { it.id == id }) {
                    workoutItem.updateSets(
                        workoutItem.sets.map {
                            if (it.id == id) it.copy(load = load) else it
                        }.toImmutableList()
                    )
                } else workoutItem
            }
        }
        syncToRepository()
    }

    fun updateSetCompleted(completed: Boolean, id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { workoutItem ->
                if (workoutItem.sets.any { it.id == id }) {
                    workoutItem.updateSets(
                        workoutItem.sets.map {
                            if (it.id == id) it.copy(completed = completed) else it
                        }.toImmutableList()
                    )
                } else workoutItem
            }
        }
        syncToRepository()
    }

    fun deleteSet(id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { workoutItem ->
                if (workoutItem.sets.any { it.id == id }) {
                    workoutItem.updateSets(
                        workoutItem.sets.filter { it.id != id }.toImmutableList()
                    )
                } else workoutItem
            }
        }
        syncToRepository()
    }

    fun updateExerciseNotes(notes: String, id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { workoutItem ->
                if (workoutItem.id == id) when (workoutItem) {
                    is UiExerciseItem -> {
                        workoutItem.copy(
                            exercise = workoutItem.exercise.copy(
                                exercise = workoutItem.exercise.exercise.copy(
                                    notes = notes
                                )
                            )
                        )
                    }

                    is UiWarmupItem -> {
                        workoutItem.copy(
                            warmup = workoutItem.warmup.copy(
                                warmup = workoutItem.warmup.warmup.copy(
                                    notes = notes
                                )
                            )
                        )
                    }
                } else workoutItem
            }
        }
        syncToRepository()
    }

    fun updateExerciseRestTime(restTime: Int, id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { workoutItem ->
                if (workoutItem.id == id) when (workoutItem) {
                    is UiExerciseItem -> {
                        workoutItem.copy(
                            exercise = workoutItem.exercise.copy(
                                exercise = workoutItem.exercise.exercise.copy(
                                    restTime = restTime
                                )
                            )
                        )
                    }

                    is UiWarmupItem -> {
                        workoutItem.copy(
                            warmup = workoutItem.warmup.copy(
                                warmup = workoutItem.warmup.warmup.copy(
                                    restTime = restTime
                                )
                            )
                        )
                    }
                } else workoutItem
            }
        }
        syncToRepository()
    }

    fun updateExerciseSetMode(setMode: SetMode, id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { eWs ->
                if (eWs is UiExerciseItem && eWs.id == id) eWs.copy(
                    exercise = eWs.exercise.copy(
                        exercise = eWs.exercise.exercise.copy(setMode = setMode)
                    )
                ) else eWs
            }
        }
        syncToRepository()
    }

    fun updateWarmupSetMode(warmupMode: WarmupMode, id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { wWs ->
                if (wWs is UiWarmupItem && wWs.id == id) wWs.copy(
                    warmup = wWs.warmup.copy(
                        warmup = wWs.warmup.warmup.copy(
                            warmupMode = warmupMode
                        ), sets = recalcWarmupSets(wWs.warmup.warmup.target, warmupMode)
                    )
                ) else wWs
            }
        }
        syncToRepository()
    }

    fun updateWarmupTarget(target: Double, id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { wWs ->
                if (wWs is UiWarmupItem && wWs.id == id) wWs.copy(
                    warmup = wWs.warmup.copy(
                        warmup = wWs.warmup.warmup.copy(
                            target = target
                        ), sets = recalcWarmupSets(target, wWs.warmup.warmup.warmupMode)
                    )
                ) else wWs
            }
        }
        syncToRepository()
    }

    fun addWarmup(id: Long, target: Double?) {
        val exercise = workoutItems.value.find { it.id == id }!!

        val sets = if (target !== null) {
            recalcWarmupSets(target, WarmupMode.DEFAULT)
        } else {
            emptyList<UiSet>().toImmutableList()
        }

        val newWarmup = UiWarmupItem(
            UiWarmupWithSets(
                warmup = UiWarmup(
                    target = target ?: 0.0
                ),
                sets = sets
            )
        )

        _workoutItems.update { currentWorkoutItems ->
            (currentWorkoutItems + newWarmup).withNormalizedExercisePositions()
        }

        moveExercise(_workoutItems.value.size - 1, exercise.position)
    }

    fun deleteExercise(exerciseId: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems
                .filter { it.id != exerciseId }
                .withNormalizedExercisePositions()
        }
        syncToRepository()
    }

    fun moveExercise(fromIndex: Int, toIndex: Int) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.moveExercise(fromIndex = fromIndex, toIndex = toIndex)
        }
        syncToRepository()
    }


    fun updateTitle(string: String) {
        _workout.update { it.copy(title = string) }
        syncToRepository()
    }

    fun updateNotes(string: String) {
        _workout.update { it.copy(notes = string) }
        syncToRepository()
    }

    fun isTitleEmpty(): Boolean {
        return workout.value.title.isEmpty()
    }

    fun isTitleTooLong(): Boolean {
        return workout.value.title.length >= 30
    }


    fun saveWorkoutWithExercisesInDB() {
        viewModelScope.launch(ioDispatcher) {
            val state = if (isRoutine.value) WorkoutState.ROUTINE else WorkoutState.COMPLETED
            workoutRepository.addWorkoutWithExercisesAndSets(
                WorkoutWithExercisesAndSets(
                    workout = workout.value.copy(state = state).toEntity(),
                    exercisesWithSets = _workoutItems.value.filterIsInstance<UiExerciseItem>()
                        .map { it.exercise.toEntity() },
                    warmupsWithSets = _workoutItems.value.filterIsInstance<UiWarmupItem>()
                        .map { it.warmup.toEntity() }
                )
            )
        }
    }


    /**
     * Returns `null` when a new routine is created, `true` when a routine is edited and `false` when
     * a past workout is edited
     */
    fun getTypeOfEdit(): Boolean? {
        return if (workout.value.id == 0L) null else isRoutine.value
    }
}
