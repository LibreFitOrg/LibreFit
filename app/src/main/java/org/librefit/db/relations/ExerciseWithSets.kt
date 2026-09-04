/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable
import org.librefit.db.entity.Exercise
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.entity.Set

/**
 * A data class representing an [Exercise] with its associated [Set]s.
 *
 * This class is used by Room to retrieve all the data associated with an exercise and
 * the sets associated with it. The actual dataset is stored as [ExerciseDC] and provided
 * by [org.librefit.db.repository.DatasetRepository]
 *
 * @property exercise It contains the user related data associated with this [Exercise].
 * @property sets The list of [Set] associated with the [exercise] containing all the user related data.
 * @property exerciseDC The actual features of the exercise itself. Details at [ExerciseDC]
 */
@Serializable
data class ExerciseWithSets(
    @Embedded val exercise: Exercise = Exercise(),
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId"
    )
    val sets: List<Set> = listOf(Set()),
    @Relation(
        parentColumn = "idExerciseDC",
        entityColumn = "id"
    )
    val exerciseDC: ExerciseDC = ExerciseDC()
)