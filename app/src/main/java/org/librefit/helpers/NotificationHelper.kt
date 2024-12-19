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
import org.librefit.util.formatTime


class NotificationHelper(context: Context) {
    companion object {
        const val WORKOUT_CHANNEL_ID = "workout_channel"
        const val WORKOUT_NOTIFICATION_ID = 1001

        const val ALARM_CHANNEL_ID = "alarm_channel"
        const val ALARM_NOTIFICATION_ID = 1002
    }

    private val appContext = context.applicationContext


    private val workoutNotificationBuilder =
        NotificationCompat.Builder(appContext, WORKOUT_CHANNEL_ID)

    private val alarmNotificationBuilder = NotificationCompat.Builder(appContext, ALARM_CHANNEL_ID)


    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private lateinit var workoutNotification: Notification

    init {
        createNotificationChannels()
        initializeNotificationBuilders()
    }

    private fun initializeNotificationBuilders() {
        workoutNotificationBuilder
            //TODO: remove hardcoded strings
            .setSmallIcon(R.drawable.ic_logo_monochrome)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(
                PendingIntent.getActivity(
                    appContext,
                    0,
                    Intent(appContext, MainActivity::class.java).apply {
                        // This flags allow to open an already running MainActivity (if any)
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)


        alarmNotificationBuilder
            .setSmallIcon(R.drawable.ic_logo_monochrome)
            .setContentTitle("Rest time is over!")
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(
                PendingIntent.getActivity(
                    appContext,
                    0,
                    Intent(appContext, MainActivity::class.java).apply {
                        // This flags allow to open an already running MainActivity (if any)
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        alarmNotification = alarmNotificationBuilder.build()
    }


    private fun createNotificationChannels() {
        //TODO: remove hardcoded strings
        notificationManager.createNotificationChannel(
            NotificationChannel(
                WORKOUT_CHANNEL_ID,
                "Workout info",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "This notification channel will be used to display information about " +
                            "ongoing workouts"
            }
        )


        notificationManager.createNotificationChannel(
            NotificationChannel(
                ALARM_CHANNEL_ID,
                "Rest alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "This notification channel will be used to send alarms " +
                        "when rest time is over"
            }
        )
    }

    fun createWorkoutNotification(): Notification {
        workoutNotification = workoutNotificationBuilder.build()
        return workoutNotification
    }

    fun notifyWorkout(timeInSeconds: Int, isChronometerPaused: Boolean) {
        workoutNotificationBuilder
            .setContentTitle(
                appContext.getString(R.string.label_elapsed_time)
                        + ": " + formatTime(timeInSeconds)
            )
            .setContentText("TODO: Now doing X or now resting for Y")

        workoutNotificationBuilder.clearActions()

        workoutNotification =
            if (isChronometerPaused) {
                workoutNotificationBuilder
                    .addAction(
                        android.R.drawable.ic_media_play,
                        "Resume",
                        createPendingIntent(WorkoutServiceActions.START_CHRONOMETER.string)
                    )
            } else {
                workoutNotificationBuilder
                    .addAction(
                        R.drawable.ic_pause,
                        "Pause",
                        createPendingIntent(WorkoutServiceActions.PAUSE_CHRONOMETER.string)
                    )
            }.build()

        notificationManager.notify(WORKOUT_NOTIFICATION_ID, workoutNotification)
    }


    private lateinit var alarmNotification: Notification

    fun notifyAlarm() {
        notificationManager.notify(ALARM_NOTIFICATION_ID, alarmNotification)
    }
    

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(appContext, WorkoutService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}