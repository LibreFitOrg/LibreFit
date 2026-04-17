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
import org.librefit.enums.SetMode
import kotlin.random.Random

/**
 * The [org.librefit.db.entity.Exercise] model used only by the ui. The difference is the use
 * of [ImmutableList] instead of [List] in order to ensure the [Immutable] annotation and improve
 * composition performance.
 *
 * @see [org.librefit.db.entity.Exercise]
 */
@Immutable
data class UiExercise(
    val id: Long = Random.nextLong(),
    val idExerciseDC: String = "",
    val notes: String = "",
    val setMode: SetMode = SetMode.LOAD,
    val restTime: Int = 0,
    val position: Int = 0,
    val workoutId: Long = 0
)
