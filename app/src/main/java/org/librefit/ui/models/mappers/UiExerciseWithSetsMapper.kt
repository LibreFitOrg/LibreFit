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
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.ui.models.UiExerciseWithSets

fun ExerciseWithSets.toUi(): UiExerciseWithSets {
    return UiExerciseWithSets(
        exercise = this.exercise.toUi(),
        sets = this.sets.map { it.toUi() }.toImmutableList(),
        exerciseDC = this.exerciseDC.toUi()
    )
}

fun UiExerciseWithSets.toEntity(): ExerciseWithSets {
    return ExerciseWithSets(
        exercise = this.exercise.toEntity(),
        sets = sets.map { it.toEntity() },
        exerciseDC = exerciseDC.toEntity()
    )
}