/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.models


fun List<UiWorkoutItem>.withNormalizedExercisePositions(): List<UiWorkoutItem> {
    return mapIndexed { index, exerciseWithSets ->
        when (exerciseWithSets) {
            is UiExerciseItem -> UiExerciseItem(
                exerciseWithSets.exercise.copy(
                    exercise = exerciseWithSets.exercise.exercise.copy(
                        position = index
                    )
                )
            )

            is UiWarmupItem -> UiWarmupItem(
                exerciseWithSets.warmup.copy(
                    warmup = exerciseWithSets.warmup.warmup.copy(
                        position = index
                    )
                )
            )
        } as UiWorkoutItem

    }
}

fun List<UiWorkoutItem>.moveExercise(fromIndex: Int, toIndex: Int): List<UiWorkoutItem> {
    if (fromIndex == toIndex || fromIndex !in indices || toIndex !in indices) return this

    return toMutableList()
        .apply {
            add(toIndex, removeAt(fromIndex))
        }
        .withNormalizedExercisePositions()
}
