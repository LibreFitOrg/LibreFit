/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.models.mappers

import org.librefit.db.entity.Warmup
import org.librefit.ui.models.UiWarmup

fun Warmup.toUi(): UiWarmup {
    return UiWarmup(
        id = this.id,
        notes = this.notes,
        warmupMode = this.warmupMode,
        restTime = this.restTime,
        position = this.position,
        target = this.target,
        workoutId = this.workoutId
    )
}

fun UiWarmup.toEntity(): Warmup {
    return Warmup(
        id = this.id,
        notes = this.notes,
        warmupMode = this.warmupMode,
        restTime = this.restTime,
        position = this.position,
        target = this.target,
        workoutId = this.workoutId
    )
}
