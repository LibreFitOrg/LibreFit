/*
 * Copyright (c) 2024. LibreFit
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

package org.librefit.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.librefit.MainActivity
import org.librefit.R
import org.librefit.enums.WorkoutServiceActions
import org.librefit.services.WorkoutService
import org.librefit.services.WorkoutService.Companion.EXTRA_ADD_TEN_SECONDS
import org.librefit.util.formatTime


class NotificationHelper(context: Context) {
    companion object {
        const val WORKOUT_CHANNEL_ID = "WORKOUT_CHANNEL_ID"
        const val WORKOUT_NOTIFICATION_ID = 1001

        const val TIMER_CHANNEL_ID = "TIMER_CHANNEL_ID"
        const val TIMER_NOTIFICATION_ID = 1002
    }

    private val appContext = context.applicationContext


    private val workoutNotificationBuilder =
        NotificationCompat.Builder(appContext, WORKOUT_CHANNEL_ID)

    private val timerNotificationBuilder = NotificationCompat.Builder(appContext, TIMER_CHANNEL_ID)


    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private lateinit var workoutNotification: Notification

    init {
        createNotificationChannels()
        initializeNotificationBuilders()
    }

    private fun initializeNotificationBuilders() {
        val contentIntent = PendingIntent.getActivity(
            appContext,
            0,
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        workoutNotificationBuilder
            .setSmallIcon(R.drawable.ic_logo_monochrome)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)


        timerNotificationBuilder
            .setSmallIcon(R.drawable.ic_logo_monochrome)
            .setContentTitle(appContext.getString(R.string.rest_time_is_over))
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)

        timerNotification = timerNotificationBuilder.build()
    }


    private fun createNotificationChannels() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                WORKOUT_CHANNEL_ID,
                appContext.getString(R.string.workout_info_channel_title),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = appContext.getString(R.string.workout_info_channel_desc)
            }
        )


        notificationManager.createNotificationChannel(
            NotificationChannel(
                TIMER_CHANNEL_ID,
                appContext.getString(R.string.rest_timer_channel_title),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = appContext.getString(R.string.rest_timer_channel_desc)
            }
        )
    }

    fun createWorkoutNotification(): Notification {
        workoutNotification = workoutNotificationBuilder.build()
        return workoutNotification
    }

    fun notifyOngoingWorkout(
        timeInSeconds: Int,
        isChronometerPaused: Boolean,
        restTime: Int = 0,
        initialRestTime: Int = 0,
    ) {
        val builder = workoutNotificationBuilder

        builder
            .setProgress(initialRestTime, restTime, false)
            .clearActions()

        if (isChronometerPaused) {
            builder
                .addAction(
                    android.R.drawable.ic_media_play,
                    appContext.getString(R.string.resume),
                    createWorkoutServiceIntent(WorkoutServiceActions.START_CHRONOMETER.string)
                )
        } else {
            builder
                .addAction(
                    R.drawable.ic_pause,
                    appContext.getString(R.string.pause),
                    createWorkoutServiceIntent(WorkoutServiceActions.PAUSE_CHRONOMETER.string)
                )
        }


        if (restTime != 0 && initialRestTime != 0) {
            builder
                .setContentTitle(
                    appContext.getString(R.string.rest) + ": " + formatTime(restTime).substring(3)
                )
                .setContentText(
                    appContext.getString(R.string.time) + ": " + formatTime(
                        timeInSeconds
                    )
                )
                .addAction(
                    R.drawable.ic_replay_10,
                    appContext.getString(R.string.reduce_ten_seconds),
                    createWorkoutServiceIntent(
                        WorkoutServiceActions.MODIFY_REST_TIMER.string,
                        false
                    )
                )
                .addAction(
                    R.drawable.ic_forward_10,
                    appContext.getString(R.string.add_ten_seconds),
                    createWorkoutServiceIntent(
                        WorkoutServiceActions.MODIFY_REST_TIMER.string,
                        true
                    )
                )
        } else {
            builder
                .setContentTitle(
                    appContext.getString(R.string.time) + ": " + formatTime(
                        timeInSeconds
                    )
                )
                .setContentText("")
        }




        workoutNotification = builder.build()

        notificationManager.notify(WORKOUT_NOTIFICATION_ID, workoutNotification)
    }


    private lateinit var timerNotification: Notification

    fun notifyTimerIsOver() {
        notificationManager.notify(TIMER_NOTIFICATION_ID, timerNotification)
    }


    private fun createWorkoutServiceIntent(
        action: String,
        addTenSeconds: Boolean? = null
    ): PendingIntent {
        val intent = Intent(appContext, WorkoutService::class.java).apply {
            this.action = action
            if (addTenSeconds != null) {
                putExtra(EXTRA_ADD_TEN_SECONDS, addTenSeconds)
            }
        }
        return PendingIntent.getService(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}