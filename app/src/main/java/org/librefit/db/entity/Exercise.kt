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

package org.librefit.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.librefit.enums.SetMode
import kotlin.random.Random

/**
 * Entity representing an exercise record in the "exercises" table.
 *
 * This entity is linked to a [Workout] entity via a foreign key defined by the [workoutId] property.
 * The foreign key constraint ensures that when a [Workout] is deleted, all related exercises are also deleted (CASCADE deletion).
 *
 * @property id The unique identifier for the exercise. It is auto-generated and serves as the primary key.
 * It is used as key identifier in lazy columns too.
 * @property exerciseId It stores [org.librefit.data.ExerciseDC.id] so [org.librefit.ui.screens.shared.SharedViewModel]
 * is able to retrieve [org.librefit.data.ExerciseDC] and provide it to [org.librefit.db.relations.ExerciseWithSets]
 * @property notes A user note editable by the user in [org.librefit.ui.screens.workout.WorkoutScreen]
 * and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property setMode The mode of the exercise set editable by the user in
 * [org.librefit.ui.screens.workout.WorkoutScreen] and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property restTime The rest time between sets in seconds editable by the user in
 * [org.librefit.ui.screens.workout.WorkoutScreen] and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property workoutId This is a foreign key reference to the [Workout] entity.
 *
 */
@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workoutId"])]
)
data class Exercise(
    @PrimaryKey(true) val id: Long = Random.nextLong() + System.currentTimeMillis(),
    val exerciseId: String = "",
    val notes: String = "",
    val setMode: SetMode = SetMode.LOAD,
    val restTime: Int = 0,
    val workoutId: Long = 0// Foreign key reference to Workout
)