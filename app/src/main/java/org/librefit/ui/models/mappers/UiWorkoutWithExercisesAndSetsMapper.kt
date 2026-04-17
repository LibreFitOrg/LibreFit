/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models.mappers

import kotlinx.collections.immutable.toImmutableList
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.ui.models.UiWorkoutWithExercisesAndSets

fun WorkoutWithExercisesAndSets.toUi(): UiWorkoutWithExercisesAndSets {
    return UiWorkoutWithExercisesAndSets(
        workout = workout.toUi(),
        exercisesWithSets = exercisesWithSets
            .sortedBy { it.exercise.position }
            .map { it.toUi() }
            .toImmutableList()
    )
}

fun UiWorkoutWithExercisesAndSets.toEntity(): WorkoutWithExercisesAndSets {
    return WorkoutWithExercisesAndSets(
        workout = workout.toEntity(),
        exercisesWithSets = exercisesWithSets.map { it.toEntity() }
    )
}
