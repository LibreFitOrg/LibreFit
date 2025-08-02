/*
 * Copyright (c) 2025. LibreFit
 *
 * This file is part of LibreFit
 *
 * LibreFit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreFit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreFit.  If not, see <https://www.gnu.org/licenses/>.
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
        images = this.images.toImmutableList()
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
    )
}