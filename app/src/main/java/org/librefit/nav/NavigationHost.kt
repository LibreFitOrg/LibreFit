/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.nav

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.librefit.enums.userPreferences.UnitSystem
import org.librefit.ui.screens.MainScreen
import org.librefit.ui.screens.about.AboutScreen
import org.librefit.ui.screens.about.DependenciesScreen
import org.librefit.ui.screens.about.LicenseScreen
import org.librefit.ui.screens.about.PrivacyScreen
import org.librefit.ui.screens.about.TutorialScreen
import org.librefit.ui.screens.about.WelcomeScreen
import org.librefit.ui.screens.beforeSaving.BeforeSavingScreen
import org.librefit.ui.screens.calendar.CalendarScreen
import org.librefit.ui.screens.editExercise.EditExerciseScreen
import org.librefit.ui.screens.editWorkout.EditWorkoutScreen
import org.librefit.ui.screens.exercises.ExercisesScreen
import org.librefit.ui.screens.infoExercise.InfoExerciseScreen
import org.librefit.ui.screens.infoWorkout.InfoWorkoutScreen
import org.librefit.ui.screens.measurements.MeasurementScreen
import org.librefit.ui.screens.settings.SettingsScreen
import org.librefit.ui.screens.shared.RequestPermissionScreen
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.screens.shared.SuccessScreen
import org.librefit.ui.screens.shared.SupportScreen
import org.librefit.ui.screens.statistics.StatisticsScreen
import org.librefit.ui.screens.workout.WorkoutScreen

val LocalUnitSystem = compositionLocalOf { UnitSystem.METRIC }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationHost(
    sharedViewModel: SharedViewModel = hiltViewModel()
) {

    val navController = rememberNavController()

    val unitSystem by sharedViewModel.unitSystem.collectAsStateWithLifecycle()

    val showWelcomeScreen by sharedViewModel.showWelcomeScreen.collectAsStateWithLifecycle()

    val requestPermissionNextTime by sharedViewModel.requestPermissionNextTime.collectAsStateWithLifecycle()

    val isSupporter by sharedViewModel.isSupporter.collectAsStateWithLifecycle()

    val startDestination = remember {
        if (showWelcomeScreen) Route.WelcomeScreen else Route.MainScreen
    }

    CompositionLocalProvider(LocalUnitSystem provides unitSystem) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = { scaleIn(tween(300), 0.9f) + fadeIn(tween(200)) },
                exitTransition = { scaleOut(tween(300), 1.1f) },
                popEnterTransition = { scaleIn(tween(300), 1.1f) },
                popExitTransition = { scaleOut(tween(300), 0.9f) + fadeOut(tween(200)) }
            ) {
                composable<Route.AboutScreen> {
                    AboutScreen(navController = navController)
                }
                composable<Route.BeforeSavingScreen> {
                    BeforeSavingScreen(
                        navController = navController,
                        animatedVisibilityScope = this
                    )
                }
                composable<Route.CalendarScreen> {
                    CalendarScreen(
                        navController = navController,
                        animatedVisibilityScope = this
                    )
                }
                composable<Route.EditExerciseScreen> {
                    EditExerciseScreen(
                        navController = navController,
                        id = it.toRoute<Route.EditExerciseScreen>().id,
                        exerciseDCid = it.toRoute<Route.EditExerciseScreen>().exerciseDCid,
                        animatedVisibilityScope = this
                    )
                }
                composable<Route.EditWorkoutScreen> {
                    EditWorkoutScreen(
                        sharedViewModel = sharedViewModel,
                        navController = navController,
                        animatedVisibilityScope = this
                    )
                }
                composable<Route.ExercisesScreen> {
                    ExercisesScreen(
                        addExercises = it.toRoute<Route.ExercisesScreen>().addExercises,
                        navController = navController,
                        sharedViewModel = sharedViewModel,
                        animatedVisibilityScope = this
                    )
                }
                composable<Route.InfoExerciseScreen> {
                    InfoExerciseScreen(
                        id = it.toRoute<Route.InfoExerciseScreen>().id,
                        animatedVisibilityScope = this,
                        navController = navController
                    )
                }
                composable<Route.InfoWorkoutScreen> {
                    InfoWorkoutScreen(
                        animatedVisibilityScope = this,
                        navController = navController,
                        workoutId = it.toRoute<Route.InfoWorkoutScreen>().workoutId,
                    )
                }
                composable<Route.MainScreen> {
                    MainScreen(
                        navController = navController,
                        animatedVisibilityScope = this
                    )
                }
                composable<Route.MeasurementScreen> {
                    MeasurementScreen(navigateBack = navController::navigateUp)
                }
                composable<Route.PrivacyScreen> {
                    PrivacyScreen(navigateBack = navController::navigateUp)
                }
                composable<Route.LibrariesScreen> {
                    DependenciesScreen(navigateBack = navController::navigateUp)
                }
                composable<Route.LicenseScreen> {
                    LicenseScreen(navigateBack = navController::navigateUp)
                }
                composable<Route.RequestPermissionScreen> {
                    RequestPermissionScreen(
                        navController = navController,
                        workoutId = it.toRoute<Route.RequestPermissionScreen>().workoutId,
                        requestPermissionNextTime = requestPermissionNextTime,
                        saveRequestPermissionAgainPreference = sharedViewModel::saveRequestPermissionAgainPreference
                    )
                }
                composable<Route.SettingsScreen> {
                    SettingsScreen(navController = navController)
                }
                composable<Route.SuccessScreen> {
                    SuccessScreen(
                        message = it.toRoute<Route.SuccessScreen>().message,
                        navController = navController
                    )
                }
                composable<Route.SupportScreen> {
                    SupportScreen(
                        navHostController = navController,
                        supporterInfo = it.toRoute<Route.SupportScreen>().supporterInfo,
                        isSupporter = isSupporter,
                        updateIsSupporter = sharedViewModel::updateIsSupporter
                    )
                }
                composable<Route.StatisticsScreen> {
                    StatisticsScreen(navController = navController)
                }
                composable<Route.TutorialScreen> {
                    TutorialScreen(
                        tutorialContent = it.toRoute<Route.TutorialScreen>().tutorialContent,
                        fromWelcomeScreen = it.toRoute<Route.TutorialScreen>().fromWelcomeScreen,
                        navController = navController
                    )
                }
                composable<Route.WelcomeScreen> {
                    WelcomeScreen(
                        navController = navController,
                        doNotShowWelcomeScreenAgain = sharedViewModel::doNotShowWelcomeScreenAgain
                    )
                }
                composable<Route.WorkoutScreen> {
                    WorkoutScreen(
                        navController = navController,
                        sharedViewModel = sharedViewModel,
                        animatedVisibilityScope = this
                    )
                }
            }
        }
    }

}