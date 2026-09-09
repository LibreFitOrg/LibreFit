/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.util

/**
 * Utilities shared by routine templates (see `res/raw/routines.json`).
 *
 * Templates are stored in the `workouts` table with [org.librefit.enums.WorkoutState.LIBRARY].
 * Since titles, descriptions and categories must stay localizable, the database rows only keep a
 * reference to the template id ([TEMPLATE_ID_NOTES_PREFIX] marker stored in
 * [org.librefit.db.entity.Workout.notes]); the localized strings are resolved at display time from
 * `strings.xml` through the parsed templates.
 */
object RoutineTemplates {

    /**
     * Marker prefixed to [org.librefit.db.entity.Workout.notes] for library workouts,
     * followed by the template id (e.g. `@template:PPL_Push`).
     */
    const val TEMPLATE_ID_NOTES_PREFIX = "@template:"

    /**
     * Returns the notes value linking a workout to the template with the passed [templateId].
     */
    fun encodeNotes(templateId: String): String = "$TEMPLATE_ID_NOTES_PREFIX$templateId"

    /**
     * Extracts the template id from workout [notes], or null when the workout is not a template.
     */
    fun templateIdFromNotes(notes: String?): String? {
        return notes?.takeIf { it.startsWith(TEMPLATE_ID_NOTES_PREFIX) }
            ?.substringAfter(TEMPLATE_ID_NOTES_PREFIX)
            ?.takeIf { it.isNotEmpty() }
    }
}
