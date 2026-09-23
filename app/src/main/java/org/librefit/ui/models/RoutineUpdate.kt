/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import kotlinx.collections.immutable.toImmutableList

fun List<UiExerciseWithSets>.withValuesOfCompletedSets(
    workoutExercises: List<UiExerciseWithSets>
): List<UiExerciseWithSets> {
    return mapIndexed { index, eWs ->
        val occurrence = take(index).count { it.exerciseDC.id == eWs.exerciseDC.id }
        val workoutEWS = workoutExercises
            .filter { it.exerciseDC.id == eWs.exerciseDC.id }
            .getOrNull(occurrence)

        if (workoutEWS != null && workoutEWS.exercise.setMode == eWs.exercise.setMode) {
            eWs.copy(
                sets = eWs.sets.mapIndexed { i, set ->
                    val workoutSet = workoutEWS.sets.getOrNull(i)

                    if (workoutSet != null && workoutSet.completed) {
                        set.copy(
                            load = workoutSet.load,
                            reps = workoutSet.reps,
                            elapsedTime = workoutSet.elapsedTime
                        )
                    } else set
                }.toImmutableList()
            )
        } else eWs
    }
}
