/*
 * Copyright (c) 2025. LibreFit
 *
 * This file is part of LibreFit
 *
 * LibreFit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreFit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreFit.  If not, see <https://www.gnu.org/licenses/>.
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
    val id: Long = Random.Default.nextLong() + System.currentTimeMillis(),
    val idExerciseDC: String = "",
    val notes: String = "",
    val setMode: SetMode = SetMode.LOAD,
    val restTime: Int = 0,
    val workoutId: Long = 0
)