/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.models

import kotlinx.collections.immutable.ImmutableList

/**
 * The [org.librefit.db.relations.WorkoutItem] model used only by the ui.
 *
 * @see [org.librefit.db.relations.WorkoutItem]
 */
sealed class UiWorkoutItem {
    abstract val id: Long
    val sets: ImmutableList<UiSet>
        get() = when (this) {
            is UiWarmupItem -> this.warmup.sets
            is UiExerciseItem -> this.exercise.sets
        }
    val position: Int
        get() = when (this) {
            is UiWarmupItem -> this.warmup.warmup.position
            is UiExerciseItem -> this.exercise.exercise.position
        }
    val restTime: Int
        get() = when (this) {
            is UiWarmupItem -> this.warmup.warmup.restTime
            is UiExerciseItem -> this.exercise.exercise.restTime
        }

    fun updateSets(newSets: ImmutableList<UiSet>): UiWorkoutItem {
        return when (this) {
            is UiWarmupItem -> UiWarmupItem(warmup.copy(sets = newSets))
            is UiExerciseItem -> UiExerciseItem(exercise.copy(sets = newSets))
        }
    }

    fun updateNotes(newNotes: String): UiWorkoutItem {
        return when (this) {
            is UiWarmupItem -> UiWarmupItem(warmup.copy(warmup = warmup.warmup.copy(notes = newNotes)))
            is UiExerciseItem -> UiExerciseItem(
                exercise.copy(
                    exercise = exercise.exercise.copy(
                        notes = newNotes
                    )
                )
            )
        }
    }

    fun updateRestTime(newRestTime: Int): UiWorkoutItem {
        return when (this) {
            is UiWarmupItem -> UiWarmupItem(warmup.copy(warmup = warmup.warmup.copy(restTime = newRestTime)))
            is UiExerciseItem -> UiExerciseItem(
                exercise.copy(
                    exercise = exercise.exercise.copy(
                        restTime = newRestTime
                    )
                )
            )
        }
    }
}

data class UiWarmupItem(val warmup: UiWarmupWithSets) : UiWorkoutItem() {
    override val id: Long = warmup.warmup.id
}

data class UiExerciseItem(val exercise: UiExerciseWithSets) : UiWorkoutItem() {
    override val id: Long = exercise.exercise.id
}