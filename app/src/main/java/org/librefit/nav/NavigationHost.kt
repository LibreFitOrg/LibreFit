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

package org.librefit.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.librefit.data.DataStoreManager
import org.librefit.ui.screens.MainScreen
import org.librefit.ui.screens.about.AboutScreen
import org.librefit.ui.screens.about.LicenseScreen
import org.librefit.ui.screens.createRoutine.CreateRoutineScreen
import org.librefit.ui.screens.infoRoutine.InfoRoutineScreen
import org.librefit.ui.screens.settings.SettingsScreen
import org.librefit.ui.screens.shared.AddExerciseScreen
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.screens.shared.SuccessScreen
import org.librefit.ui.screens.workout.RequestPermissionsScreen
import org.librefit.ui.screens.workout.WorkoutScreen
import org.librefit.ui.screens.workout.beforeSaving.BeforeSavingScreen

@Composable
fun NavigationHost(userPreferences: DataStoreManager) {

    val navController = rememberNavController()

    val sharedViewModel: SharedViewModel = viewModel()


    NavHost(
        navController = navController,
        startDestination = Destination.MainScreen,
        enterTransition = { scaleIn(tween(300), 0.8f) + fadeIn(tween(200)) },
        exitTransition = { scaleOut(tween(300), 1.2f) },
        popEnterTransition = { scaleIn(tween(300), 1.2f) },
        popExitTransition = { scaleOut(tween(300), 0.8f) + fadeOut(tween(200)) }
    ) {
        composable<Destination.AboutScreen> {
            AboutScreen(navController = navController)
        }
        composable<Destination.AddExerciseScreen> {
            AddExerciseScreen(
                navigateBack = { navController.popBackStack() },
                viewModel = sharedViewModel
            )
        }
        composable<Destination.BeforeSavingScreen> {
            BeforeSavingScreen(
                sharedViewModel = sharedViewModel,
                navController = navController
            )
        }
        composable<Destination.CreateRoutineScreen> {
            CreateRoutineScreen(
                sharedViewModel = sharedViewModel,
                navController = navController
            )
        }
        composable<Destination.InfoRoutineScreen> {
            InfoRoutineScreen(
                workoutId = it.toRoute<Destination.InfoRoutineScreen>().workoutId,
                workoutTitle = it.toRoute<Destination.InfoRoutineScreen>().workoutTitle,
                navController = navController
            )
        }
        composable<Destination.MainScreen> {
            MainScreen(
                navController = navController,
                userPreferences = userPreferences
            )
        }
        composable<Destination.LicenseScreen> {
            LicenseScreen(navigateBack = { navController.popBackStack() })
        }
        composable<Destination.RequestPermissionsScreen> {
            RequestPermissionsScreen(
                userPreferences = userPreferences,
                workoutId = it.toRoute<Destination.RequestPermissionsScreen>().workoutId,
                workoutTitle = it.toRoute<Destination.RequestPermissionsScreen>().workoutTitle,
                navController = navController
            )
        }
        composable<Destination.SettingsScreen> {
            SettingsScreen(
                navigateBack = { navController.popBackStack() },
                userPreferences = userPreferences
            )
        }
        composable<Destination.SuccessScreen> {
            SuccessScreen(
                message = it.toRoute<Destination.SuccessScreen>().message,
                navigateBack = { navController.popBackStack() }
            )
        }
        composable<Destination.WorkoutScreen> {
            WorkoutScreen(
                userPreferences = userPreferences,
                workoutId = it.toRoute<Destination.WorkoutScreen>().workoutId,
                workoutTitle = it.toRoute<Destination.WorkoutScreen>().workoutTitle,
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
    }
}