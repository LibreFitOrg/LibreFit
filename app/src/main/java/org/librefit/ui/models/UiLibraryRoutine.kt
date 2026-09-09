/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import androidx.compose.runtime.Immutable

/**
 * A routine template of the library as shown in the library screen. It merges the workout row
 * persisted with [org.librefit.enums.WorkoutState.LIBRARY] with the metadata parsed from
 * `res/raw/routines.json`.
 *
 * @property workoutId The id of the underlying [org.librefit.db.entity.Workout], used to delete
 * the template from the library or to start it like any other routine.
 * @property templateId The id of the template inside `res/raw/routines.json`
 * (e.g. "PPL_Push").
 * @property title The localized title of the template.
 * @property description The localized description of the template.
 * @property categoryKey The string resource name of the category the template belongs to. It is
 * used to group templates in the library screen.
 * @property exerciseNames The localized names of the exercises composing the routine, in
 * execution order.
 * @property estimatedMinutes The estimated duration of the whole routine, in minutes.
 */
@Immutable
data class UiLibraryRoutine(
    val workoutId: Long = 0,
    val templateId: String = "",
    val title: String = "",
    val description: String = "",
    val categoryKey: String = "",
    val exerciseNames: List<String> = listOf(),
    val estimatedMinutes: Int = 0
)

/**
 * A section of the library screen: all the [routines] sharing the same template category.
 *
 * @property categoryKey The string resource name of the category.
 * @property categoryName The localized category name to display.
 */
@Immutable
data class UiLibraryCategory(
    val categoryKey: String = "",
    val categoryName: String = "",
    val routines: List<UiLibraryRoutine> = listOf()
)
