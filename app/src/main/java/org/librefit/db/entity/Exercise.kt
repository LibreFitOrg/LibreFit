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
 * @property idExerciseDC It stores [ExerciseDC.id] in order to be able to retrieve
 * [ExerciseDC] and provide it to [org.librefit.db.relations.ExerciseWithSets]
 * @property notes A user note editable by the user in [org.librefit.ui.screens.workout.WorkoutScreen]
 * and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property setMode The mode of the exercise set editable by the user in
 * [org.librefit.ui.screens.workout.WorkoutScreen] and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property restTime The rest time between sets in seconds editable by the user in
 * [org.librefit.ui.screens.workout.WorkoutScreen] and [org.librefit.ui.screens.editWorkout.EditWorkoutScreen]
 * @property position The explicit position of the exercise in its parent workout. It is used to
 * keep exercise order stable across reloads and edits.
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
        ),
        ForeignKey(
            entity = ExerciseDC::class,
            parentColumns = ["id"],
            childColumns = ["idExerciseDC"],
            onDelete = ForeignKey.CASCADE // Delete exercise when its respective exerciseDC is deleted
        )
    ],
    indices = [
        Index(value = ["workoutId"]),
        Index(value = ["workoutId", "position"]),
        Index(value = ["idExerciseDC"])
    ]
)
@Serializable
data class Exercise(
    @PrimaryKey(true) val id: Long = Random.nextLong(),
    val idExerciseDC: String = "",
    val notes: String = "",
    val setMode: SetMode = SetMode.LOAD,
    val restTime: Int = 0,
    val position: Int = 0,
    val workoutId: Long = 0// Foreign key reference to Workout
)
