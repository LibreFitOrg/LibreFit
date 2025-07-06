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
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.ui.screens.MainScreen
import org.librefit.ui.screens.about.AboutScreen
import org.librefit.ui.screens.about.LibrariesScreen
import org.librefit.ui.screens.about.LicenseScreen
import org.librefit.ui.screens.about.PrivacyScreen
import org.librefit.ui.screens.beforeSaving.BeforeSavingScreen
import org.librefit.ui.screens.calendar.CalendarScreen
import org.librefit.ui.screens.editWorkout.EditWorkoutScreen
import org.librefit.ui.screens.exercises.ExercisesScreen
import org.librefit.ui.screens.infoWorkout.InfoWorkoutScreen
import org.librefit.ui.screens.measurements.MeasurementScreen
import org.librefit.ui.screens.requestPermission.RequestPermissionScreen
import org.librefit.ui.screens.settings.SettingsScreen
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.screens.shared.SuccessScreen
import org.librefit.ui.screens.workout.WorkoutScreen
import kotlin.reflect.typeOf

@Composable
fun NavigationHost() {

    val navController = rememberNavController()

    val sharedViewModel: SharedViewModel = viewModel()

    //TODO: create tutorial screen

    NavHost(
        navController = navController,
        startDestination = Route.MainScreen,
        enterTransition = { scaleIn(tween(300), 0.9f) + fadeIn(tween(200)) },
        exitTransition = { scaleOut(tween(300), 1.1f) },
        popEnterTransition = { scaleIn(tween(300), 1.1f) },
        popExitTransition = { scaleOut(tween(300), 0.9f) + fadeOut(tween(200)) }
    ) {
        composable<Route.AboutScreen> {
            AboutScreen(navController = navController)
        }
        composable<Route.BeforeSavingScreen>(
            typeMap = mapOf(
                typeOf<WorkoutWithExercisesAndSets>() to WorkoutWithExercisesAndSetsNavType()
            )
        ) {
            BeforeSavingScreen(
                navController = navController
            )
        }
        composable<Route.CalendarScreen> {
            CalendarScreen(
                navController = navController
            )
        }
        composable<Route.EditWorkoutScreen> {
            EditWorkoutScreen(
                sharedViewModel = sharedViewModel,
                navController = navController,
                workoutId = it.toRoute<Route.EditWorkoutScreen>().workoutId
            )
        }
        composable<Route.ExercisesScreen> {
            ExercisesScreen(
                addExercises = it.toRoute<Route.ExercisesScreen>().addExercises,
                navigateBack = navController::popBackStack,
                sharedViewModel = sharedViewModel
            )
        }
        composable<Route.InfoWorkoutScreen> {
            InfoWorkoutScreen(
                navController = navController,
                workoutId = it.toRoute<Route.InfoWorkoutScreen>().workoutId
            )
        }
        composable<Route.MainScreen> {
            MainScreen(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable<Route.MeasurementScreen> {
            MeasurementScreen(navigateBack = navController::popBackStack)
        }
        composable<Route.PrivacyScreen> {
            PrivacyScreen { navController.popBackStack() }
        }
        composable<Route.LibrariesScreen> {
            LibrariesScreen(navigateBack = navController::popBackStack)
        }
        composable<Route.LicenseScreen> {
            LicenseScreen(navigateBack = navController::popBackStack)
        }
        composable<Route.RequestPermissionScreen> {
            RequestPermissionScreen(
                navController = navController
            )
        }
        composable<Route.SettingsScreen> {
            SettingsScreen(
                navController = navController
            )
        }
        composable<Route.SuccessScreen> {
            SuccessScreen(
                message = it.toRoute<Route.SuccessScreen>().message,
                navigateBack = navController::popBackStack
            )
        }
        composable<Route.WorkoutScreen> {
            WorkoutScreen(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
    }
}