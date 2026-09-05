/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.enums

import org.librefit.ui.models.UiSet

enum class WarmupMode {
    DEFAULT
}

fun WarmupMode.createUiSets(target: Double?): List<UiSet> {
    val loadTarget = target ?: 0.0;
    return when (this) {
        WarmupMode.DEFAULT -> listOf(
            UiSet(load = loadTarget * 0.4, reps = 10, warmup = true),
            UiSet(load = loadTarget * 0.6, reps = 6, warmup = true),
            UiSet(load = loadTarget * 0.7, reps = 3, warmup = true)
        )
    }
}
