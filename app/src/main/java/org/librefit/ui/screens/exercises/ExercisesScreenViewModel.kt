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

package org.librefit.ui.screens.exercises

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle
import org.librefit.util.ExerciseDC

class ExercisesScreenViewModel : ViewModel() {
    private var levelFilter = mutableStateOf<Level?>(null)
    private var forceFilter = mutableStateOf<Force?>(null)
    private var mechanicFilter = mutableStateOf<Mechanic?>(null)
    private var equipmentFilter = mutableStateOf<Equipment?>(null)
    private var muscleFilter = mutableStateOf<Muscle?>(null)
    private var categoryFilter = mutableStateOf<Category?>(null)


    fun updateFilter(enum: Enum<*>?, mode: Int) {
        when (mode) {
            0 -> levelFilter.value = enum as Level?
            1 -> forceFilter.value = enum as Force?
            2 -> mechanicFilter.value = enum as Mechanic?
            3 -> equipmentFilter.value = enum as Equipment?
            4 -> muscleFilter.value = enum as Muscle?
            5 -> categoryFilter.value = enum as Category?
            else -> null
        }
    }

    fun getFilter(mode: Int): Enum<*>? {
        return when (mode) {
            0 -> levelFilter.value
            1 -> forceFilter.value
            2 -> mechanicFilter.value
            3 -> equipmentFilter.value
            4 -> muscleFilter.value
            5 -> categoryFilter.value
            else -> null
        }
    }

    fun filterExercise(exercise: ExerciseDC): Boolean {
        if (levelFilter.value != null && levelFilter.value != exercise.level) {
            return false
        }
        if (forceFilter.value != null && forceFilter.value != exercise.force) {
            return false
        }
        if (mechanicFilter.value != null && mechanicFilter.value != exercise.mechanic) {
            return false
        }
        if (equipmentFilter.value != null && equipmentFilter.value != exercise.equipment) {
            return false
        }
        if (muscleFilter.value != null && !exercise.primaryMuscles.contains(muscleFilter.value)
            && !exercise.secondaryMuscles.contains(muscleFilter.value)
        ) {
            return false
        }
        if (categoryFilter.value != null && categoryFilter.value != exercise.category) {
            return false
        }
        return true
    }
}