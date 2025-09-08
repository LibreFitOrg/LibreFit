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

import kotlinx.serialization.Serializable
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.enums.SuccessMessage
import org.librefit.enums.pages.TutorialContent


sealed class Route {
    @Serializable
    object AboutScreen

    @Serializable
    data class BeforeSavingScreen(
        val workoutWithExercisesAndSets: WorkoutWithExercisesAndSets
    )

    @Serializable
    object CalendarScreen

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
    object MainScreen

    @Serializable
    object MeasurementScreen

    @Serializable
    object PrivacyScreen

    @Serializable
    data class RequestPermissionScreen(
        val workoutId: Long
    )

    @Serializable
    object LibrariesScreen

    @Serializable
    object LicenseScreen

    @Serializable
    object SettingsScreen

    @Serializable
    object StatisticsScreen

    @Serializable
    data class SuccessScreen(
        val message: SuccessMessage,
    )

    @Serializable
    object SupportScreen

    @Serializable
    data class TutorialScreen(
        val tutorialContent: TutorialContent = TutorialContent.CREATE_ROUTINE,
        val fromWelcomeScreen: Boolean = false
    )

    @Serializable
    object WelcomeScreen

    @Serializable
    data class WorkoutScreen(
        val workoutId: Long
    )
}