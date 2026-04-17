/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

fun List<UiExerciseWithSets>.withNormalizedExercisePositions(): List<UiExerciseWithSets> {
    return mapIndexed { index, exerciseWithSets ->
        exerciseWithSets.copy(exercise = exerciseWithSets.exercise.copy(position = index))
    }
}

fun List<UiExerciseWithSets>.moveExercise(fromIndex: Int, toIndex: Int): List<UiExerciseWithSets> {
    if (fromIndex == toIndex || fromIndex !in indices || toIndex !in indices) return this

    return toMutableList()
        .apply {
            add(toIndex, removeAt(fromIndex))
        }
        .withNormalizedExercisePositions()
}
