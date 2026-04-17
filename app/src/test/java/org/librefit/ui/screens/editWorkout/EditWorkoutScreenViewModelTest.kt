/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.editWorkout

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.moveExercise
import org.librefit.ui.models.withNormalizedExercisePositions

class EditWorkoutScreenViewModelTest {

    private val exercises = listOf(
        UiExerciseWithSets(exercise = UiExercise(id = 11L, position = 0)),
        UiExerciseWithSets(exercise = UiExercise(id = 22L, position = 1)),
        UiExerciseWithSets(exercise = UiExercise(id = 33L, position = 2))
    )

    @Test
    fun `moveExercise reorders list and rewrites positions`() {
        val reordered = exercises.moveExercise(fromIndex = 0, toIndex = 2)

        assertThat(reordered.map { it.exercise.id }).containsExactly(22L, 33L, 11L).inOrder()
        assertThat(reordered.map { it.exercise.position }).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun `moveExercise ignores invalid indices`() {
        val reordered = exercises.moveExercise(fromIndex = -1, toIndex = 2)

        assertThat(reordered).isEqualTo(exercises)
    }

    @Test
    fun `withNormalizedExercisePositions rewrites positions sequentially`() {
        val normalized = listOf(
            UiExerciseWithSets(exercise = UiExercise(id = 22L, position = 99)),
            UiExerciseWithSets(exercise = UiExercise(id = 11L, position = 44))
        ).withNormalizedExercisePositions()

        assertThat(normalized.map { it.exercise.id }).containsExactly(22L, 11L).inOrder()
        assertThat(normalized.map { it.exercise.position }).containsExactly(0, 1).inOrder()
    }
}
