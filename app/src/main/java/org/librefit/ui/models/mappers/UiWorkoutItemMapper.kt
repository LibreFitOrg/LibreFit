/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.models.mappers

import org.librefit.db.relations.ExerciseItem
import org.librefit.db.relations.WarmupItem
import org.librefit.db.relations.WorkoutItem
import org.librefit.ui.models.UiExerciseItem
import org.librefit.ui.models.UiWarmupItem
import org.librefit.ui.models.UiWorkoutItem

fun WorkoutItem.toUi(): UiWorkoutItem {
    return when (this) {
        is WarmupItem -> this.toUi()
        is ExerciseItem -> this.toUi()
    }
}

fun UiWorkoutItem.toEntity(): WorkoutItem {
    return when (this) {
        is UiWarmupItem -> this.toEntity()
        is UiExerciseItem -> this.toEntity()
    }
}