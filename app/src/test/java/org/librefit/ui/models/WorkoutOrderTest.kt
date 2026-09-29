/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class WorkoutOrderTest {

    private val routines = listOf(
        UiWorkout(id = 11L, position = 0),
        UiWorkout(id = 22L, position = 1),
        UiWorkout(id = 33L, position = 2)
    )

    @Test
    fun `moveWorkout reorders list and rewrites positions`() {
        val reordered = routines.moveWorkout(fromIndex = 0, toIndex = 2)

        assertContentEquals(listOf(22L, 33L, 11L), reordered.map { it.id })
        assertContentEquals(listOf(0, 1, 2), reordered.map { it.position })
    }

    @Test
    fun `moveWorkout ignores invalid indices`() {
        val reordered = routines.moveWorkout(fromIndex = -1, toIndex = 2)

        assertEquals(routines, reordered)
    }

    @Test
    fun `withNormalizedWorkoutPositions rewrites positions sequentially`() {
        val normalized = listOf(
            UiWorkout(id = 22L, position = 99),
            UiWorkout(id = 11L, position = 44)
        ).withNormalizedWorkoutPositions()

        assertContentEquals(listOf(22L, 11L), normalized.map { it.id })
        assertContentEquals(listOf(0, 1), normalized.map { it.position })
    }
}
