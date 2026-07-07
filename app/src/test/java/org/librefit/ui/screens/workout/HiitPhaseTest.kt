/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.workout

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HiitPhaseTest {

    private val exerciseId = 42L

    @Test
    fun `nextPhaseAfterCountdown transitions to rest when there are more sets and auto-advance is on`() {
        val current = HiitPhase.SetCountdown(exerciseId = exerciseId, setIndex = 0)

        val next = current.nextPhaseAfterCountdown(totalSets = 3, autoAdvance = true)

        assertThat(next).isEqualTo(
            HiitPhase.RestBetweenSets(exerciseId = exerciseId, nextSetIndex = 1)
        )
    }

    @Test
    fun `nextPhaseAfterCountdown finishes exercise when the last set has just completed`() {
        val current = HiitPhase.SetCountdown(exerciseId = exerciseId, setIndex = 2)

        val next = current.nextPhaseAfterCountdown(totalSets = 3, autoAdvance = true)

        assertThat(next).isEqualTo(HiitPhase.ExerciseDone)
    }

    @Test
    fun `nextPhaseAfterCountdown finishes exercise when auto-advance is off`() {
        val current = HiitPhase.SetCountdown(exerciseId = exerciseId, setIndex = 0)

        val next = current.nextPhaseAfterCountdown(totalSets = 3, autoAdvance = false)

        assertThat(next).isEqualTo(HiitPhase.ExerciseDone)
    }

    @Test
    fun `nextPhaseAfterCountdown preserves the exercise id across transitions`() {
        val current = HiitPhase.SetCountdown(exerciseId = 9999L, setIndex = 0)

        val next = current.nextPhaseAfterCountdown(totalSets = 2, autoAdvance = true)

        assertThat(next).isInstanceOf(HiitPhase.RestBetweenSets::class.java)
        assertThat((next as HiitPhase.RestBetweenSets).exerciseId).isEqualTo(9999L)
    }
}
