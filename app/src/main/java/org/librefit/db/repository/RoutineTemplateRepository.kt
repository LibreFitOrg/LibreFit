/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.repository

import android.content.Context
import org.librefit.db.dao.DatasetDao
import org.librefit.db.dao.WorkoutDao
import org.librefit.db.entity.Exercise
import org.librefit.db.entity.Set
import org.librefit.db.entity.Workout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.enums.SetMode
import org.librefit.enums.WorkoutState
import org.librefit.models.RoutineCatalog
import org.librefit.models.RoutineTemplate

/**
 * Repository providing the routine template library and turning a template into a routine owned by
 * the user.
 *
 * Templates are static assets shipped with the app (see [RoutineCatalog]) and are deliberately not
 * persisted in the database: the library is a read-only catalog, and the only user preference
 * involved is the set of hidden template ids kept by [UserPreferencesRepository]. A template
 * reaches the database only when the user copies it, through [addTemplateAsRoutine].
 *
 * @param workoutDao The [WorkoutDao] used to persist the routines created from a template.
 * @param datasetDao The [DatasetDao] used to resolve the exercises referenced by the templates.
 * @param context Used to resolve the localized strings of a template.
 */
class RoutineTemplateRepository(
    private val workoutDao: WorkoutDao,
    private val datasetDao: DatasetDao,
    private val context: Context,
) {

    /**
     * Copies the template with the passed [templateId] into the user's routines, resolving localized
     * strings at copy time so that the resulting routine is fully owned and editable by the user.
     *
     * This is `suspend` rather than fire-and-forget so the caller can await the result and tell the
     * user when the copy failed.
     *
     * @return true when the routine was created, false when the template is unknown or none of its
     * exercises is available in the dataset.
     */
    suspend fun addTemplateAsRoutine(templateId: String): Boolean {
        val template = RoutineCatalog.findById(templateId) ?: return false

        val exercisesWithSets = buildExerciseWithSets(template)
        if (exercisesWithSets.isEmpty()) return false

        workoutDao.addWorkoutWithExercisesAndSets(
            WorkoutWithExercisesAndSets(
                workout = Workout(
                    notes = context.getString(template.descriptionRes),
                    title = context.getString(template.titleRes),
                    state = WorkoutState.ROUTINE
                ),
                exercisesWithSets = exercisesWithSets
            )
        )
        return true
    }

    /**
     * Resolves the exercises of [template] against the dataset, skipping the ones which are not
     * available so that an outdated reference never prevents the routine from being created.
     */
    private suspend fun buildExerciseWithSets(template: RoutineTemplate): List<ExerciseWithSets> {
        return template.exercises.mapIndexedNotNull { index, templateExercise ->
            val exerciseDC = datasetDao.getExerciseFromId(templateExercise.idExerciseDC)
                ?: return@mapIndexedNotNull null

            val exercise = Exercise(
                idExerciseDC = exerciseDC.id,
                setMode = templateExercise.setMode,
                restTime = templateExercise.restTime,
                position = index
            )

            // Suggested reps/durations are pre-filled; loads stay empty for the user to fill
            val sets = List(templateExercise.sets) {
                Set(
                    reps = if (templateExercise.setMode != SetMode.DURATION) templateExercise.reps else 0,
                    elapsedTime = if (templateExercise.setMode == SetMode.DURATION) templateExercise.elapsedTime else 0
                )
            }

            ExerciseWithSets(exercise = exercise, sets = sets, exerciseDC = exerciseDC)
        }
    }
}