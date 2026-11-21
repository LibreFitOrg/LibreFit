/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.enums

import org.librefit.models.Weight
import org.librefit.ui.models.UiSet

// The default warmup set mode is taken as the first available has the associated SetMode of the exercise
enum class WarmupMode(val associatedSetModes: List<SetMode>) {
    FORTYSIXTYEIGHTY(listOf(SetMode.LOAD, SetMode.BODYWEIGHT_WITH_LOAD)),

    CUSTOM(SetMode.entries.toList());

    companion object {
        fun modesForSetMode(setMode: SetMode): List<WarmupMode> {
            return WarmupMode.entries.filter { warmupMode ->
                warmupMode.associatedSetModes.contains(
                    setMode
                )
            }
        }

    }
}

fun WarmupMode.createUiSets(target: Weight): List<UiSet> {
    return when (this) {
        WarmupMode.FORTYSIXTYEIGHTY -> listOf(
            UiSet(load = target * 0.4, reps = 10, warmup = true),
            UiSet(load = target * 0.6, reps = 6, warmup = true),
            UiSet(load = target * 0.8, reps = 3, warmup = true)
        )

        WarmupMode.CUSTOM -> listOf(UiSet(warmup = true))
    }
}
