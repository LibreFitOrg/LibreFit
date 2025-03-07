/*
 * Copyright (c) 2024-2025. LibreFit
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

package org.librefit.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.random.Random

/**
 * Entity representing a set record in the "sets" table.
 *
 * This entity is linked to a [Exercise] entity via a foreign key defined by the [exerciseId] property.
 * The foreign key constraint ensures that when a [Exercise] is deleted, all related sets are also deleted (CASCADE deletion).
 *
 * ### Note
 * - The value of [reps],[weight] and [elapsedTime] are exclusive between each other and it depends
 * by the [Exercise.setMode] value when saving in db. For instance if `setMode = SetMode.WEIGHT`
 * then [reps] and [elapsedTime] are assigned 0.
 * - These properties [reps],[weight] and [elapsedTime] can be edited by the user in
 *  [org.librefit.ui.screens.workout.WorkoutScreen] and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 *
 *
 * @property id The unique identifier for the set. It is auto-generated and serves as the primary key.
 * @property weight The weight used for the set, in kilograms.
 * @property reps The number of repetitions performed in the set.
 * @property elapsedTime The time taken to complete the set, in seconds.
 * @property completed Indicates whether the set has been completed.
 * @property exerciseId This is a foreign key reference to the [Exercise] entity.
 *
 */
@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["exerciseId"])]
)
data class Set(
    @PrimaryKey(true) val id: Int = Random.nextInt(), //TODO: change id from Int to Long
    val weight: Float = 0f,
    val reps: Int = 0,
    val elapsedTime: Int = 0,
    val completed: Boolean = false,
    val exerciseId: Int = 0// Foreign key reference to Exercise
)