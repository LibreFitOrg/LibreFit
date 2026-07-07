/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.services

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.librefit.enums.WorkoutServiceActions
import org.librefit.services.WorkoutService.Companion.EXTRA_ADD_TEN_SECONDS
import org.librefit.services.WorkoutService.Companion.EXTRA_COUNTDOWN_DURATION
import org.librefit.services.WorkoutService.Companion.EXTRA_COUNTDOWN_REST_DURATION
import org.librefit.services.WorkoutService.Companion.EXTRA_INITIAL_REST_TIME
import org.librefit.services.WorkoutService.Companion.EXTRA_IS_FOCUSED
import org.librefit.services.WorkoutService.Companion.EXTRA_SET_ELAPSED_TIME
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutServiceManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val workoutServiceIntent by lazy {
        Intent(context, WorkoutService::class.java)
    }

    fun startStopwatch() {
        val serviceIntent = workoutServiceIntent.apply {
            action = WorkoutServiceActions.START_STOPWATCH.string
        }
        context.startForegroundService(serviceIntent)
    }

    fun pauseStopwatch() {
        val serviceIntent = workoutServiceIntent.apply {
            action = WorkoutServiceActions.PAUSE_STOPWATCH.string
        }
        context.startForegroundService(serviceIntent)
    }

    fun updateFocus(isFocused: Boolean) {
        val serviceIntent = workoutServiceIntent.apply {
            action = WorkoutServiceActions.WORKOUT_FOCUS.string
            putExtra(EXTRA_IS_FOCUSED, isFocused)
        }
        context.startForegroundService(serviceIntent)
    }

    fun startRestTimer(initialValue: Int) {
        val serviceIntent = workoutServiceIntent.apply {
            action = WorkoutServiceActions.START_REST_TIMER.string
            putExtra(EXTRA_INITIAL_REST_TIME, initialValue)
        }
        context.startForegroundService(serviceIntent)
    }

    fun modifyRestTime(addTenSeconds: Boolean) {
        val serviceIntent = workoutServiceIntent.apply {
            action = WorkoutServiceActions.MODIFY_REST_TIMER.string
            putExtra(EXTRA_ADD_TEN_SECONDS, addTenSeconds)
        }
        context.startForegroundService(serviceIntent)
    }

    fun setInitialTimeElapsed(seconds: Int) {
        val serviceIntent = workoutServiceIntent.apply {
            action = WorkoutServiceActions.SET_ELAPSED_TIME.string
            putExtra(EXTRA_SET_ELAPSED_TIME, seconds)
        }
        context.startForegroundService(serviceIntent)
    }

    /**
     * Start a HIIT countdown timer that counts down from [durationSeconds] to 0,
     * then auto-starts rest timer for [restDurationSeconds] if > 0.
     */
    fun startCountdown(durationSeconds: Int, restDurationSeconds: Int) {
        val serviceIntent = workoutServiceIntent.apply {
            action = WorkoutServiceActions.START_COUNTDOWN.string
            putExtra(EXTRA_COUNTDOWN_DURATION, durationSeconds)
            putExtra(EXTRA_COUNTDOWN_REST_DURATION, restDurationSeconds)
        }
        context.startForegroundService(serviceIntent)
    }

    fun cancelCountdown() {
        val serviceIntent = workoutServiceIntent.apply {
            action = WorkoutServiceActions.CANCEL_COUNTDOWN.string
        }
        context.startForegroundService(serviceIntent)
    }

    fun stopService() {
        val serviceIntent = workoutServiceIntent.apply {
            action = WorkoutServiceActions.STOP_SERVICE.string
        }
        context.startService(serviceIntent)
    }

}