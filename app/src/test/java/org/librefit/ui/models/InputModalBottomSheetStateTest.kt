/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class InputModalBottomSheetStateTest {

    // ---------- MinutesSeconds ----------

    @Test
    fun `steppedBy carries over from seconds to minutes on positive delta`() {
        val state = InputModalBottomSheetState.MinutesSeconds(minutes = 0, seconds = 50)

        val stepped = state.steppedBy(30)

        assertThat(stepped.minutes).isEqualTo(1)
        assertThat(stepped.seconds).isEqualTo(20)
    }

    @Test
    fun `steppedBy carries over from minutes to seconds on negative delta`() {
        val state = InputModalBottomSheetState.MinutesSeconds(minutes = 1, seconds = 5)

        val stepped = state.steppedBy(-30)

        assertThat(stepped.minutes).isEqualTo(0)
        assertThat(stepped.seconds).isEqualTo(35)
    }

    @Test
    fun `steppedBy clamps minutes seconds state at zero`() {
        val state = InputModalBottomSheetState.MinutesSeconds(minutes = 0, seconds = 10)

        val stepped = state.steppedBy(-30)

        assertThat(stepped)
            .isEqualTo(InputModalBottomSheetState.MinutesSeconds(minutes = 0, seconds = 0))
    }

    @Test
    fun `steppedBy clamps minutes seconds state at maximum range`() {
        val state = InputModalBottomSheetState.MinutesSeconds(minutes = 59, seconds = 59)

        val stepped = state.steppedBy(30)

        assertThat(stepped)
            .isEqualTo(InputModalBottomSheetState.MinutesSeconds(minutes = 59, seconds = 59))
    }

    @Test
    fun `steppedBy with zero delta returns an equal minutes seconds state`() {
        val state = InputModalBottomSheetState.MinutesSeconds(minutes = 3, seconds = 25)

        val stepped = state.steppedBy(0)

        assertThat(stepped).isEqualTo(state)
    }

    // ---------- HoursMinutesSeconds ----------

    @Test
    fun `steppedBy carries over across the hour boundary`() {
        val state =
            InputModalBottomSheetState.HoursMinutesSeconds(hours = 0, minutes = 59, seconds = 50)

        val stepped = state.steppedBy(30)

        assertThat(stepped.hours).isEqualTo(1)
        assertThat(stepped.minutes).isEqualTo(0)
        assertThat(stepped.seconds).isEqualTo(20)
    }

    @Test
    fun `steppedBy clamps hours minutes seconds state at zero`() {
        val state =
            InputModalBottomSheetState.HoursMinutesSeconds(hours = 0, minutes = 0, seconds = 10)

        val stepped = state.steppedBy(-30)

        assertThat(stepped)
            .isEqualTo(
                InputModalBottomSheetState.HoursMinutesSeconds(
                    hours = 0,
                    minutes = 0,
                    seconds = 0
                )
            )
    }

    @Test
    fun `steppedBy clamps hours minutes seconds state at maximum range`() {
        val state =
            InputModalBottomSheetState.HoursMinutesSeconds(hours = 23, minutes = 59, seconds = 59)

        val stepped = state.steppedBy(30)

        assertThat(stepped)
            .isEqualTo(
                InputModalBottomSheetState.HoursMinutesSeconds(
                    hours = 23,
                    minutes = 59,
                    seconds = 59
                )
            )
    }

    // ---------- Reps ----------

    @Test
    fun `steppedBy increments and decrements reps`() {
        val state = InputModalBottomSheetState.Reps(reps = 8)

        val incremented = state.steppedBy(5)
        val decremented = state.steppedBy(-5)

        assertThat(incremented.reps).isEqualTo(13)
        assertThat(decremented.reps).isEqualTo(3)
    }

    @Test
    fun `steppedBy clamps reps at range bounds`() {
        val decremented = InputModalBottomSheetState.Reps(reps = 2).steppedBy(-5)
        val incrementedFromMax = InputModalBottomSheetState.Reps(reps = 997).steppedBy(5)

        assertThat(decremented.reps).isEqualTo(0)
        assertThat(incrementedFromMax.reps).isEqualTo(999)
    }

    // ---------- Weight ----------

    @Test
    fun `steppedBy shifts the integer part and preserves the decimal part`() {
        val state = InputModalBottomSheetState.Weight.create(integerWeight = 62, decimalWeight = 30)

        val stepped = state.steppedBy(10)

        assertThat(stepped.integerWeight).isEqualTo(72)
        assertThat(stepped.decimalWeight).isEqualTo(30)
        assertThat(stepped.totalWeight).isWithin(1e-9).of(72.3)
    }

    @Test
    fun `steppedBy clamps weight at range bounds while keeping the decimal part`() {
        val state = InputModalBottomSheetState.Weight.create(integerWeight = 2, decimalWeight = 30)

        val stepped = state.steppedBy(-10)

        assertThat(stepped.integerWeight).isEqualTo(0)
        assertThat(stepped.decimalWeight).isEqualTo(30)
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

        assertThat(stepped.integerWeight).isEqualTo(15)
    }
}
