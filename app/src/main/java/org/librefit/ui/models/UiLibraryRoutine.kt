/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.librefit.models.RoutineCategory

/**
 * A routine template of the library as shown in the library screen. It merges a
 * [org.librefit.models.RoutineTemplate] of [org.librefit.models.RoutineCatalog] with the exercise
 * names and the duration computed for the current dataset and language.
 *
 * @property templateId The id of the template inside
 * [org.librefit.models.RoutineCatalog] (e.g. "PPL_Push"), used to copy the template into the user's
 * routines or to hide it from the library.
 * @property title The localized title of the template.
 * @property description The localized description of the template.
 * @property category The section the template belongs to.
 * @property exerciseNames The localized names of the exercises composing the routine, in
 * execution order.
 * @property estimatedMinutes The estimated duration of the whole routine, in minutes.
 */
@Immutable
data class UiLibraryRoutine(
    val templateId: String = "",
    val title: String = "",
    val description: String = "",
    val category: RoutineCategory = RoutineCategory.BEGINNER,
    val exerciseNames: ImmutableList<String> = persistentListOf(),
    val estimatedMinutes: Int = 0
)

/**
 * A section of the library screen: all the [routines] sharing the same [category].
 *
 * @property categoryName The localized category name to display as section header.
 */
@Immutable
data class UiLibraryCategory(
    val category: RoutineCategory = RoutineCategory.BEGINNER,
    val categoryName: String = "",
    val routines: ImmutableList<UiLibraryRoutine> = persistentListOf()
)
