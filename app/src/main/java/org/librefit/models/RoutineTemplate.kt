/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.models

import kotlinx.serialization.Serializable
import org.librefit.enums.SetMode

/**
 * This data class stores routine templates as parsed from `res/raw/routines.json`.
 * `kotlinx.serialization` is used for JSON deserialization as indicated by the [Serializable]
 * annotation.
 *
 * A template is a ready-to-use workout routine shipped with the app and shown in the library.
 * Titles, descriptions and categories are not stored here: they are string resource keys
 * (e.g. `routine_ppl_push_title`) declared in `strings.xml` so that they can be localized
 * through Weblate like any other string of the app.
 *
 * The JSON schema associated with this file is defined in `schemas/routines-schema.json` and it
 * can be validated with `validate_routines_json.py`.
 *
 * @property id Unique identifier of the template (e.g., "PPL_Push"). Must match the pattern
 * "^[0-9a-zA-Z_-]+$" and be unique across the file.
 * @property titleKey The name of the string resource used as template title.
 * @property descriptionKey The name of the string resource used as template description.
 * @property categoryKey The name of the string resource of the category the template belongs to.
 * It is used to group templates in the library screen.
 * @property exercises The list of [RoutineTemplateExercise]s composing the routine, in execution order.
 */
@Serializable
data class RoutineTemplate(
    val id: String = "",
    val titleKey: String = "",
    val descriptionKey: String = "",
    val categoryKey: String = "",
    val exercises: List<RoutineTemplateExercise> = listOf()
)

/**
 * A single exercise entry inside a [RoutineTemplate].
 *
 * @property idExerciseDC The id of the exercise in the exercises dataset (`exercises.json`),
 * referenced by [org.librefit.db.entity.Exercise.idExerciseDC].
 * @property setMode How each set of this exercise is tracked.
 * @property sets Number of sets to perform.
 * @property reps Suggested number of repetitions per set. Only meaningful when [setMode] is not
 * [SetMode.DURATION]. The load is intentionally left empty so that users pick their own weights.
 * @property elapsedTime Suggested duration of each set in seconds. Only meaningful when
 * [setMode] is [SetMode.DURATION].
 * @property restTime Rest time in seconds after each set.
 */
@Serializable
data class RoutineTemplateExercise(
    val idExerciseDC: String = "",
    val setMode: SetMode = SetMode.LOAD,
    val sets: Int = 3,
    val reps: Int = 0,
    val elapsedTime: Int = 0,
    val restTime: Int = 60
)
