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

import org.librefit.db.entity.Exercise
import org.librefit.ui.models.UiExercise

fun Exercise.toUi(): UiExercise {
    return UiExercise(
        id = this.id,
        idExerciseDC = this.idExerciseDC,
        notes = this.notes,
        setMode = this.setMode,
        restTime = this.restTime,
        workoutId = this.workoutId
    )
}

fun UiExercise.toEntity(): Exercise {
    return Exercise(
        id = this.id,
        idExerciseDC = this.idExerciseDC,
        notes = this.notes,
        setMode = this.setMode,
        restTime = this.restTime,
        workoutId = this.workoutId
    )
}