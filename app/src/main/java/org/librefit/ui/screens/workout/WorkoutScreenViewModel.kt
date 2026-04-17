/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.workout

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.DatasetRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.PreviousPerformanceSet
import org.librefit.enums.SetMode
import org.librefit.enums.WorkoutState
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.nav.Route
import org.librefit.services.WorkoutService
import org.librefit.services.WorkoutServiceManager
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.models.mappers.toUi
import javax.inject.Inject
import kotlin.random.Random

@OptIn(FlowPreview::class)
@HiltViewModel
class WorkoutScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @param:ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesRepository,
    private val workoutServiceManager: WorkoutServiceManager,
    private val workoutRepository: WorkoutRepository,
    private val datasetRepository: DatasetRepository
) : ViewModel() {
    private val _idsOfSetsWithStopwatchNotStartedAtLeastOnce =
        MutableStateFlow<Set<Long>>(emptySet())
    val idsOfSetsWithStopwatchNotStartedAtLeastOnce =
        _idsOfSetsWithStopwatchNotStartedAtLeastOnce.asStateFlow()

    private val _idSetWithRunningStopwatch = MutableStateFlow<Long?>(null)
    val idSetWithRunningStopwatch = _idSetWithRunningStopwatch.asStateFlow()

    fun updateIdSetWithRunningStopwatch(setId: Long?) {
        _idSetWithRunningStopwatch.update { setId }
    }

    private suspend fun startSetStopwatch(set: UiSet) {
        val startTime = System.currentTimeMillis()
        val id = set.id
        val initialElapsedTime = if (id in idsOfSetsWithStopwatchNotStartedAtLeastOnce.value) {
            _idsOfSetsWithStopwatchNotStartedAtLeastOnce.update { set ->
                set.filter { it != id }.toSet()
            }
            0
        } else {
            set.elapsedTime
        }

        // The loop is infinite, but the coroutine will be stopped when its Job is cancelled
        while (true) {
            val currentTime = System.currentTimeMillis()
            val newElapsedTime = initialElapsedTime + ((currentTime - startTime) / 1000)

            updateSetTime(newElapsedTime.toInt(), set.id)

            delay(1000)
        }
    }

    private val workoutId = savedStateHandle.toRoute<Route.WorkoutScreen>().workoutId


    private val _workout = MutableStateFlow(UiWorkout())
    val workout = _workout.asStateFlow()

    private val _exercises = MutableStateFlow<List<UiExerciseWithSets>>(emptyList())
    val exercises = _exercises.asStateFlow()

    val previousPerformances: StateFlow<List<List<PreviousPerformanceSet>>> =
        combine(exercises, workout) { list, w ->
            list.map { eWs ->
                val list = workoutRepository.getCompletedWorkoutsWithExercisesWithIdExerciseDC(
                    idExerciseDC = eWs.exerciseDC.id
                ).firstOrNull()

                // It tries to find in the completed workouts the previous performance of
                // the same exercise and in the same routine. If there isn't a linked routine, it takes the
                // latest workout with that exercise
                val previousWorkout =
                    list?.find { it.workout.routineId == w.routineId } ?: list?.firstOrNull()
                val previousEWS =
                    previousWorkout?.exercisesWithSets?.find { it.exerciseDC.id == eWs.exerciseDC.id }

                eWs.sets.mapIndexed { index, _ ->
                    val previousSet = previousEWS?.sets?.getOrNull(index)
                    val reps = previousSet?.reps ?: 0
                    val load = previousSet?.load ?: 0.0
                    val time = previousSet?.elapsedTime ?: 0


                    when (eWs.exercise.setMode) {
                        SetMode.LOAD -> PreviousPerformanceSet(load = load, reps = reps)
                        SetMode.BODYWEIGHT -> PreviousPerformanceSet(reps = reps)
                        SetMode.BODYWEIGHT_WITH_LOAD -> PreviousPerformanceSet(
                            load = load,
                            reps = reps
                        )

                        SetMode.DURATION -> PreviousPerformanceSet(time = time)
                    }

                }
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun applyPreviousSetPerformance(setId: Long) {
        exercises.value.forEachIndexed { index, eWs ->
            val setToUpdateIndex =
                eWs.sets.indexOfFirst { it.id == setId }

            val previousPerformanceSet =
                previousPerformances.value.getOrNull(index)?.getOrNull(setToUpdateIndex)

            previousPerformanceSet?.let { values ->
                val (reps, load, time) = values
                when (eWs.exercise.setMode) {
                    SetMode.LOAD -> {
                        updateSetLoad(load, setId)
                        updateSetReps(reps, setId)
                    }

                    SetMode.BODYWEIGHT -> {
                        updateSetReps(reps, setId)
                    }

                    SetMode.BODYWEIGHT_WITH_LOAD -> {
                        updateSetLoad(load, setId)
                        updateSetReps(reps, setId)
                    }

                    SetMode.DURATION -> {
                        updateSetTime(time, setId)
                    }
                }
            }
        }
    }

    // A Job to hold the running set's stopwatch coroutine
    private var stopwatchJob: Job? = null

    init {
        viewModelScope.launch {
            if (workoutId != 0L) {
                val workoutWithExercisesAndSets =
                    workoutRepository.getWorkoutWithExercisesAndSets(workoutId)

                _workout.update {
                    workoutWithExercisesAndSets.workout
                }

                // Delete previous running workout from db as it will be resaved later (see down below)
                if (workoutWithExercisesAndSets.workout.state == WorkoutState.RUNNING) {
                    workoutRepository.deleteWorkout(workoutWithExercisesAndSets.workout.toEntity())
                    workoutServiceManager.setInitialTimeElapsed(workoutWithExercisesAndSets.workout.timeElapsed)
                }

                _exercises.update {
                    workoutWithExercisesAndSets.exercisesWithSets
                }

                _idsOfSetsWithStopwatchNotStartedAtLeastOnce.update {
                    exercises.value.flatMap { it.sets }.map { it.id }.toSet()
                }
            }
        }

        viewModelScope.launch {
            idSetWithRunningStopwatch.collect { runningSetId ->
                // Whenever the ID changes, cancel any existing timer.
                stopwatchJob?.cancel()

                // If the new ID is a valid set ID, start a new timer.
                if (runningSetId != null) {
                    exercises.value.flatMap { it.sets }.find { it.id == runningSetId }?.let {
                        // Launch a new coroutine for the timer and assign it to the Job.
                        stopwatchJob = launch { startSetStopwatch(it) }
                    }
                }
            }
        }

        // Keep updated the exerciseDCs based on user changes
        viewModelScope.launch {
            datasetRepository.customExercises
                .distinctUntilChanged()
                .collect { customExercises ->
                    _exercises.update { exercises ->
                        exercises.mapNotNull { exercise ->
                            if (exercise.exerciseDC.isCustomExercise) {
                                // Fetch updated exerciseDC from DB. If it's deleted, delete also the associated exercises (by returning null in a mapNotNull)
                                customExercises.find { it.id == exercise.exerciseDC.id }?.let {
                                    exercise.copy(exerciseDC = it.toUi())
                                }
                            } else {
                                exercise
                            }
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
            exercises + newExercise
        }
    }

    fun addSetToExercise(exerciseId: Long) {
        val newId = Random.nextLong()
        _exercises.update { exercises ->
            exercises.map { exercise ->
                if (exercise.exercise.id == exerciseId) {
                    val newSet = exercise.sets
                        .lastOrNull()?.copy(id = newId)
                        ?: UiSet()

                    val newSets = exercise.sets.toMutableList() + newSet
                    exercise.copy(sets = newSets.toImmutableList())
                } else exercise
            }
        }

        _idsOfSetsWithStopwatchNotStartedAtLeastOnce.update {
            it + newId
        }
    }

    fun updateSetTime(time: Int, id: Long) {
        _exercises.update { currentExercises ->
            currentExercises.map { exercise ->
                if (exercise.sets.any { it.id == id }) {
                    exercise.copy(
                        sets = exercise.sets.map {
                            if (it.id == id) it.copy(
                                elapsedTime = time.coerceAtMost(60 * 100) // max = 99:59
                            ) else it
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
            startRestTimer(exerciseWithSets.exercise.restTime)
        }
    }

    fun deleteSet(id: Long) {
        // If there's the match, then the set has a running stopwatch and it has to be stopped by assigning null
        if (idSetWithRunningStopwatch.value == id) {
            _idSetWithRunningStopwatch.update { null }
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
        // If there's the match, then the set has a running stopwatch and it has to be stopped by assign 0
        if (exerciseWithSets.sets.any { it.id == idSetWithRunningStopwatch.value }) {
            _idSetWithRunningStopwatch.update { 0L }
        }
        _exercises.update { currentExercises ->
            currentExercises.filter { it.exercise.id != exerciseId }
        }
    }

    val workoutProgress: StateFlow<Pair<Int, Int>> = exercises
        .map { list ->
            val totalSets = list.sumOf { it.sets.size }

            val completedSets = list.sumOf { it.sets.count { s -> s.completed } }

            completedSets to totalSets
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0 to 0
        )


    val timeElapsed = WorkoutService.timeElapsed
    val isStopwatchPaused = WorkoutService.isStopwatchPaused
    val restTime = WorkoutService.restTime


    private var initialRestTime = 1
    private var isFocused = true


    init {
        workoutServiceManager.startStopwatch()
        observeChanges()
    }


    private fun observeChanges() {
        viewModelScope.launch(Dispatchers.Main) {
            WorkoutService.restTime.collect { newRestTime ->
                // When timer is over and screen is visible, it plays alert sound only by respecting user preference
                if (initialRestTime != 1 && newRestTime == 0 && isFocused) {
                    if (userPreferences.restTimerSoundOn.value) {
                        val mediaPlayer = MediaPlayer.create(context, R.raw.alert_notification)
                        mediaPlayer.setOnCompletionListener {
                            it.release()
                        }
                        mediaPlayer.start()
                    }
                    initialRestTime = 1
                }
            }
        }
    }

    fun toggleStopwatch() {
        if (isStopwatchPaused.value) {
            workoutServiceManager.startStopwatch()
        } else {
            workoutServiceManager.pauseStopwatch()
        }
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

    val restTimerProgress = restTime
        .map { it.toFloat() / initialRestTime }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    fun stopWorkoutService() {
        workoutServiceManager.stopService()
    }


    val keepScreenOn = userPreferences.workoutScreenOn


    val runningWorkoutState = combine(
        workout, exercises, timeElapsed
    ) { w, e, t -> Triple(w, e, t) }
        .debounce(500L)

    private val _runningWorkoutId = MutableStateFlow(0L)
    val runningWorkoutId = _runningWorkoutId.asStateFlow()
    suspend fun saveRunningWorkout(state: Triple<UiWorkout, List<UiExerciseWithSets>, Int>) {
        val (w, e, t) = state
        _runningWorkoutId.update { id ->
            workoutRepository.addWorkoutWithExercisesAndSets(
                WorkoutWithExercisesAndSets(
                    workout = w.copy(
                        id = id,
                        state = WorkoutState.RUNNING,
                        timeElapsed = t
                    ).toEntity(),
                    exercisesWithSets = e.map { it.toEntity() },
                )
            )
        }
    }


    val isHeaderSticky = userPreferences.isWorkoutHeaderSticky

    val useNumberPicker = userPreferences.useNumberPicker
}
