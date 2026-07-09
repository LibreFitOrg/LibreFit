/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.models.mappers

import kotlinx.collections.immutable.toImmutableList
import org.librefit.db.relations.WarmupWithSets
import org.librefit.ui.models.UiWarmupWithSets

fun WarmupWithSets.toUi(): UiWarmupWithSets {
    return UiWarmupWithSets(
        warmup = this.warmup.toUi(),
        sets = this.sets.map { it.toUi() }.toImmutableList(),
    )
}

fun UiWarmupWithSets.toEntity(): WarmupWithSets {
    return WarmupWithSets(
        warmup = this.warmup.toEntity(),
        sets = sets.map { it.toEntity() },
    )
}