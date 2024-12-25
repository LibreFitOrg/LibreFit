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

package org.librefit.nav

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController

/**
 * Navigates to the [org.librefit.ui.screens.workout.WorkoutScreen] or [org.librefit.ui.screens.workout.RequestPermissionsScreen]
 * based on the granted permissions.
 *
 * @param workoutId The ID of the [org.librefit.db.Workout] to open in [org.librefit.ui.screens.workout.WorkoutScreen].
 * @param title The title of the workout.
 * @param requestPermissionAgain Flag indicating whether to request permissions again (default is false).
 * @param navController The NavHostController used for navigation.
 *
 * If the user lacks [android.Manifest.permission.POST_NOTIFICATIONS] and [requestPermissionAgain]
 * is `true`,the method navigates to the permission request screen. Otherwise, it navigates to the
 * workout screen, popping the permission request screen from the back stack if it exists.
 */
fun checkPermissionsBeforeNavigateToWorkout(
    workoutId: Int = 0,
    title: String = "",
    requestPermissionAgain: Boolean = false,
    navController: NavHostController,
    appContext: Context
) {

    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.checkSelfPermission(
            appContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // For Android versions below TIRAMISU, notifications are allowed by default
        true
    }

    val requestPermission = !hasNotificationPermission && requestPermissionAgain

    if (requestPermission) {
        navController.navigate(Destination.RequestPermissionsScreen(workoutId, title))
    } else {
        navController.navigate(Destination.WorkoutScreen(workoutId, title)) {
            popUpTo(Destination.RequestPermissionsScreen(workoutId, title)) { inclusive = true }
        }
    }
}

