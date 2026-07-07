/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.enums.WorkoutServiceActions
import org.librefit.helpers.NotificationHelper
import org.librefit.services.WorkoutService.Companion.EXTRA_ADD_TEN_SECONDS
import org.librefit.services.WorkoutService.Companion.EXTRA_INITIAL_REST_TIME
import org.librefit.services.WorkoutService.Companion.EXTRA_IS_FOCUSED
import org.librefit.services.WorkoutService.Companion.isStopwatchPaused
import org.librefit.services.WorkoutService.Companion.restTime
import org.librefit.services.WorkoutService.Companion.timeElapsed
import javax.inject.Inject

/**
 * A service that manages the stopwatch and the rest timer during a workout session.
 *
 * This service is designed to run in the foreground and it's used by
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel] to start, pause, and modify
 * a stopwatch and a rest timer.
 *
 * ## State Flows
 * - [timeElapsed]: A [StateFlow] that emits the total time elapsed in seconds.
 * - [isStopwatchPaused]: A [StateFlow] that indicates whether the stopwatch is currently paused.
 * - [restTime]: A [StateFlow] that emits the remaining time for the rest timer in seconds.
 *
 * ## Intent Extras
 * - [EXTRA_INITIAL_REST_TIME]: An integer extra that specifies the initial time for the rest timer.
 * - [EXTRA_ADD_TEN_SECONDS]: A boolean extra that indicates how to modify the rest timer. When
 *   `true` it adds ten seconds otherwise it subtracts.
 * - [EXTRA_IS_FOCUSED]: A boolean extra that indicates whether the workout is currently focused. When
 *   `false` the rest timer notification is sent.
 *
 * ## Actions
 * The service can handle the following actions:
 * - [WorkoutServiceActions.START_STOPWATCH]: Starts and resumes the stopwatch with [startStopwatch]
 * - [WorkoutServiceActions.PAUSE_STOPWATCH]: Pauses the stopwatch with [pauseStopwatch]
 * - [WorkoutServiceActions.START_REST_TIMER]: Starts the rest timer with the specified initial time with [startRestTimer]
 * - [WorkoutServiceActions.MODIFY_REST_TIMER]: Modifies the rest timer by adding or subtracting ten
 *   seconds with [modifyRestTimer].
 * - [WorkoutServiceActions.WORKOUT_FOCUS]: Updates [isFocused] based on the focus state of [org.librefit.ui.screens.workout.WorkoutScreen].
 * - [WorkoutServiceActions.STOP_SERVICE]: It calls [stopService] and resets all timers.
 *
 * ## Lifecycle
 * - [onBind]: Returns null as this service is not bound to any component.
 * - [onStartCommand]: Handles incoming intents and performs the corresponding actions.
 *
 * ## Notifications
 * The service uses [NotificationHelper] to create and manage notifications.
 */

@AndroidEntryPoint
class WorkoutService : Service() {

    // A lifecycle-aware scope for the entire service
    // SupervisorJob ensures that if one child coroutine fails, it doesn't cancel the others.
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private val _timeElapsed = MutableStateFlow(0)
        val timeElapsed: StateFlow<Int> = _timeElapsed

        private val _isStopwatchPaused = MutableStateFlow(false)
        val isStopwatchPaused: StateFlow<Boolean> = _isStopwatchPaused

        private val _restTime = MutableStateFlow(0)
        val restTime: StateFlow<Int> = _restTime

        /** Countdown timer for HIIT sets (counts down to 0). */
        private val _countdownTime = MutableStateFlow(0)
        val countdownTime: StateFlow<Int> = _countdownTime

        /** Whether a HIIT countdown is actively running. */
        private val _isCountdownActive = MutableStateFlow(false)
        val isCountdownActive: StateFlow<Boolean> = _isCountdownActive

        /** Total duration for the current countdown (used for progress calculation). */
        private val _countdownTotal = MutableStateFlow(0)
        val countdownTotal: StateFlow<Int> = _countdownTotal

        /** Emits true once when a countdown finishes — consumed by the ViewModel to auto-start rest. */
        private val _countdownFinished = MutableStateFlow(false)
        val countdownFinished: StateFlow<Boolean> = _countdownFinished

        const val EXTRA_INITIAL_REST_TIME = "EXTRA_INITIAL_REST_TIME"
        const val EXTRA_ADD_TEN_SECONDS = "EXTRA_ADD_TEN_SECONDS"
        const val EXTRA_IS_FOCUSED = "EXTRA_IS_FOCUSED"
        const val EXTRA_SET_ELAPSED_TIME = "EXTRA_SET_ELAPSED_TIME"
        const val EXTRA_COUNTDOWN_DURATION = "EXTRA_COUNTDOWN_DURATION"
        /** Rest duration to auto-start after countdown finishes (0 = skip rest). */
        const val EXTRA_COUNTDOWN_REST_DURATION = "EXTRA_COUNTDOWN_REST_DURATION"

        /** Acknowledge the [countdownFinished] signal (call from ViewModel after reading it). */
        fun clearCountdownFinished() {
            _countdownFinished.update { false }
        }
    }

    private var initialRestTime = 0
    private var isFocused = true
    private var countdownRestDuration = 0

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = WorkoutServiceActions.entries.find { it.string == intent?.action }
            ?: WorkoutServiceActions.START_STOPWATCH
        when (action) {
            WorkoutServiceActions.START_STOPWATCH -> {
                startStopwatch()
                startForeground(
                    NotificationHelper.WORKOUT_NOTIFICATION_ID,
                    notificationHelper.createWorkoutNotification()
                )
            }

            WorkoutServiceActions.PAUSE_STOPWATCH -> pauseStopwatch()
            WorkoutServiceActions.START_REST_TIMER -> {
                val initialRestTime = intent?.getIntExtra(EXTRA_INITIAL_REST_TIME, 0) ?: 0

                restTimerJob?.cancel()
                this.initialRestTime = initialRestTime
                _restTime.update { initialRestTime }

                startRestTimer()
            }

            WorkoutServiceActions.MODIFY_REST_TIMER -> {
                val addTenSeconds = intent?.getBooleanExtra(EXTRA_ADD_TEN_SECONDS, true) != false
                modifyRestTimer(addTenSeconds)
            }

            WorkoutServiceActions.WORKOUT_FOCUS -> {
                isFocused = intent?.getBooleanExtra(EXTRA_IS_FOCUSED, true) != false
            }

            WorkoutServiceActions.STOP_SERVICE -> stopService()
            WorkoutServiceActions.SET_ELAPSED_TIME -> {
                _timeElapsed.update {
                    it + (intent?.getIntExtra(EXTRA_SET_ELAPSED_TIME, 0) ?: 0)
                }
            }

            WorkoutServiceActions.START_COUNTDOWN -> {
                val duration = intent?.getIntExtra(EXTRA_COUNTDOWN_DURATION, 0) ?: 0
                countdownRestDuration =
                    intent?.getIntExtra(EXTRA_COUNTDOWN_REST_DURATION, 0) ?: 0
                countdownJob?.cancel()
                _countdownTotal.update { duration }
                _countdownTime.update { duration }
                _isCountdownActive.update { true }
                _countdownFinished.update { false }
                startCountdown()
            }

            WorkoutServiceActions.CANCEL_COUNTDOWN -> {
                countdownJob?.cancel()
                _isCountdownActive.update { false }
                _countdownTime.update { 0 }
            }
        }

        return START_STICKY
    }

    fun stopService() {
        stopwatchJob?.cancel()
        restTimerJob?.cancel()
        countdownJob?.cancel()
        _timeElapsed.update { 0 }
        _restTime.update { 0 }
        _countdownTime.update { 0 }
        _isCountdownActive.update { false }
        _countdownFinished.update { false }
        _countdownTotal.update { 0 }
        initialRestTime = 0
        countdownRestDuration = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }


    private var stopwatchJob: Job? = null

    private fun startStopwatch() {

        _isStopwatchPaused.update { false }

        stopwatchJob?.cancel()

        stopwatchJob = serviceScope.launch {
            while (true) {
                delay(1000)

                if (!isStopwatchPaused.value) {
                    _timeElapsed.update {
                        // TODO: use a more accurate calculation
                        it + 1
                    }
                }

                if (isFocused) {
                    notificationHelper.cancelTimerIsOverNotification()
                }

                notificationHelper.notifyOngoingWorkout(
                    timeElapsed.value,
                    isStopwatchPaused.value,
                    restTime.value,
                    initialRestTime
                )
            }
        }
    }

    private fun pauseStopwatch() {
        _isStopwatchPaused.update { true }
        notificationHelper.notifyOngoingWorkout(timeElapsed.value, isStopwatchPaused.value)
    }


    private var countdownJob: Job? = null

    /**
     * HIIT countdown: counts from [countdownTime] down to 0, then:
     * 1. Signals [countdownFinished] for the ViewModel
     * 2. Auto-starts the rest timer if [countdownRestDuration] > 0
     */
    private fun startCountdown() {
        countdownJob = serviceScope.launch {
            while (countdownTime.value > 0) {
                delay(1000)
                _countdownTime.update { (it - 1).coerceAtLeast(0) }
            }
            _isCountdownActive.update { false }
            _countdownFinished.update { true }

            // Auto-start rest if configured
            if (countdownRestDuration > 0) {
                restTimerJob?.cancel()
                initialRestTime = countdownRestDuration
                _restTime.update { countdownRestDuration }
                startRestTimer()
            }
        }
    }



    private var restTimerJob: Job? = null

    private fun startRestTimer() {
        restTimerJob = serviceScope.launch {
            while (restTime.value > 0) {
                delay(1000)
                _restTime.update { (it - 1).coerceAtLeast(0) }
            }
            if (!isFocused) {
                notificationHelper.notifyTimerIsOver()
            }

            initialRestTime = 0
        }
    }

    private fun modifyRestTimer(addTenSeconds: Boolean) {
        _restTime.update { currentRestTime ->
            if (addTenSeconds) {
                currentRestTime + 10
            } else {
                if (currentRestTime > 10) {
                    currentRestTime - 10
                } else {
                    0
                }
            }.coerceAtLeast(0)
        }
    }
}