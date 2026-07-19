/*
 *
 *  * SPDX-License-Identifier: GPL-3.0-or-later
 *  * Copyright (c) 2025-2026. The LibreFit Contributors
 *  *
 *  * LibreFit is subject to additional terms covering author attribution and trademark usage;
 *  * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.db.relations

import kotlinx.serialization.Serializable
import org.librefit.db.entity.Set

/**
 * A sealed class used to allow [WarmupWithSets] and [ExerciseWithSets] to be handled together.
 * It also provides a consistent interface for working with both classes.
 *
 * @property id The [org.librefit.db.entity.Warmup] or [org.librefit.db.entity.Exercise] id for
 * the wrapped class.
 * @property position The position of the [org.librefit.db.entity.Warmup] or [org.librefit.db.entity.Exercise].
 * @property sets A getter function which gets the sets associated with the [WarmupWithSets] or [ExerciseWithSets].
 */
@Serializable
sealed class WorkoutItem {
    abstract val id: Long
    abstract val position: Int
    val sets: List<Set>
        get() = when (this) {
            is WarmupItem -> this.warmup.sets
            is ExerciseItem -> this.exercise.sets
        }

}

/**
 * A data class which allows a [WarmupWithSets] to be considered a [WorkoutItem].
 * So that it can be used in the same places as [ExerciseItem].
 */
@Serializable
data class WarmupItem(val warmup: WarmupWithSets) : WorkoutItem() {
    override val id: Long = warmup.warmup.id
    override val position: Int = warmup.warmup.position
}

/**
 * A data class which allows a [ExerciseWithSets] to be considered a [WorkoutItem].
 * So that it can be used in the same places as [WarmupItem].
 */
@Serializable
data class ExerciseItem(val exercise: ExerciseWithSets) : WorkoutItem() {
    override val id: Long = exercise.exercise.id
    override val position: Int = exercise.exercise.position
}
