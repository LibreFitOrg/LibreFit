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
import org.librefit.enums.SuccessMessage
import org.librefit.enums.pages.TutorialContent
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
                popExitTransition = { scaleOut(tween(300), 0.9f) + fadeOut(tween(200)) },
                predictivePopEnterTransition = { scaleIn(tween(300), 1.1f) },
                predictivePopExitTransition = { scaleOut(tween(300), 0.9f) + fadeOut(tween(200)) }
            ) {
                composable<Route.AboutScreen> {
                    AboutScreen(
                        onNavigateBack = navController::navigateUp,
                        onNavigateToSupportScreen = {
                            navController.navigate(Route.SupportScreen()) { launchSingleTop = true }
                        },
                        onNavigateToTutorialScreen = {
                            navController.navigate(Route.TutorialScreen()) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToPrivacyScreen = {
                            navController.navigate(Route.PrivacyScreen) { launchSingleTop = true }
                        },
                        onNavigateToLicenseScreen = {
                            navController.navigate(Route.LicenseScreen) { launchSingleTop = true }
                        },
                        onNavigateToDependenciesScreen = {
                            navController.navigate(Route.DependenciesScreen) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<Route.BeforeSavingScreen> {
                    BeforeSavingScreen(
                        onNavigateBack = navController::navigateUp,
                        onNavigateToInfoWorkout = { workoutId ->
                            navController.navigate(Route.InfoWorkoutScreen(workoutId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSuccessScreen = {
                            navController.navigate(Route.SuccessScreen(SuccessMessage.WORKOUT_SAVED)) {
                                launchSingleTop = true
                                popUpTo(Route.MainScreen) { inclusive = false }
                            }
                        },
                        animatedVisibilityScope = this
                    )
                }
                composable<Route.CalendarScreen> {
                    CalendarScreen(
                        onNavigateBack = navController::navigateUp,
                        onNavigateToInfoWorkout = { workoutId ->
                            navController.navigate(Route.InfoWorkoutScreen(workoutId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToTutorialScreen = {
                            navController.navigate(
                                Route.TutorialScreen(TutorialContent.COMPLETE_WORKOUT)
                            ) {
                                launchSingleTop = true
                            }
                        },
                        animatedVisibilityScope = this
                    )
                }
                composable<Route.EditExerciseScreen> {
                    val route = it.toRoute<Route.EditExerciseScreen>()
                    EditExerciseScreen(
                        animatedVisibilityScope = this,
                        id = route.id,
                        onNavigateBack = navController::navigateUp,
                        onNavigateToSuccessScreen = {
                            navController.navigate(
                                Route.SuccessScreen(SuccessMessage.EXERCISE_SAVED)
                            ) {
                                launchSingleTop = true
                                popUpTo(route) { inclusive = true }
                            }
                        }
                    )
                }
                composable<Route.EditWorkoutScreen> {
                    EditWorkoutScreen(
                        sharedViewModel = sharedViewModel,
                        onNavigateBack = navController::navigateUp,
                        onNavigateToInfoExercise = { id, exerciseDCid ->
                            navController.navigate(Route.InfoExerciseScreen(id, exerciseDCid)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAddExercises = {
                            navController.navigate(Route.ExercisesScreen(addExercises = true)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToBeforeSavingScreen = { workoutId ->
                            navController.navigate(Route.BeforeSavingScreen(workoutId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSuccessScreen = {
                            navController.navigate(Route.SuccessScreen(SuccessMessage.ROUTINE_SAVED)) {
                                launchSingleTop = true
                                popUpTo(Route.MainScreen) { inclusive = false }
                            }
                        },
                        animatedVisibilityScope = this
                    )
                }
                composable<Route.ExercisesScreen> {
                    ExercisesScreen(
                        addExercises = it.toRoute<Route.ExercisesScreen>().addExercises,
                        sharedViewModel = sharedViewModel,
                        onNavigateBack = navController::navigateUp,
                        onNavigateToInfoExercise = { exerciseDC ->
                            navController.navigate(Route.InfoExerciseScreen(0L, exerciseDC.id)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToEditExercise = {
                            navController.navigate(Route.EditExerciseScreen()) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSupportScreen = {
                            navController.navigate(Route.SupportScreen(supporterInfo = true)) {
                                launchSingleTop = true
                            }
                        },
                        animatedVisibilityScope = this
                    )
                }
                composable<Route.InfoExerciseScreen> {
                    val route = it.toRoute<Route.InfoExerciseScreen>()
                    InfoExerciseScreen(
                        id = route.id,
                        animatedVisibilityScope = this,
                        onNavigateBack = navController::navigateUp,
                        onNavigateToEditExercise = { exerciseDCid ->
                            navController.navigate(
                                Route.EditExerciseScreen(
                                    id = route.id,
                                    exerciseDCid = exerciseDCid
                                )
                            ) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToInfoWorkout = { workoutId ->
                            navController.navigate(Route.InfoWorkoutScreen(workoutId))
                        }
                    )
                }
                composable<Route.InfoWorkoutScreen> {
                    InfoWorkoutScreen(
                        workoutId = it.toRoute<Route.InfoWorkoutScreen>().workoutId,
                        animatedVisibilityScope = this,
                        onNavigateBack = navController::navigateUp,
                        onNavigateToEditWorkout = { workoutId ->
                            navController.navigate(Route.EditWorkoutScreen(workoutId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToInfoWorkout = { workoutId ->
                            navController.navigate(Route.InfoWorkoutScreen(workoutId))
                        },
                        onNavigateToInfoExercise = { id, exerciseDCid ->
                            navController.navigate(Route.InfoExerciseScreen(id, exerciseDCid)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<Route.MainScreen> {
                    MainScreen(
                        onNavigateToSupportScreen = {
                            navController.navigate(Route.SupportScreen()) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAboutScreen = {
                            navController.navigate(Route.AboutScreen) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSettingsScreen = {
                            navController.navigate(Route.SettingsScreen) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToEditWorkout = {
                            navController.navigate(Route.EditWorkoutScreen(0L)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToInfoWorkout = { workoutId ->
                            navController.navigate(Route.InfoWorkoutScreen(workoutId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToRequestPermissionScreen = { workoutId ->
                            navController.navigate(Route.RequestPermissionScreen(workoutId)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToWorkout = { workoutId ->
                            navController.navigate(Route.WorkoutScreen(workoutId)) {
                                launchSingleTop = true
                                popUpTo(Route.RequestPermissionScreen(workoutId)) {
                                    inclusive = true
                                }
                            }
                        },
                        onNavigateToTutorialScreen = {
                            navController.navigate(Route.TutorialScreen()) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCompleteWorkoutTutorial = {
                            navController.navigate(
                                Route.TutorialScreen(TutorialContent.COMPLETE_WORKOUT)
                            ) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToExercisesScreen = {
                            navController.navigate(Route.ExercisesScreen(addExercises = false)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToStatisticsScreen = {
                            navController.navigate(Route.StatisticsScreen) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToMeasurementsScreen = {
                            navController.navigate(Route.MeasurementScreen) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCalendarScreen = {
                            navController.navigate(Route.CalendarScreen) {
                                launchSingleTop = true
                            }
                        },
                        animatedVisibilityScope = this
                    )
                }
                composable<Route.MeasurementScreen> {
                    MeasurementScreen(navigateBack = navController::navigateUp)
                }
                composable<Route.PrivacyScreen> {
                    PrivacyScreen(navigateBack = navController::navigateUp)
                }
                composable<Route.DependenciesScreen> {
                    DependenciesScreen(navigateBack = navController::navigateUp)
                }
                composable<Route.LicenseScreen> {
                    LicenseScreen(navigateBack = navController::navigateUp)
                }
                composable<Route.RequestPermissionScreen> {
                    val route = it.toRoute<Route.RequestPermissionScreen>()
                    RequestPermissionScreen(
                        onNavigateBack = navController::navigateUp,
                        onNavigateToWorkoutScreen = {
                            navController.navigate(Route.WorkoutScreen(workoutId = route.workoutId)) {
                                launchSingleTop = true
                                popUpTo(Route.RequestPermissionScreen(workoutId = route.workoutId)) {
                                    inclusive = true
                                }
                            }
                        },
                        requestPermissionNextTime = requestPermissionNextTime,
                        saveRequestPermissionAgainPreference = sharedViewModel::saveRequestPermissionAgainPreference
                    )
                }
                composable<Route.SettingsScreen> {
                    SettingsScreen(
                        onNavigateBack = navController::navigateUp,
                        onNavigateToSupportScreen = {
                            navController.navigate(Route.SupportScreen(supporterInfo = true)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<Route.SuccessScreen> {
                    SuccessScreen(
                        message = it.toRoute<Route.SuccessScreen>().message,
                        onNavigateBack = navController::navigateUp,
                        onNavigateToSupportScreen = {
                            navController.navigate(Route.SupportScreen()) {
                                launchSingleTop = true
                                popUpTo(Route.MainScreen)
                            }
                        }
                    )
                }
                composable<Route.SupportScreen> {
                    SupportScreen(
                        onNavigateBack = navController::navigateUp,
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