/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.librefit.enums.WarmupMode

/**
 * The [org.librefit.db.relations.WarmupWithSets] model used only by the ui. The difference is the use
 * of [ImmutableList] instead of [List] in order to ensure the [Immutable] annotation and improve
 * composition performance.
 *
 * @see [org.librefit.db.relations.WarmupWithSets]
 */
@Immutable
data class UiWarmupWithSets(
    val warmup: UiWarmup = UiWarmup(),
    val sets: ImmutableList<UiSet> = persistentListOf(UiSet())
)

fun recalcWarmupSets(target: Double, warmupMode: WarmupMode): ImmutableList<UiSet> {
    return when (warmupMode) {
        WarmupMode.DEFAULT -> persistentListOf(
            UiSet(load = target * 0.4, reps = 10),
            UiSet(load = target * 0.6, reps = 6),
            UiSet(load = target * 0.7, reps = 3)
        )
    }
}