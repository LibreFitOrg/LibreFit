/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models.mappers

import org.librefit.db.entity.Workout
import org.librefit.ui.models.UiWorkout

fun Workout.toUi(): UiWorkout {
    return UiWorkout(
        id = this.id,
        routineId = this.routineId,
        notes = this.notes,
        title = this.title,
        state = this.state,
        timeElapsed = this.timeElapsed,
        position = this.position,
        created = this.created,
        completed = this.completed
    )
}

fun UiWorkout.toEntity(): Workout {
    return Workout(
        id = this.id,
        routineId = this.routineId,
        notes = this.notes,
        title = this.title,
        state = this.state,
        timeElapsed = this.timeElapsed,
        position = this.position,
        created = this.created,
        completed = this.completed
    )
}