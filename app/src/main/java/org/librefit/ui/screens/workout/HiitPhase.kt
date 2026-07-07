/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.workout

import androidx.compose.runtime.Immutable

/**
 * State of the HIIT countdown for a single exercise.
 *
 * The phase is owned by [WorkoutScreenViewModel] and consumed by
 * [org.librefit.ui.components.HiitCountdownCard]. The state machine is:
 *
 * ```
 * Idle ──onPlay──▶ SetCountdown ──finish──▶ RestBetweenSets ──finish──▶ SetCountdown ──▶ … ──▶ ExerciseDone
 *                       │                          │
 *                       └──cancel──▶ Idle ◀────────┘ (skip rest re-enters SetCountdown for the next set)
 * ```
 */
@Immutable
sealed class HiitPhase {
    /** No HIIT countdown running. The user can press play to begin. */
    data object Idle : HiitPhase()

    /** Countdown is active for the given exercise's [setIndex]. */
    data class SetCountdown(val exerciseId: Long, val setIndex: Int) : HiitPhase()

    /** Rest countdown that precedes set [nextSetIndex]. */
    data class RestBetweenSets(val exerciseId: Long, val nextSetIndex: Int) : HiitPhase()

    /** All sets for the current exercise are complete. */
    data object ExerciseDone : HiitPhase()
}

/**
 * Pure state-transition helper used when a set countdown finishes. Returns the next phase
 * given the current set index, the total number of sets, and whether auto-advance is on.
 *
 * Extracted to make the transition logic trivially unit-testable without spinning up
 * a ViewModel.
 */
fun HiitPhase.SetCountdown.nextPhaseAfterCountdown(
    totalSets: Int,
    autoAdvance: Boolean
): HiitPhase {
    val nextIndex = setIndex + 1
    return if (autoAdvance && nextIndex < totalSets) {
        HiitPhase.RestBetweenSets(exerciseId = exerciseId, nextSetIndex = nextIndex)
    } else {
        HiitPhase.ExerciseDone
    }
}
