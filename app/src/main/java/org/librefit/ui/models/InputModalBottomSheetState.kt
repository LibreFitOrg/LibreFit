/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

sealed class InputModalBottomSheetState {
    data class Time(
        val minutes: Int = 0,
        val seconds: Int = 0,
        val minutesRange: ImmutableList<Int> = (0..59).toImmutableList(),
        val secondsRange: ImmutableList<Int> = (0..59).toImmutableList()
    ) : InputModalBottomSheetState()

    data class Weight(
        val integerWeight: Int = 0,
        val decimalWeight: Int = 0,
        val integerWeightRange: ImmutableList<Int> = (0..300).toImmutableList(),
        val decimalWeightRange: ImmutableList<Int> = (0..99).toImmutableList()
    ) : InputModalBottomSheetState()

    data class Reps(
        val reps: Int = 0,
        val repsRange: ImmutableList<Int> = (0..999).toImmutableList()
    ) : InputModalBottomSheetState()
}