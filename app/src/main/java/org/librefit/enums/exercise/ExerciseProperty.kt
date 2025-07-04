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

package org.librefit.enums.exercise

import kotlin.reflect.KClass

sealed interface ExerciseProperty {

    companion object {
        val propertiesPairsByEnum
                : List<Pair<List<ExerciseProperty?>, KClass<out ExerciseProperty>>> = listOf(
            (listOf<ExerciseProperty?>(null) + Level.entries.toList()) to Level::class,
            (listOf<ExerciseProperty?>(null) + Force.entries.toList()) to Force::class,
            (listOf<ExerciseProperty?>(null) + Mechanic.entries.toList()) to Mechanic::class,
            (listOf<ExerciseProperty?>(null) + Equipment.entries.toList()) to Equipment::class,
            (listOf<ExerciseProperty?>(null) + Muscle.entries.toList()) to Muscle::class,
            (listOf<ExerciseProperty?>(null) + Category.entries.toList()) to Category::class
        )
    }
}