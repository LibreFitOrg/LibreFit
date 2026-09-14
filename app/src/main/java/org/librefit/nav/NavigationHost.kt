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
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
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

    val unitSystem by sharedViewModel.unitSystem.collectAsStateWithLifecycle()

    val showWelcomeScreen by sharedViewModel.showWelcomeScreen.collectAsStateWithLifecycle()

    val requestPermissionNextTime by sharedViewModel.requestPermissionNextTime.collectAsStateWithLifecycle()

    val isSupporter by sharedViewModel.isSupporter.collectAsStateWithLifecycle()

    val startDestination = remember {
        if (showWelcomeScreen) Route.WelcomeScreen else Route.MainScreen
    }

    val backStack = rememberNavBackStack(startDestination)

    CompositionLocalProvider(LocalUnitSystem provides unitSystem) {
        SharedTransitionLayout {
            NavDisplay(
                backStack = backStack,
                onBack = backStack::goBack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    // Gives each NavEntry its own ViewModelStore so hiltViewModel() inside
                    // screens is scoped (and cleared) per destination, matching Nav2 behavior.
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                sharedTransitionScope = this,
                transitionSpec = {
                    scaleIn(tween(300), 0.9f) + fadeIn(tween(200)) togetherWith
                            scaleOut(tween(300), 1.1f)
                },
                popTransitionSpec = {
                    scaleIn(tween(300), 1.1f) togetherWith
                            scaleOut(tween(300), 0.9f) + fadeOut(tween(200))
                },
                predictivePopTransitionSpec = {
                    scaleIn(tween(300), 1.1f) togetherWith
                            scaleOut(tween(300), 0.9f) + fadeOut(tween(200))
                },
                entryProvider = entryProvider {
                    entry<Route.AboutScreen> {
                        AboutScreen(
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToSupportScreen = dropUnlessResumed {
                                backStack.navigate(Route.SupportScreen())
                            },
                            onNavigateToTutorialScreen = dropUnlessResumed {
                                backStack.navigate(Route.TutorialScreen())
                            },
                            onNavigateToPrivacyScreen = dropUnlessResumed {
                                backStack.navigate(Route.PrivacyScreen)
                            },
                            onNavigateToLicenseScreen = dropUnlessResumed {
                                backStack.navigate(Route.LicenseScreen)
                            },
                            onNavigateToDependenciesScreen = dropUnlessResumed {
                                backStack.navigate(Route.DependenciesScreen)
                            }
                        )
                    }
                    entry<Route.BeforeSavingScreen> { route ->
                        BeforeSavingScreen(
                            route = route,
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToInfoWorkout = dropUnlessResumedNav { workoutId ->
                                backStack.navigate(Route.InfoWorkoutScreen(workoutId))
                            },
                            onNavigateToSuccessScreen = dropUnlessResumed {
                                backStack.navigate(
                                    Route.SuccessScreen(SuccessMessage.WORKOUT_SAVED),
                                    popUpTo = Route.MainScreen
                                )
                            },
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                    }
                    entry<Route.CalendarScreen> {
                        CalendarScreen(
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToInfoWorkout = dropUnlessResumedNav { workoutId ->
                                backStack.navigate(Route.InfoWorkoutScreen(workoutId))
                            },
                            onNavigateToTutorialScreen = dropUnlessResumed {
                                backStack.navigate(
                                    Route.TutorialScreen(TutorialContent.COMPLETE_WORKOUT)
                                )
                            },
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                    }
                    entry<Route.EditExerciseScreen> { key ->
                        EditExerciseScreen(
                            route = key,
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                            id = key.id,
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToSuccessScreen = dropUnlessResumed {
                                backStack.navigate(
                                    Route.SuccessScreen(SuccessMessage.EXERCISE_SAVED),
                                    popUpTo = key,
                                    popUpToInclusive = true
                                )
                            }
                        )
                    }
                    entry<Route.EditWorkoutScreen> { route ->
                        EditWorkoutScreen(
                            route = route,
                            sharedViewModel = sharedViewModel,
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToInfoExercise = dropUnlessResumedNav { id, exerciseDCid ->
                                backStack.navigate(Route.InfoExerciseScreen(id, exerciseDCid))
                            },
                            onNavigateToAddExercises = dropUnlessResumed {
                                backStack.navigate(Route.ExercisesScreen(addExercises = true))
                            },
                            onNavigateToBeforeSavingScreen = dropUnlessResumedNav { workoutId ->
                                backStack.navigate(Route.BeforeSavingScreen(workoutId))
                            },
                            onNavigateToSuccessScreen = dropUnlessResumed {
                                backStack.navigate(
                                    Route.SuccessScreen(SuccessMessage.ROUTINE_SAVED),
                                    popUpTo = Route.MainScreen
                                )
                            },
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                    }
                    entry<Route.ExercisesScreen> { key ->
                        ExercisesScreen(
                            addExercises = key.addExercises,
                            sharedViewModel = sharedViewModel,
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToInfoExercise = dropUnlessResumedNav { exerciseDC ->
                                backStack.navigate(Route.InfoExerciseScreen(0L, exerciseDC.id))
                            },
                            onNavigateToEditExercise = dropUnlessResumed {
                                backStack.navigate(Route.EditExerciseScreen())
                            },
                            onNavigateToSupportScreen = dropUnlessResumed {
                                backStack.navigate(Route.SupportScreen(supporterInfo = true))
                            },
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                    }
                    entry<Route.InfoExerciseScreen> { key ->
                        InfoExerciseScreen(
                            route = key,
                            id = key.id,
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToEditExercise = dropUnlessResumedNav { exerciseDCid ->
                                backStack.navigate(
                                    Route.EditExerciseScreen(
                                        id = key.id,
                                        exerciseDCid = exerciseDCid
                                    )
                                )
                            },
                            onNavigateToInfoWorkout = dropUnlessResumedNav { workoutId ->
                                backStack.navigate(Route.InfoWorkoutScreen(workoutId))
                            }
                        )
                    }
                    entry<Route.InfoWorkoutScreen> { key ->
                        InfoWorkoutScreen(
                            route = key,
                            workoutId = key.workoutId,
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToEditWorkout = dropUnlessResumedNav { workoutId ->
                                backStack.navigate(Route.EditWorkoutScreen(workoutId))
                            },
                            onNavigateToInfoWorkout = dropUnlessResumedNav { workoutId ->
                                backStack.navigate(Route.InfoWorkoutScreen(workoutId))
                            },
                            onNavigateToInfoExercise = dropUnlessResumedNav { id, exerciseDCid ->
                                backStack.navigate(Route.InfoExerciseScreen(id, exerciseDCid))
                            }
                        )
                    }
                    entry<Route.MainScreen> {
                        MainScreen(
                            onNavigateToSupportScreen = dropUnlessResumed {
                                backStack.navigate(Route.SupportScreen())
                            },
                            onNavigateToAboutScreen = dropUnlessResumed {
                                backStack.navigate(Route.AboutScreen)
                            },
                            onNavigateToSettingsScreen = dropUnlessResumed {
                                backStack.navigate(Route.SettingsScreen)
                            },
                            onNavigateToEditWorkout = dropUnlessResumed {
                                backStack.navigate(Route.EditWorkoutScreen(0L))
                            },
                            onNavigateToInfoWorkout = dropUnlessResumedNav { workoutId ->
                                backStack.navigate(Route.InfoWorkoutScreen(workoutId))
                            },
                            onNavigateToRequestPermissionScreen = dropUnlessResumedNav { workoutId ->
                                backStack.navigate(Route.RequestPermissionScreen(workoutId))
                            },
                            onNavigateToWorkout = dropUnlessResumedNav { workoutId ->
                                backStack.navigate(
                                    Route.WorkoutScreen(workoutId),
                                    popUpTo = Route.RequestPermissionScreen(workoutId),
                                    popUpToInclusive = true
                                )
                            },
                            onNavigateToTutorialScreen = dropUnlessResumed {
                                backStack.navigate(Route.TutorialScreen())
                            },
                            onNavigateToCompleteWorkoutTutorial = dropUnlessResumed {
                                backStack.navigate(
                                    Route.TutorialScreen(TutorialContent.COMPLETE_WORKOUT)
                                )
                            },
                            onNavigateToExercisesScreen = dropUnlessResumed {
                                backStack.navigate(Route.ExercisesScreen(addExercises = false))
                            },
                            onNavigateToStatisticsScreen = dropUnlessResumed {
                                backStack.navigate(Route.StatisticsScreen)
                            },
                            onNavigateToMeasurementsScreen = dropUnlessResumed {
                                backStack.navigate(Route.MeasurementScreen)
                            },
                            onNavigateToCalendarScreen = dropUnlessResumed {
                                backStack.navigate(Route.CalendarScreen)
                            },
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                    }
                    entry<Route.MeasurementScreen> {
                        MeasurementScreen(navigateBack = dropUnlessResumed { backStack.goBack() })
                    }
                    entry<Route.PrivacyScreen> {
                        PrivacyScreen(navigateBack = dropUnlessResumed { backStack.goBack() })
                    }
                    entry<Route.DependenciesScreen> {
                        DependenciesScreen(navigateBack = dropUnlessResumed { backStack.goBack() })
                    }
                    entry<Route.LicenseScreen> {
                        LicenseScreen(navigateBack = dropUnlessResumed { backStack.goBack() })
                    }
                    entry<Route.RequestPermissionScreen> { key ->
                        RequestPermissionScreen(
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToWorkoutScreen = dropUnlessResumed {
                                backStack.navigate(
                                    Route.WorkoutScreen(workoutId = key.workoutId),
                                    popUpTo = key,
                                    popUpToInclusive = true
                                )
                            },
                            requestPermissionNextTime = requestPermissionNextTime,
                            saveRequestPermissionAgainPreference =
                                sharedViewModel::saveRequestPermissionAgainPreference
                        )
                    }
                    entry<Route.SettingsScreen> {
                        SettingsScreen(
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToSupportScreen = dropUnlessResumed {
                                backStack.navigate(Route.SupportScreen(supporterInfo = true))
                            }
                        )
                    }
                    entry<Route.SuccessScreen> { key ->
                        SuccessScreen(
                            message = key.message,
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToSupportScreen = dropUnlessResumed {
                                backStack.navigate(
                                    Route.SupportScreen(),
                                    popUpTo = Route.MainScreen
                                )
                            }
                        )
                    }
                    entry<Route.SupportScreen> { key ->
                        SupportScreen(
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            supporterInfo = key.supporterInfo,
                            isSupporter = isSupporter,
                            updateIsSupporter = sharedViewModel::updateIsSupporter
                        )
                    }
                    entry<Route.StatisticsScreen> {
                        StatisticsScreen(onNavigateBack = dropUnlessResumed { backStack.goBack() })
                    }
                    entry<Route.TutorialScreen> { key ->
                        TutorialScreen(
                            tutorialContent = key.tutorialContent,
                            fromWelcomeScreen = key.fromWelcomeScreen,
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToMainScreen = dropUnlessResumed {
                                backStack.navigate(
                                    Route.MainScreen,
                                    popUpTo = key,
                                    popUpToInclusive = true
                                )
                            }
                        )
                    }
                    entry<Route.WelcomeScreen> {
                        WelcomeScreen(
                            onNavigateToTutorialScreen = dropUnlessResumed {
                                backStack.navigate(
                                    Route.TutorialScreen(fromWelcomeScreen = true),
                                    popUpTo = Route.WelcomeScreen,
                                    popUpToInclusive = true
                                )
                            },
                            onNavigateToMainScreen = dropUnlessResumed {
                                backStack.navigate(
                                    Route.MainScreen,
                                    popUpTo = Route.WelcomeScreen,
                                    popUpToInclusive = true
                                )
                            },
                            doNotShowWelcomeScreenAgain =
                                sharedViewModel::doNotShowWelcomeScreenAgain
                        )
                    }
                    entry<Route.WorkoutScreen> { route ->
                        WorkoutScreen(
                            route = route,
                            onNavigateBack = dropUnlessResumed { backStack.goBack() },
                            onNavigateToBeforeSavingScreen = dropUnlessResumedNav { workoutId ->
                                backStack.navigate(Route.BeforeSavingScreen(workoutId))
                            },
                            onNavigateToAddExercises = dropUnlessResumed {
                                backStack.navigate(Route.ExercisesScreen(addExercises = true))
                            },
                            onNavigateToInfoExercise = dropUnlessResumedNav { id, exerciseDCid ->
                                backStack.navigate(Route.InfoExerciseScreen(id, exerciseDCid))
                            },
                            sharedViewModel = sharedViewModel,
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                    }
                }
            )
        }
    }
}
