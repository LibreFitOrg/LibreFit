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

package org.librefit.data

import androidx.room.Embedded
import androidx.room.Relation
import org.librefit.db.Exercise
import org.librefit.db.Workout

/**
 * A data class representing a [Workout] and its associated [ExerciseWithSets].
 *
 * This class is used by Room to retrieve all the data associated with an workout and
 * the exercisesWithSets associated with it.
 *
 * @property workout It contains the data associated with this [Workout] some of which is user generated.
 * @property exercisesWithSets A list of [ExerciseWithSets] entities where each entry consists of an [Exercise]
 * and its related [Set]s.
 */
data class WorkoutWithExercisesAndSets(
    @Embedded val workout: Workout,
    @Relation(
        entity = Exercise::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercisesWithSets: List<ExerciseWithSets>
)
