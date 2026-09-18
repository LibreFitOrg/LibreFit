/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

fun List<UiWorkout>.withNormalizedWorkoutPositions(): List<UiWorkout> {
    return mapIndexed { index, workout ->
        workout.copy(position = index)
    }
}

fun List<UiWorkout>.moveWorkout(fromIndex: Int, toIndex: Int): List<UiWorkout> {
    if (fromIndex == toIndex || fromIndex !in indices || toIndex !in indices) return this

    return toMutableList()
        .apply {
            add(toIndex, removeAt(fromIndex))
        }
        .withNormalizedWorkoutPositions()
}
