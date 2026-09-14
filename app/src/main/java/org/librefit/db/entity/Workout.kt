/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.librefit.enums.WorkoutState
import java.time.LocalDateTime
import kotlin.random.Random

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
 * @property state It indicates whether the workout is a workout, routine, archived or in the library of workouts.
 * When it is set as `ROUTINE`, it will be displayed as a playable routine in the [org.librefit.ui.screens.home.HomeScreen]
 * and the info in [org.librefit.ui.screens.infoWorkout.InfoWorkoutScreen] will be shown differently.
 * When it is set as `ARCHIVED`, it will be located in the respective section in `HomeScreen`. Same goes for `LIBRARY`
 * @property timeElapsed The total time elapsed during the workout, measured in seconds by
 * [org.librefit.services.WorkoutService]'s stopwatch during a workout session.
 * @property created The timestamp indicating when a routine was created.
 * @property completed The timestamp indicating when the workout was completed.
 * This is set to the current date and time by default, but it is updated when the workout is
 * finished in the [org.librefit.ui.screens.workout.WorkoutScreen].
 */
@Entity(tableName = "workouts")
@Serializable
data class Workout(
    @PrimaryKey(true)
    val id: Long = 0,
    val routineId: Long = Random.nextLong(),
    val notes: String = "",
    val title: String = "",
    val state: WorkoutState = WorkoutState.COMPLETED,
    val timeElapsed: Int = 0,
    @Serializable(with = LocalDateTimeSerializer::class)
    val created: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val completed: LocalDateTime = LocalDateTime.now(),
    val healthConnectRecordVersion: Long = 0
)
