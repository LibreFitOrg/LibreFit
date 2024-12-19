/*
 * Copyright (c) 2024 LibreFit
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.librefit.MainApplication
import org.librefit.enums.WorkoutServiceActions
import org.librefit.helpers.NotificationHelper

class WorkoutService : Service() {

    companion object {
        private val _timeElapsed = MutableStateFlow(0)
        val timeElapsed: StateFlow<Int> = _timeElapsed

        private val _isChronometerPaused = MutableStateFlow(false)
        val isChronometerPaused: StateFlow<Boolean> = _isChronometerPaused

        private val _restTime = MutableStateFlow(0)
        val restTime: StateFlow<Int> = _restTime

        private val _isRestTimerPaused = MutableStateFlow(false)
        val isRestTimerPaused: StateFlow<Boolean> = _isRestTimerPaused
    }


    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = WorkoutServiceActions.entries.find { it.string == intent?.action }
            ?: WorkoutServiceActions.START_CHRONOMETER
        when (action) {
            WorkoutServiceActions.START_CHRONOMETER -> startChronometer()
            WorkoutServiceActions.PAUSE_CHRONOMETER -> pauseChronometer()
            WorkoutServiceActions.START_REST_TIMER -> startRestTimer()
            WorkoutServiceActions.MODIFY_REST_TIMER -> modifyRestTimer()
            WorkoutServiceActions.STOP_SERVICE -> stopService()
        }
        return START_STICKY
    }

    fun stopService() {
        pauseChronometer()
        chronometerJob?.cancel()
        restTimerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private var notificationHelper = MainApplication.notificationHelper


    private var chronometerJob: Job? = null

    private fun startChronometer() {
        startForeground(
            NotificationHelper.WORKOUT_NOTIFICATION_ID,
            notificationHelper.createWorkoutNotification()
        )

        _isChronometerPaused.value = false

        val startTime = System.currentTimeMillis()
        val pastTime = _timeElapsed.value

        chronometerJob = CoroutineScope(Dispatchers.Main).launch {
            while (!_isChronometerPaused.value) {
                val currentTime = System.currentTimeMillis()

                _timeElapsed.value = (currentTime - startTime).toInt() / 1000 + pastTime

                notificationHelper.notifyWorkout(_timeElapsed.value, _isChronometerPaused.value)

                delay(1000)
            }
        }
    }

    private fun pauseChronometer() {
        _isChronometerPaused.value = true
    }


    private var restTimerJob: Job? = null

    private fun startRestTimer() {
        //TODO: retrieve restTimer from WorkoutScreenViewModel
        _restTime.value = 10
        val target = System.currentTimeMillis() + restTime.value * 1000

        restTimerJob = CoroutineScope(Dispatchers.Main).launch {
            while (_restTime.value > 0) {
                val currentTime = System.currentTimeMillis()

                _restTime.value = ((target - currentTime) / 1000).toInt()

                delay(1000)
            }
            notificationHelper.notifyAlarm()
        }
    }

    private fun modifyRestTimer() {

    }
}