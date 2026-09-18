/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WorkoutOrderTest {

    private val routines = listOf(
        UiWorkout(id = 11L, position = 0),
        UiWorkout(id = 22L, position = 1),
        UiWorkout(id = 33L, position = 2)
    )

    @Test
    fun `moveWorkout reorders list and rewrites positions`() {
        val reordered = routines.moveWorkout(fromIndex = 0, toIndex = 2)

        assertThat(reordered.map { it.id }).containsExactly(22L, 33L, 11L).inOrder()
        assertThat(reordered.map { it.position }).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun `moveWorkout ignores invalid indices`() {
        val reordered = routines.moveWorkout(fromIndex = -1, toIndex = 2)

        assertThat(reordered).isEqualTo(routines)
    }

    @Test
    fun `withNormalizedWorkoutPositions rewrites positions sequentially`() {
        val normalized = listOf(
            UiWorkout(id = 22L, position = 99),
            UiWorkout(id = 11L, position = 44)
        ).withNormalizedWorkoutPositions()

        assertThat(normalized.map { it.id }).containsExactly(22L, 11L).inOrder()
        assertThat(normalized.map { it.position }).containsExactly(0, 1).inOrder()
    }
}
