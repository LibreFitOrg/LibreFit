/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models.mappers

import org.librefit.db.entity.Set
import org.librefit.ui.models.UiSet

fun Set.toUi(): UiSet {
    return UiSet(
        id = this.id,
        load = this.load,
        reps = this.reps,
        elapsedTime = this.elapsedTime,
        startedAt = this.startedAt,
        completedAt = this.completedAt,
        completed = this.completed,
        exerciseId = this.exerciseId
    )
}

fun UiSet.toEntity(): Set {
    return Set(
        id = this.id,
        load = this.load,
        reps = this.reps,
        elapsedTime = this.elapsedTime,
        startedAt = this.startedAt,
        completedAt = this.completedAt,
        completed = this.completed,
        exerciseId = this.exerciseId
    )
}
