/*
 * Copyright (c) 2024-2025. LibreFit Team
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
 *
 * LibreFit is subject to additional terms covering author attribution and
 * trademark usage, as found in the accompanying ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md.
 */

package org.librefit.nav

import kotlinx.serialization.Serializable
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.enums.SuccessMessage
import org.librefit.enums.pages.TutorialContent


sealed interface Route {
    @Serializable
    data object AboutScreen

    @Serializable
    data class BeforeSavingScreen(
        val workoutWithExercisesAndSets: WorkoutWithExercisesAndSets,
        val runningWorkoutId: Long? = null
    )

    @Serializable
    data object CalendarScreen

    @Serializable
    data class EditExerciseScreen(
        val id: Long = 0,
        val exerciseDCid: String = ""
    )

    @Serializable
    data class EditWorkoutScreen(
        val workoutId: Long
    )

    @Serializable
    data class ExercisesScreen(
        val addExercises: Boolean
    )

    @Serializable
    data class InfoExerciseScreen(
        val id: Long,
        val exerciseDC: ExerciseDC
    )

    @Serializable
    data class InfoWorkoutScreen(
        val workoutId: Long
    )

    @Serializable
    data object MainScreen

    @Serializable
    data object MeasurementScreen

    @Serializable
    data object PrivacyScreen

    @Serializable
    data class RequestPermissionScreen(
        val workoutId: Long
    )

    @Serializable
    data object LibrariesScreen

    @Serializable
    data object LicenseScreen

    @Serializable
    data object SettingsScreen

    @Serializable
    data object StatisticsScreen

    @Serializable
    data class SuccessScreen(
        val message: SuccessMessage,
    )

    @Serializable
    data class SupportScreen(
        val supporterInfo: Boolean = false
    )

    @Serializable
    data class TutorialScreen(
        val tutorialContent: TutorialContent = TutorialContent.CREATE_ROUTINE,
        val fromWelcomeScreen: Boolean = false
    )

    @Serializable
    data object WelcomeScreen

    @Serializable
    data class WorkoutScreen(
        val workoutId: Long
    )
}