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
import org.librefit.ui.screens.MainScreen
import org.librefit.ui.screens.about.AboutScreen
import org.librefit.ui.screens.about.LicenseScreen
import org.librefit.ui.screens.beforeSaving.BeforeSavingScreen
import org.librefit.ui.screens.calendar.CalendarScreen
import org.librefit.ui.screens.editWorkout.EditWorkoutScreen
import org.librefit.ui.screens.exercises.ExercisesScreen
import org.librefit.ui.screens.infoWorkout.InfoWorkoutScreen
import org.librefit.ui.screens.requestPermission.RequestPermissionScreen
import org.librefit.ui.screens.settings.SettingsScreen
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.screens.shared.SuccessScreen
import org.librefit.ui.screens.workout.WorkoutScreen

@Composable
fun NavigationHost() {

    val navController = rememberNavController()

    val sharedViewModel: SharedViewModel = viewModel()

    //TODO: create tutorial screen

    NavHost(
        navController = navController,
        startDestination = Destination.MainScreen,
        enterTransition = { scaleIn(tween(200), 0.9f) + fadeIn(tween(100)) },
        exitTransition = { scaleOut(tween(200), 1.1f) },
        popEnterTransition = { scaleIn(tween(200), 1.1f) },
        popExitTransition = { scaleOut(tween(200), 0.9f) + fadeOut(tween(100)) }
    ) {
        composable<Destination.AboutScreen> {
            AboutScreen(navController = navController)
        }
        composable<Destination.BeforeSavingScreen> {
            BeforeSavingScreen(
                sharedViewModel = sharedViewModel,
                navController = navController
            )
        }
        composable<Destination.CalendarScreen> {
            CalendarScreen(
                sharedViewModel = sharedViewModel,
                navController = navController
            )
        }
        composable<Destination.EditWorkoutScreen> {
            EditWorkoutScreen(
                sharedViewModel = sharedViewModel,
                navController = navController
            )
        }
        composable<Destination.ExercisesScreen> {
            ExercisesScreen(
                addExercises = it.toRoute<Destination.ExercisesScreen>().addExercises,
                navigateBack = { navController.popBackStack() },
                sharedViewModel = sharedViewModel
            )
        }
        composable<Destination.InfoWorkoutScreen> {
            InfoWorkoutScreen(
                sharedViewModel = sharedViewModel,
                navController = navController
            )
        }
        composable<Destination.MainScreen> {
            MainScreen(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable<Destination.LicenseScreen> {
            LicenseScreen(navigateBack = { navController.popBackStack() })
        }
        composable<Destination.RequestPermissionScreen> {
            RequestPermissionScreen(
                navController = navController
            )
        }
        composable<Destination.SettingsScreen> {
            SettingsScreen(
                navController = navController
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
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
    }
}