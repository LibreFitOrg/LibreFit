/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.nav

import kotlinx.serialization.Serializable
import org.librefit.enums.SuccessMessage
import org.librefit.enums.pages.TutorialContent


sealed interface Route {
    @Serializable
    data object AboutScreen : Route

    @Serializable
    data class BeforeSavingScreen(
        val runningWorkoutId: Long
    ) : Route

    @Serializable
    data object CalendarScreen : Route

    @Serializable
    data class EditExerciseScreen(
        val id: Long = 0,
        val exerciseDCid: String = ""
    ) : Route

    @Serializable
    data class EditWorkoutScreen(
        val workoutId: Long
    ) : Route

    @Serializable
    data class ExercisesScreen(
        val addExercises: Boolean
    ) : Route

    @Serializable
    data class InfoExerciseScreen(
        val id: Long,
        val idExerciseDC: String
    ) : Route

    @Serializable
    data class InfoWorkoutScreen(
        val workoutId: Long
    ) : Route

    @Serializable
    data object MainScreen : Route

    @Serializable
    data object MeasurementScreen : Route

    @Serializable
    data object PrivacyScreen : Route

    @Serializable
    data class RequestPermissionScreen(
        val workoutId: Long
    ) : Route

    @Serializable
    data object DependenciesScreen : Route

    @Serializable
    data object LicenseScreen : Route

    @Serializable
    data object SettingsScreen : Route

    @Serializable
    data object StatisticsScreen : Route

    @Serializable
    data class SuccessScreen(
        val message: SuccessMessage,
    ) : Route

    @Serializable
    data class SupportScreen(
        val supporterInfo: Boolean = false
    ) : Route

    @Serializable
    data class TutorialScreen(
        val tutorialContent: TutorialContent = TutorialContent.CREATE_ROUTINE,
        val fromWelcomeScreen: Boolean = false
    ) : Route

    @Serializable
    data object WelcomeScreen : Route

    @Serializable
    data class WorkoutScreen(
        val workoutId: Long
    ) : Route
}