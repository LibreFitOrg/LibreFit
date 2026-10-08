/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals

class InputModalBottomSheetStateTest {

    // ---------- MinutesSeconds ----------

    @Test
    fun `steppedBy carries over from seconds to minutes on positive delta`() {
        val state = InputModalBottomSheetState.MinutesSeconds(minutes = 0, seconds = 50)

        val stepped = state.steppedBy(30)

        assertEquals(1, stepped.minutes)
        assertEquals(20, stepped.seconds)
    }

    @Test
    fun `steppedBy carries over from minutes to seconds on negative delta`() {
        val state = InputModalBottomSheetState.MinutesSeconds(minutes = 1, seconds = 5)

        val stepped = state.steppedBy(-30)

        assertEquals(0, stepped.minutes)
        assertEquals(35, stepped.seconds)
    }

    @Test
    fun `steppedBy clamps minutes seconds state at zero`() {
        val state = InputModalBottomSheetState.MinutesSeconds(minutes = 0, seconds = 10)

        val stepped = state.steppedBy(-30)

        assertEquals(
            InputModalBottomSheetState.MinutesSeconds(minutes = 0, seconds = 0),
            stepped
        )
    }

    @Test
    fun `steppedBy clamps minutes seconds state at maximum range`() {
        val state = InputModalBottomSheetState.MinutesSeconds(minutes = 59, seconds = 59)

        val stepped = state.steppedBy(30)

        assertEquals(
            InputModalBottomSheetState.MinutesSeconds(minutes = 59, seconds = 59),
            stepped
        )
    }

    @Test
    fun `steppedBy with zero delta returns an equal minutes seconds state`() {
        val state = InputModalBottomSheetState.MinutesSeconds(minutes = 3, seconds = 25)

        val stepped = state.steppedBy(0)

        assertEquals(state, stepped)
    }

    // ---------- HoursMinutesSeconds ----------

    @Test
    fun `steppedBy carries over across the hour boundary`() {
        val state =
            InputModalBottomSheetState.HoursMinutesSeconds(hours = 0, minutes = 59, seconds = 50)

        val stepped = state.steppedBy(30)

        assertEquals(1, stepped.hours)
        assertEquals(0, stepped.minutes)
        assertEquals(20, stepped.seconds)
    }

    @Test
    fun `steppedBy clamps hours minutes seconds state at zero`() {
        val state =
            InputModalBottomSheetState.HoursMinutesSeconds(hours = 0, minutes = 0, seconds = 10)

        val stepped = state.steppedBy(-30)

        assertEquals(
            InputModalBottomSheetState.HoursMinutesSeconds(
                hours = 0,
                minutes = 0,
                seconds = 0
            ),
            stepped
        )
    }

    @Test
    fun `steppedBy clamps hours minutes seconds state at maximum range`() {
        val state =
            InputModalBottomSheetState.HoursMinutesSeconds(hours = 23, minutes = 59, seconds = 59)

        val stepped = state.steppedBy(30)

        assertEquals(
            InputModalBottomSheetState.HoursMinutesSeconds(
                hours = 23,
                minutes = 59,
                seconds = 59
            ),
            stepped
        )
    }

    // ---------- Reps ----------

    @Test
    fun `steppedBy increments and decrements reps`() {
        val state = InputModalBottomSheetState.Reps(reps = 8)

        val incremented = state.steppedBy(5)
        val decremented = state.steppedBy(-5)

        assertEquals(13, incremented.reps)
        assertEquals(3, decremented.reps)
    }

    @Test
    fun `steppedBy clamps reps at range bounds`() {
        val decremented = InputModalBottomSheetState.Reps(reps = 2).steppedBy(-5)
        val incrementedFromMax = InputModalBottomSheetState.Reps(reps = 997).steppedBy(5)

        assertEquals(0, decremented.reps)
        assertEquals(999, incrementedFromMax.reps)
    }

    // ---------- Weight ----------

    @Test
    fun `steppedBy shifts the integer part and preserves the decimal part`() {
        val state = InputModalBottomSheetState.Weight.create(integerWeight = 62, decimalWeight = 30)

        val stepped = state.steppedBy(10)

        assertEquals(72, stepped.integerWeight)
        assertEquals(30, stepped.decimalWeight)
        assertEquals(72.3, stepped.totalWeight, absoluteTolerance = 1e-9)
    }

    @Test
    fun `steppedBy clamps weight at range bounds while keeping the decimal part`() {
        val state = InputModalBottomSheetState.Weight.create(integerWeight = 2, decimalWeight = 30)

        val stepped = state.steppedBy(-10)

        assertEquals(0, stepped.integerWeight)
        assertEquals(30, stepped.decimalWeight)
    }

    @Test
    fun `steppedBy snaps the shifted weight to the configured range steps`() {
        val state = InputModalBottomSheetState.Weight.create(
            integerWeight = 5,
            decimalWeight = 0,
            integerStep = 5,
            integerWeightRange = (0..999 step 5).toImmutableList()
        )

        val stepped = state.steppedBy(10)

        assertEquals(15, stepped.integerWeight)
    }
}
