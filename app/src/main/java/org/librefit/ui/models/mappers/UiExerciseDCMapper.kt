/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models.mappers

import kotlinx.collections.immutable.toImmutableList
import org.librefit.db.entity.ExerciseDC
import org.librefit.ui.models.UiExerciseDC

fun ExerciseDC.toUi(): UiExerciseDC {
    return UiExerciseDC(
        id = this.id,
        name = this.name,
        force = this.force,
        level = this.level,
        mechanic = this.mechanic,
        equipment = this.equipment,
        primaryMuscles = this.primaryMuscles.toImmutableList(),
        secondaryMuscles = this.secondaryMuscles.toImmutableList(),
        instructions = this.instructions.toImmutableList(),
        category = this.category,
        images = this.images.toImmutableList(),
        isCustomExercise = this.isCustomExercise
    )
}

fun UiExerciseDC.toEntity(): ExerciseDC {
    return ExerciseDC(
        id = this.id,
        name = this.name,
        force = this.force,
        level = this.level,
        mechanic = this.mechanic,
        equipment = this.equipment,
        primaryMuscles = this.primaryMuscles,
        secondaryMuscles = this.secondaryMuscles,
        instructions = this.instructions,
        category = this.category,
        images = this.images,
        isCustomExercise = this.isCustomExercise
    )
}