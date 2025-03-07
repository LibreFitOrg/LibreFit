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
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entity representing an workout record in the "workouts" table.
 *
 * @property id The unique identifier for the workout. This is the primary key and is auto-incremented.
 * @property routineId An identifier used for statistics and to determine if the workout has a parent routine.
 * This means every workout from a single routine will share the same [routineId].
 * @property title The title of the workout visible to the user. The user can modify it in [org.librefit.ui.screens.workout.WorkoutScreen]
 * and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property notes A user note about the routine or workout. The user can modify it in [org.librefit.ui.screens.workout.WorkoutScreen]
 *  * and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property routine A boolean flag indicating whether the workout is a routine.
 * If `true`, the workout will be displayed as a playable routine in the [org.librefit.ui.screens.home.HomeScreen]
 * and the info in [org.librefit.ui.screens.infoWorkout.InfoWorkoutScreen] will be shown differently
 * @property timeElapsed The total time elapsed during the workout, measured in seconds by
 * [org.librefit.services.WorkoutService] chronometer during a workout session.
 * @property created The timestamp indicating when a routine was created.
 * @property completed The timestamp indicating when the workout was completed.
 * This is set to the current date and time by default, but it is updated when the workout is
 * finished in the [org.librefit.ui.screens.workout.WorkoutScreen].
 */
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(true) val id: Int = 0,
    val routineId: Long = System.currentTimeMillis(),
    val notes: String = "",
    val title: String = "",
    val routine: Boolean = false,
    val timeElapsed: Int = 0,
    val created: LocalDateTime = LocalDateTime.now(),
    val completed: LocalDateTime = LocalDateTime.now()
)
