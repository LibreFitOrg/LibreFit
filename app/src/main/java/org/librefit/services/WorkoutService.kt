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

package org.librefit.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import org.librefit.services.WorkoutService.Companion.isChronometerPaused
import org.librefit.services.WorkoutService.Companion.restTime
import org.librefit.services.WorkoutService.Companion.timeElapsed
import javax.inject.Inject

/**
 * A service that manages the chronometer and the rest timer during a workout session.
 *
 * This service is designed to run in the foreground and it's used by
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel] to start, pause, and modify
 * a chronometer and a rest timer.
 *
 * ## State Flows
 * - [timeElapsed]: A [StateFlow] that emits the total time elapsed in seconds.
 * - [isChronometerPaused]: A [StateFlow] that indicates whether the chronometer is currently paused.
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
 * - [WorkoutServiceActions.START_CHRONOMETER]: Starts and resumes the chronometer with [startChronometer]
 * - [WorkoutServiceActions.PAUSE_CHRONOMETER]: Pauses the chronometer with [pauseChronometer]
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

    companion object {
        private val _timeElapsed = MutableStateFlow(0)
        val timeElapsed: StateFlow<Int> = _timeElapsed

        private val _isChronometerPaused = MutableStateFlow(false)
        val isChronometerPaused: StateFlow<Boolean> = _isChronometerPaused

        private val _restTime = MutableStateFlow(0)
        val restTime: StateFlow<Int> = _restTime

        const val EXTRA_INITIAL_REST_TIME = "EXTRA_INITIAL_REST_TIME"
        const val EXTRA_ADD_TEN_SECONDS = "EXTRA_ADD_TEN_SECONDS"
        const val EXTRA_IS_FOCUSED = "EXTRA_IS_FOCUSED"
    }

    private var initialRestTime = 0
    private var isFocused = true

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = WorkoutServiceActions.entries.find { it.string == intent?.action }
            ?: WorkoutServiceActions.START_CHRONOMETER
        when (action) {
            WorkoutServiceActions.START_CHRONOMETER -> startChronometer()
            WorkoutServiceActions.PAUSE_CHRONOMETER -> pauseChronometer()
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
        }

        startForeground(
            NotificationHelper.WORKOUT_NOTIFICATION_ID,
            notificationHelper.createWorkoutNotification()
        )

        return START_STICKY
    }

    fun stopService() {
        chronometerJob?.cancel()
        restTimerJob?.cancel()
        _timeElapsed.update { 0 }
        _restTime.update { 0 }
        initialRestTime = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }



    private var chronometerJob: Job? = null

    private fun startChronometer() {

        _isChronometerPaused.update { false }

        val startTime = System.currentTimeMillis()
        val pastTime = timeElapsed.value

        chronometerJob?.cancel()

        chronometerJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                if (!isChronometerPaused.value) {
                    val currentTime = System.currentTimeMillis()

                    _timeElapsed.update {
                        (currentTime - startTime).toInt() / 1000 + pastTime
                    }
                }

                notificationHelper.notifyOngoingWorkout(
                    timeElapsed.value,
                    isChronometerPaused.value,
                    restTime.value,
                    initialRestTime
                )

                delay(1000)
            }
        }
    }

    private fun pauseChronometer() {
        _isChronometerPaused.update { true }
        notificationHelper.notifyOngoingWorkout(timeElapsed.value, isChronometerPaused.value)
    }


    private var restTimerJob: Job? = null

    private fun startRestTimer() {
        restTimerJob = CoroutineScope(Dispatchers.Main).launch {
            while (restTime.value > 0) {
                _restTime.update { it - 1 }
                delay(1000)
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
            }
        }
    }
}