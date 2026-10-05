/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.models

import androidx.annotation.StringRes
import org.librefit.enums.SetMode

/**
 * A ready-to-use workout routine shipped with the app and shown in the library.
 *
 * Titles and descriptions reference string resources instead of holding raw text so that they
 * can be localized through Weblate like any other string of the app. Because the resources are
 * referenced statically, R8 sees every usage and resource shrinking stays safe.
 *
 * @property id Stable identifier of the template (e.g., "PPL_Push"), persisted in DataStore when
 * the user hides it from the library.
 * @property titleRes The title of the template, localized through [org.librefit.R.string].
 * @property descriptionRes A short summary of the template, shown under the title.
 * @property category The section the template is grouped under in the library screen.
 * @property exercises The exercises composing the routine, in execution order.
 */
data class RoutineTemplate(
    val id: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val category: RoutineCategory,
    val exercises: List<RoutineTemplateExercise>
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
data class RoutineTemplateExercise(
    val idExerciseDC: String,
    val setMode: SetMode,
    val sets: Int,
    val reps: Int = 0,
    val elapsedTime: Int = 0,
    val restTime: Int = 60
)

/**
 * The sections of the library screen, in display order.
 *
 * @property labelRes The localized name of the category, displayed as section header.
 */
enum class RoutineCategory(@param:StringRes val labelRes: Int) {
    BEGINNER(org.librefit.R.string.routine_category_beginner),
    FULL_BODY(org.librefit.R.string.routine_category_full_body),
    PPL(org.librefit.R.string.routine_category_ppl),
    STRENGTH(org.librefit.R.string.routine_category_strength),
    HYPERTROPHY(org.librefit.R.string.routine_category_hypertrophy),
    BODYWEIGHT(org.librefit.R.string.routine_category_bodyweight),
    CARDIO(org.librefit.R.string.routine_category_cardio),
    TARGETED(org.librefit.R.string.routine_category_targeted),
    MOBILITY(org.librefit.R.string.routine_category_mobility)
}
