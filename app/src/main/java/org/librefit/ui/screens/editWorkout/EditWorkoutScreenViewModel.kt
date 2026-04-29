/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
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
import org.librefit.db.repository.WorkoutRepository
import org.librefit.di.qualifiers.IoDispatcher
import org.librefit.enums.SetMode
import org.librefit.enums.WorkoutState
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.nav.Route
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.models.UiWorkoutWithExercisesAndSets
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.models.mappers.toUi
import org.librefit.ui.models.moveExercise
import org.librefit.ui.models.withNormalizedExercisePositions
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class EditWorkoutScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val workoutId = savedStateHandle.toRoute<Route.EditWorkoutScreen>().workoutId


    private val _isRoutine = MutableStateFlow(false)
    private val isRoutine = _isRoutine.asStateFlow()

    private val _workout = MutableStateFlow(UiWorkout())
    val workout = _workout.asStateFlow()

    private val _routine = MutableStateFlow(UiWorkout())
    val routine = _routine.asStateFlow()

    private val _exercises = MutableStateFlow<List<UiExerciseWithSets>>(emptyList())
    val exercises = _exercises.asStateFlow()

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

                _exercises.update {
                    workoutWithExercisesAndSets.exercisesWithSets
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
            exercisesWithSets = exercises.value.withNormalizedExercisePositions().toImmutableList()
        )
        viewModelScope.launch(ioDispatcher) {
            workoutRepository.setPendingWorkout(workoutWithExercises)
        }
    }

    fun addExerciseWithSets(exerciseDC: ExerciseDC) {
        val newExercise = UiExerciseWithSets(
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

        _exercises.update { exercises ->
            (exercises + newExercise).withNormalizedExercisePositions()
        }
        syncToRepository()
    }

    fun addSetToExercise(exerciseId: Long) {
        _exercises.update { exercises ->
            exercises.map { exercise ->
                if (exercise.exercise.id == exerciseId) {
                    val newSet = exercise.sets
                        .lastOrNull()?.copy(id = Random.nextLong())
                        ?: UiSet()

                    val newSets = exercise.sets.toMutableList() + newSet
                    exercise.copy(sets = newSets.toImmutableList())
                } else exercise
            }
        }
        syncToRepository()
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
        syncToRepository()
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
        syncToRepository()
    }

    fun updateSetLoad(load: Double, id: Long) {
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
        syncToRepository()
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
        syncToRepository()
    }

    fun deleteSet(id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { exercise ->
                if (exercise.sets.any { it.id == id }) {
                    exercise.copy(
                        sets = exercise.sets.filter { it.id != id }.toImmutableList()
                    )
                } else exercise
            }
        }
        syncToRepository()
    }

    fun updateExerciseNotes(notes: String, id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { eWs ->
                if (eWs.exercise.id == id) eWs.copy(exercise = eWs.exercise.copy(notes = notes)) else eWs
            }
        }
        syncToRepository()
    }

    fun updateExerciseRestTime(restTime: Int, id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { eWs ->
                if (eWs.exercise.id == id) eWs.copy(exercise = eWs.exercise.copy(restTime = restTime)) else eWs
            }
        }
        syncToRepository()
    }

    fun updateExerciseSetMode(setMode: SetMode, id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { eWs ->
                if (eWs.exercise.id == id) eWs.copy(exercise = eWs.exercise.copy(setMode = setMode)) else eWs
            }
        }
        syncToRepository()
    }

    fun deleteExercise(exerciseId: Long) {
        _exercises.update { currentExercises ->
            currentExercises
                .filter { it.exercise.id != exerciseId }
                .withNormalizedExercisePositions()
        }
        syncToRepository()
    }

    fun moveExercise(fromIndex: Int, toIndex: Int) {
        _exercises.update { currentExercises ->
            currentExercises.moveExercise(fromIndex = fromIndex, toIndex = toIndex)
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
                    exercisesWithSets = exercises.value
                        .withNormalizedExercisePositions()
                        .map { it.toEntity() }
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
