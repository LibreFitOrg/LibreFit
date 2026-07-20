/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.librefit.models.Weight
import kotlin.random.Random

/**
 * Entity representing a set record in the "sets" table.
 *
 * This entity is linked to a [Exercise] entity via a foreign key defined by the [exerciseId] property.
 * The foreign key constraint ensures that when a [Exercise] is deleted, all related sets are also deleted (CASCADE deletion).
 *
 * ### Note
 * - The value of [reps],[load] and [elapsedTime] are exclusive between each other and it depends
 * by the [Exercise.setMode] value when saving in db. For instance if `setMode = SetMode.WEIGHT`
 * then [reps] and [elapsedTime] are assigned 0.
 * - These properties [reps],[load] and [elapsedTime] can be edited by the user in
 *  [org.librefit.ui.screens.workout.WorkoutScreen] and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 *
 *
 * @property id The unique identifier for the set. It is auto-generated and serves as the primary key.
 * @property load The weight of the set, in kilograms.
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
@Serializable
data class Set(
    @PrimaryKey(true) val id: Long = Random.nextLong(),
    val load: Weight = Weight.zero(),
    val reps: Int = 0,
    val elapsedTime: Int = 0,
    val completed: Boolean = false,
    val exerciseId: Long = 0// Foreign key reference to Exercise
)