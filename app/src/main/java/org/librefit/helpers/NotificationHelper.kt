/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import org.librefit.R
import org.librefit.activities.MainActivity
import org.librefit.enums.WorkoutServiceActions
import org.librefit.services.WorkoutService
import org.librefit.services.WorkoutService.Companion.EXTRA_ADD_TEN_SECONDS
import org.librefit.util.Formatter.formatTime
import javax.inject.Inject
import javax.inject.Singleton


/**
 * A helper class for managing notifications during workout sessions.
 *
 * This class is responsible for creating and displaying notifications for ongoing workouts
 * and timer events. It sets up notification channels, builds notification content, and
 * handles user interactions through actions in the notifications.
 *
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext context: Context
) {
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
            .setSmallIcon(R.drawable.ic_notification_monochrome)
            .setColor(appContext.getColor(R.color.md_theme_primary))
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)


        timerNotificationBuilder
            .setSmallIcon(R.drawable.ic_notification_monochrome)
            .setColor(appContext.getColor(R.color.md_theme_primary))
            .setContentTitle(appContext.getString(R.string.rest_time_is_over))
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)

        timerNotification = timerNotificationBuilder.build()
    }


    private fun createNotificationChannels() {
        val workoutChannel = NotificationChannel(
            WORKOUT_CHANNEL_ID,
            appContext.getString(R.string.workout_info_channel_title),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = appContext.getString(R.string.workout_info_channel_desc)
        }

        workoutChannel.setSound(
            "android.resource://${appContext.packageName}/raw/system_notification".toUri(),
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
        )

        notificationManager.createNotificationChannel(workoutChannel)


        val timerChannel = NotificationChannel(
            TIMER_CHANNEL_ID,
            appContext.getString(R.string.rest_timer_channel_title),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = appContext.getString(R.string.rest_timer_channel_desc)
        }

        timerChannel.setSound(
            "android.resource://${appContext.packageName}/raw/alert_notification".toUri(),
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()
        )

        notificationManager.createNotificationChannel(timerChannel)
    }

    fun createWorkoutNotification(): Notification {
        workoutNotification = workoutNotificationBuilder.build()
        return workoutNotification
    }

    fun notifyOngoingWorkout(
        timeInSeconds: Int,
        isStopwatchPaused: Boolean,
        restTime: Int = 0,
        initialRestTime: Int = 0,
    ) {
        val builder = workoutNotificationBuilder

        builder
            .setProgress(initialRestTime, restTime - 1, false)
            .clearActions()

        if (isStopwatchPaused) {
            builder
                .addAction(
                    R.drawable.ic_play_arrow,
                    appContext.getString(R.string.resume),
                    createWorkoutServiceIntent(WorkoutServiceActions.START_STOPWATCH.string)
                )
        } else {
            builder
                .addAction(
                    R.drawable.ic_pause,
                    appContext.getString(R.string.pause),
                    createWorkoutServiceIntent(WorkoutServiceActions.PAUSE_STOPWATCH.string)
                )
        }


        if (restTime != 0 && initialRestTime != 0) {
            builder
                .setContentTitle(
                    appContext.getString(R.string.rest) + ": " + formatTime(restTime - 1).substring(
                        3
                    )
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
                    appContext.getString(R.string.elapsed_time) + ": " + formatTime(
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

    fun cancelTimerIsOverNotification() {
        notificationManager.cancel(TIMER_NOTIFICATION_ID)
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