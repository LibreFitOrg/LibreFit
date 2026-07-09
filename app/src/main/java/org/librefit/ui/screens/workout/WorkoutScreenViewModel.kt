/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
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
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.db.repository.DatasetRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.di.qualifiers.IoDispatcher
import org.librefit.di.qualifiers.MainDispatcher
import org.librefit.enums.PreviousPerformanceSet
import org.librefit.enums.SetMode
import org.librefit.enums.WarmupMode
import org.librefit.enums.WorkoutState
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.helpers.SoundPlayer
import org.librefit.nav.Route
import org.librefit.services.WorkoutService
import org.librefit.services.WorkoutServiceManager
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

@OptIn(FlowPreview::class)
@HiltViewModel
class WorkoutScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userPreferences: UserPreferencesRepository,
    private val workoutServiceManager: WorkoutServiceManager,
    private val workoutRepository: WorkoutRepository,
    private val datasetRepository: DatasetRepository,
    private val soundPlayer: SoundPlayer,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher
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

        // The loop is infinite, but the coroutine will be stopped when its Job is canceled
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

    private val _workoutItems = MutableStateFlow<List<UiWorkoutItem>>(emptyList())
    val workoutItems = _workoutItems.asStateFlow()

    val previousPerformances: StateFlow<List<List<PreviousPerformanceSet>>> =
        combine(workoutItems, workout) { list, w ->
            list.filterIsInstance<UiExerciseItem>().map { eWs: UiExerciseItem ->
                val list = workoutRepository.getCompletedWorkoutsWithExercisesWithIdExerciseDC(
                    idExerciseDC = eWs.exercise.exerciseDC.id
                ).firstOrNull()

                // It tries to find in the completed workouts the previous performance of
                // the same exercise and in the same routine. If there isn't a linked routine, it takes the
                // latest workout with that exercise
                val previousWorkout =
                    list?.find { it.workout.routineId == w.routineId } ?: list?.firstOrNull()
                val previousEWS =
                    previousWorkout?.exercisesWithSets?.find { it.exerciseDC.id == eWs.exercise.exerciseDC.id }

                List(eWs.sets.size) { index ->
                    val previousSet = previousEWS?.sets?.getOrNull(index)
                    val reps = previousSet?.reps ?: 0
                    val load = previousSet?.load ?: 0.0
                    val time = previousSet?.elapsedTime ?: 0


                    when (eWs.exercise.exercise.setMode) {
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
        workoutItems.value.filterIsInstance<UiExerciseItem>().forEachIndexed { index, eWs ->
            val setToUpdateIndex =
                eWs.sets.indexOfFirst { it.id == setId }

            val previousPerformanceSet =
                previousPerformances.value.getOrNull(index)?.getOrNull(setToUpdateIndex)

            previousPerformanceSet?.let { values ->
                val (reps, load, time) = values
                when (eWs.exercise.exercise.setMode) {
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

    override fun onCleared() {
        super.onCleared()
        stopwatchJob?.cancel()
    }

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

                _workoutItems.update {
                    (workoutWithExercisesAndSets.exercisesWithSets.map { UiExerciseItem(it) } + workoutWithExercisesAndSets.warmupsWithSets.map {
                        UiWarmupItem(
                            it
                        )
                    }).sortedBy { it.position }
                }

                _idsOfSetsWithStopwatchNotStartedAtLeastOnce.update {
                    workoutItems.value.flatMap { it.sets }.map { it.id }.toSet()
                }
            }
        }

        viewModelScope.launch {
            idSetWithRunningStopwatch.collect { runningSetId ->
                // Whenever the ID changes, cancel any existing timer.
                stopwatchJob?.cancel()

                // If the new ID is a valid set ID, start a new timer.
                if (runningSetId != null) {
                    workoutItems.value.flatMap { it.sets }.find { it.id == runningSetId }?.let {
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
                    _workoutItems.update { currentWorkoutItems ->
                        currentWorkoutItems.filterIsInstance<UiExerciseItem>()
                            .mapNotNull { exercise ->
                                if (exercise.exercise.exerciseDC.isCustomExercise) {
                                    // Fetch updated exerciseDC from DB. If it's deleted, delete also the associated exercises (by returning null in a mapNotNull)
                                    customExercises.find { it.id == exercise.exercise.exerciseDC.id }
                                        ?.let {
                                            exercise.copy(
                                                exercise = exercise.exercise.copy(
                                                    exerciseDC = it.toUi()
                                                )
                                            )
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

        _workoutItems.update { currentWorkoutItems ->
            (currentWorkoutItems + newExercise).withNormalizedExercisePositions()
        }
        syncToRepository()
    }

    fun addSetToExercise(exerciseId: Long) {
        val newId = Random.nextLong()
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { workoutItem ->
                if (workoutItem.id == exerciseId) {
                    val newSet = workoutItem.sets
                        .lastOrNull()?.copy(id = newId)
                        ?: UiSet()

                    val newSets = workoutItem.sets.toMutableList() + newSet
                    workoutItem.updateSets(newSets.toImmutableList())
                } else workoutItem
            }
        }

        _idsOfSetsWithStopwatchNotStartedAtLeastOnce.update {
            it + newId
        }
        syncToRepository()
    }

    fun updateSetTime(time: Int, id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { workoutItem ->
                if (workoutItem.sets.any { it.id == id }) {
                    workoutItem.updateSets(
                        workoutItem.sets.map {
                            if (it.id == id) it.copy(
                                elapsedTime = time.coerceAtMost(60 * 100) // max = 99:59
                            ) else it
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
        val workoutItem = workoutItems.value.find { e -> e.sets.any { it.id == id } }!!
        if (completed && workoutItem.restTime != 0) {
            startRestTimer(workoutItem.restTime)
        }
        syncToRepository()
    }

    fun deleteSet(id: Long) {
        // If there's the match, then the set has a running stopwatch and it has to be stopped by assigning null
        if (idSetWithRunningStopwatch.value == id) {
            _idSetWithRunningStopwatch.update { null }
        }

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
                if (workoutItem.id == id) workoutItem.updateNotes(notes) else workoutItem
            }
        }
        syncToRepository()
    }

    fun updateExerciseRestTime(restTime: Int, id: Long) {
        _workoutItems.update { currentWorkoutItems ->
            currentWorkoutItems.map { workoutItem ->
                if (workoutItem.id == id) workoutItem.updateRestTime(restTime) else workoutItem
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
        val exerciseWithSets = workoutItems.value.find { it.id == exerciseId }!!
        // If there's the match, then the set has a running stopwatch and it has to be stopped by assign 0
        if (exerciseWithSets.sets.any { it.id == idSetWithRunningStopwatch.value }) {
            _idSetWithRunningStopwatch.update { 0L }
        }
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

    val workoutProgress: StateFlow<Pair<Int, Int>> = workoutItems
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
        viewModelScope.launch(mainDispatcher) {
            WorkoutService.restTime.collect { newRestTime ->
                // When timer is over and screen is visible, it plays alert sound only by respecting user preference
                if (initialRestTime != 1 && newRestTime == 0 && isFocused) {
                    if (userPreferences.restTimerSoundOn.value) {
                        soundPlayer.playAlert()
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
     * It ensures that [WorkoutService] sends an alert notification only when the app is not focused
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


    /**
     * Auto-syncs UI changes to the repository. This guarantees the repository is the
     * single source of truth for all uncommitted workout state.
     */
    private fun syncToRepository() {
        viewModelScope.launch(ioDispatcher) {
            workoutRepository.setPendingWorkout(
                UiWorkoutWithExercisesAndSets(
                    workout = workout.value.copy(
                        state = WorkoutState.RUNNING,
                        timeElapsed = timeElapsed.value
                    ),
                    exercisesWithSets = workoutItems.value.filterIsInstance<UiExerciseItem>()
                        .map { it.exercise }.toImmutableList(),
                    warmupsWithSets = workoutItems.value.filterIsInstance<UiWarmupItem>()
                        .map { it.warmup }.toImmutableList()
                )
            )
        }
    }


    data class WorkoutCurrentState(
        val workout: UiWorkout,
        val exercises: List<UiExerciseWithSets>,
        val warmups: List<UiWarmupWithSets>,
        val timeElapsed: Int
    )

    // Safety-net persistence: writes to DB every 2s if state is pending.
    // Separate this from UI-driven sync to avoid the heartbeat being reset by frequent timer ticks.
    init {
        viewModelScope.launch(ioDispatcher) {
            // Observe workout and exercises only, ignoring timeElapsed to avoid frequent resets
            combine(workout, workoutItems, timeElapsed) { w, wi, t ->
                WorkoutCurrentState(
                    w,
                    wi.filterIsInstance<UiExerciseItem>().map { it.exercise },
                    wi.filterIsInstance<UiWarmupItem>().map { it.warmup },
                    t
                )
            }
                .debounce(2000L)
                .collect { state ->
                    // Do not save when user navigates away
                    if (isFocused) {
                        saveRunningWorkout(state)
                    }
                }
        }
    }

    private val _runningWorkoutId = MutableStateFlow(0L)
    val runningWorkoutId = _runningWorkoutId.asStateFlow()
    suspend fun saveRunningWorkout(state: WorkoutCurrentState) {
        _runningWorkoutId.update { id ->
            workoutRepository.addWorkoutWithExercisesAndSets(
                WorkoutWithExercisesAndSets(
                    workout = state.workout.copy(
                        id = id,
                        state = WorkoutState.RUNNING,
                        timeElapsed = state.timeElapsed
                    ).toEntity(),
                    exercisesWithSets = state.exercises.map { it.toEntity() },
                    warmupsWithSets = state.warmups.map { it.toEntity() }
                )
            )
        }
    }


    val isHeaderSticky = userPreferences.isWorkoutHeaderSticky

    val useScrollWheelForInput = userPreferences.useScrollWheelForInput

    val dismissScrollWheelInputAutomatically = userPreferences.dismissScrollWheelInputAutomatically
}
