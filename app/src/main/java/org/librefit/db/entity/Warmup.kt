/*
 *
 *  * SPDX-License-Identifier: GPL-3.0-or-later
 *  * Copyright (c) 2025-2026. The LibreFit Contributors
 *  *
 *  * LibreFit is subject to additional terms covering author attribution and trademark usage;
 *  * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.librefit.enums.WarmupMode
import kotlin.random.Random

/**
 * Entity representing a warmup record in the "warmup" table.
 *
 * As with the [Exercise] entity, this entity is linked to a [Workout] entity via a foreign key defined by the [workoutId] property.
 * The foreign key constraint ensures that when a [Workout] is deleted, all related exercises are also deleted (CASCADE deletion).
 *
 * @property id The unique identifier for the exercise. It is auto-generated and serves as the primary key.
 * It is used as key identifier in lazy columns too.
 * @property notes A user note editable by the user in [org.librefit.ui.screens.workout.WorkoutScreen]
 * and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property warmupMode The mode of the warmup set editable by the user in
 * [org.librefit.ui.screens.workout.WorkoutScreen] and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property restTime The rest time between sets in seconds editable by the user in
 * [org.librefit.ui.screens.workout.WorkoutScreen] and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property position The explicit position of the exercise in its parent workout. It is used to
 * keep exercise order stable across reloads and edits.
 * @property target The target load to warmup up to. It is used to automatically calculate
 * warmup sets in [org.librefit.ui.models.UiWarmupWithSets] and is editable by the user in
 * [org.librefit.ui.screens.workout.WorkoutScreen] and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property workoutId This is a foreign key reference to the [Workout] entity.
 *
 */
@Entity(
    tableName = "warmups",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutId"]),
        Index(value = ["workoutId", "position"])
    ]
)
@Serializable
data class Warmup(
    @PrimaryKey(true) val id: Long = Random.nextLong(),
    val notes: String = "",
    val warmupMode: WarmupMode = WarmupMode.DEFAULT,
    val restTime: Int = 0,
    val position: Int = 0,
    val target: Double = .0,
    val workoutId: Long = 0,
)

