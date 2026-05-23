/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle

/**
 * The [org.librefit.db.entity.ExerciseDC] model used only by the ui. The difference is the use
 * of [ImmutableList] instead of [List] in order to ensure the [Immutable] annotation and improve
 * composition performance.
 *
 * @see [org.librefit.db.entity.ExerciseDC]
 */
@Immutable
data class UiExerciseDC(
    val id: String = "",
    val name: String = "",
    val force: Force? = null,
    val level: Level = Level.BEGINNER,
    val mechanic: Mechanic? = null,
    val equipment: Equipment? = null,
    val primaryMuscles: ImmutableList<Muscle> = persistentListOf(),
    val secondaryMuscles: ImmutableList<Muscle> = persistentListOf(),
    val instructions: ImmutableList<String> = persistentListOf(),
    val category: Category = Category.POWERLIFTING,
    val images: ImmutableList<String> = persistentListOf(),
    val isCustomExercise: Boolean = false
)