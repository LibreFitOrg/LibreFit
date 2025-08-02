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

package org.librefit.ui.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle

/**
 * The [org.librefit.db.entity.ExerciseDC] model used only by the ui. The difference is the use
 * of [ImmutableList] instead of [List] in order to ensure the [Immutable] annotation and improve
 * composition performance.
 *
 * @see [org.librefit.db.entity.ExerciseDC]
 */
@Immutable
data class UiExerciseDC(
    val id: String = "",
    val name: String = "",
    val force: Force? = null,
    val level: Level = Level.BEGINNER,
    val mechanic: Mechanic? = null,
    val equipment: Equipment? = null,
    val primaryMuscles: ImmutableList<Muscle> = persistentListOf(),
    val secondaryMuscles: ImmutableList<Muscle> = persistentListOf(),
    val instructions: ImmutableList<String> = persistentListOf(),
    val category: Category = Category.POWERLIFTING,
    val images: ImmutableList<String> = persistentListOf()
)