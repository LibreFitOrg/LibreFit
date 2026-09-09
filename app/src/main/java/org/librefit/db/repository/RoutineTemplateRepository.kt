/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.db.repository

import android.content.Context
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.librefit.R
import org.librefit.db.dao.DatasetDao
import org.librefit.db.dao.WorkoutDao
import org.librefit.db.entity.Exercise
import org.librefit.db.entity.Set
import org.librefit.db.entity.Workout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.di.qualifiers.ApplicationScope
import org.librefit.enums.SetMode
import org.librefit.enums.WorkoutState
import org.librefit.models.RoutineTemplate
import org.librefit.util.RoutineTemplates
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Repository class to provide `res/raw/routines.json` as a [List] of [RoutineTemplate] and to
 * manage their persistence as [Workout]s with [WorkoutState.LIBRARY].
 *
 * Templates are synced into the database on every app update (see [updateRoutinesOnAppUpdate]);
 * only templates which are not in the database yet are inserted, so user deletions are preserved.
 * Each template keeps a reference to its template id through the marker stored in
 * [Workout.notes] (see [RoutineTemplates]), while localized strings live in string resources.
 *
 * @param workoutDao The [WorkoutDao] used to access workouts from the database.
 * @param datasetDao The [DatasetDao] used to resolve exercises referenced by the templates.
 * @param applicationScope A long-lived coroutine on the application scope.
 * @param userPreferencesRepository Used to check whether routines were already synced for the
 * current app version.
 * @property routineTemplates The routine templates parsed from `res/raw/routines.json`.
 */
@Singleton
class RoutineTemplateRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val datasetDao: DatasetDao,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    private val userPreferencesRepository: UserPreferencesRepository,
    @param:ApplicationContext private val context: Context
) {
    private companion object {
        const val TAG = "RoutineTemplateRepo"
    }

    /**
     * The routine templates parsed from `res/raw/routines.json`. Parsed lazily on first access,
     * which always happens on a background coroutine (app startup sync or UI collection).
     */
    val routineTemplates: List<RoutineTemplate> by lazy { loadTemplates() }

    /**
     * The library workouts with their exercises and sets (one per template currently visible in
     * the library).
     */
    val libraryWorkouts: Flow<List<WorkoutWithExercisesAndSets>> =
        workoutDao.getWorkoutsWithExercisesAndSetsByState(WorkoutState.LIBRARY)

    fun updateRoutinesOnAppUpdate() {
        applicationScope.launch(Dispatchers.IO) {
            updateRoutinesOnAppUpdateSynchronously()
        }
    }

    /**
     * Same as [updateRoutinesOnAppUpdate] but it suspends until completed, so callers can run it
     * right after the dataset update (templates reference exercises by id).
     */
    suspend fun updateRoutinesOnAppUpdateSynchronously() {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val currentVersion =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else pInfo.versionCode.toLong()
        val pastVersion = userPreferencesRepository.pastRoutinesVersionCode.value

        // Update routines only on app update. A dedicated version code is used (instead of the
        // dataset's one) so that this repository stays decoupled from DatasetRepository.
        if (pastVersion == currentVersion) return

        val templates = routineTemplates
        val existingTemplateIds = workoutDao
            .getWorkoutsWithExercisesAndSetsListByState(WorkoutState.LIBRARY)
            .mapNotNull { RoutineTemplates.templateIdFromNotes(it.workout.notes) }
            .toSet()

        Log.i(TAG, "Sync: ${templates.size} templates parsed, ${existingTemplateIds.size} already present")

        var failures = 0
        templates
            .filter { it.id !in existingTemplateIds }
            .forEach { template ->
                try {
                    addTemplateToLibrary(template)
                } catch (e: Exception) {
                    // A single broken template must not abort the whole sync nor block future
                    // app updates; the version is still saved and the template can be fixed in
                    // a later release.
                    failures++
                    Log.e(TAG, "Failed to insert routine template '${template.id}'", e)
                }
            }

        if (failures > 0) Log.w(TAG, "$failures routine template(s) failed to be inserted")

        userPreferencesRepository.savePastRoutinesVersionCode(currentVersion)
    }

    private fun loadTemplates(): List<RoutineTemplate> {
        val jsonFile =
            context.resources.openRawResource(R.raw.routines).bufferedReader().use { it.readText() }

        // All entries of all enums must be annotated with @SerialName with its corresponding value in json file
        return Json.decodeFromString(jsonFile)
    }

    private suspend fun addTemplateToLibrary(template: RoutineTemplate) {
        val titleResId = getStringResourceId(template.titleKey)

        workoutDao.addWorkoutWithExercisesAndSets(
            WorkoutWithExercisesAndSets(
                workout = Workout(
                    notes = RoutineTemplates.encodeNotes(template.id),
                    title = titleResId?.let { context.getString(it) } ?: template.titleKey,
                    state = WorkoutState.LIBRARY
                ),
                exercisesWithSets = buildExerciseWithSets(template)
            )
        )
    }

    /**
     * Adds a copy of the template with the passed [templateId] to the user's routines, resolving
     * localized strings at copy time so that the resulting routine is fully owned and editable by
     * the user.
     *
     * @return true when the routine was created, false when the template is unknown or its
     * exercises are not available anymore.
     */
    suspend fun addTemplateAsRoutine(templateId: String): Boolean {
        val template = routineTemplates.find { it.id == templateId } ?: return false

        val titleResId = getStringResourceId(template.titleKey)
        val descriptionResId = getStringResourceId(template.descriptionKey)

        val exercisesWithSets = buildExerciseWithSets(template)
        if (exercisesWithSets.isEmpty()) return false

        workoutDao.addWorkoutWithExercisesAndSets(
            WorkoutWithExercisesAndSets(
                workout = Workout(
                    notes = descriptionResId?.let { context.getString(it) } ?: "",
                    title = titleResId?.let { context.getString(it) } ?: template.titleKey,
                    state = WorkoutState.ROUTINE,
                    routineId = Random.nextLong()
                ),
                exercisesWithSets = exercisesWithSets
            )
        )
        return true
    }

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

    /**
     * Resolves a string resource name (e.g. `routine_ppl_push_title`) to its resource id,
     * returning null when the resource does not exist.
     */
    private fun getStringResourceId(name: String): Int? {
        val id = context.resources.getIdentifier(name, "string", context.packageName)
        return id.takeIf { it != 0 }
    }
}
