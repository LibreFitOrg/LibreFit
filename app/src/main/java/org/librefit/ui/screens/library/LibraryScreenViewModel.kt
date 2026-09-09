/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.entity.Workout
import org.librefit.db.repository.RoutineTemplateRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.di.qualifiers.IoDispatcher
import org.librefit.enums.SetMode
import org.librefit.enums.WorkoutState
import org.librefit.ui.models.UiLibraryCategory
import org.librefit.ui.models.UiLibraryRoutine
import org.librefit.util.RoutineTemplates
import javax.inject.Inject

/**
 * The categories of the library, in display order. Each value is the name of the string resource
 * used as section header.
 */
private val LIBRARY_CATEGORY_KEYS = listOf(
    "routine_category_beginner",
    "routine_category_full_body",
    "routine_category_ppl",
    "routine_category_strength",
    "routine_category_hypertrophy",
    "routine_category_bodyweight",
    "routine_category_cardio",
    "routine_category_targeted",
    "routine_category_mobility"
)

@HiltViewModel
class LibraryScreenViewModel @Inject constructor(
    private val routineTemplateRepository: RoutineTemplateRepository,
    private val workoutRepository: WorkoutRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stringResourceIds = mutableMapOf<String, Int>()

    private fun resolveStringResourceId(name: String): Int? {
        if (!stringResourceIds.containsKey(name)) {
            stringResourceIds[name] =
                context.resources.getIdentifier(name, "string", context.packageName)
        }
        return stringResourceIds[name]?.takeIf { it != 0 }
    }

    private fun localizedString(name: String, fallback: String): String {
        val resId = resolveStringResourceId(name) ?: return fallback
        return runCatching { context.getString(resId) }.getOrDefault(fallback)
    }

    /**
     * Routines of the library grouped by category, following [LIBRARY_CATEGORY_KEYS] order.
     * Categories without routines are omitted. Localized strings are resolved at emission time
     * and re-resolved on language change (the ViewModel outlives activity recreations).
     */
    val libraryCategories: StateFlow<List<UiLibraryCategory>> =
        combine(
            routineTemplateRepository.libraryWorkouts,
            userPreferencesRepository.language
        ) { workoutsWithExercisesAndSets, _ ->
            val templatesById =
                routineTemplateRepository.routineTemplates.associateBy { it.id }

            val routines = workoutsWithExercisesAndSets.mapNotNull { workoutWithExercisesAndSets ->
                val workout = workoutWithExercisesAndSets.workout
                val templateId =
                    RoutineTemplates.templateIdFromNotes(workout.notes) ?: return@mapNotNull null
                val template = templatesById[templateId]

                UiLibraryRoutine(
                    workoutId = workout.id,
                    templateId = templateId,
                    title = template?.let {
                        localizedString(
                            it.titleKey,
                            fallback = workout.title.ifEmpty { it.titleKey }
                        )
                    } ?: workout.title,
                    description = template?.let { localizedString(it.descriptionKey, "") } ?: "",
                    categoryKey = template?.categoryKey ?: "",
                    exerciseNames = workoutWithExercisesAndSets.exercisesWithSets
                        .sortedBy { it.exercise.position }
                        .map { it.exerciseDC.name },
                    estimatedMinutes = estimateMinutes(
                        workoutWithExercisesAndSets.exercisesWithSets.map { eWs ->
                            ExerciseEstimate(
                                setMode = eWs.exercise.setMode,
                                restTime = eWs.exercise.restTime,
                                setsCount = eWs.sets.size,
                                elapsedTime = eWs.sets.firstOrNull()?.elapsedTime ?: 0
                            )
                        }
                    )
                )
            }

            LIBRARY_CATEGORY_KEYS.mapNotNull { categoryKey ->
                val inCategory = routines.filter { it.categoryKey == categoryKey }
                if (inCategory.isEmpty()) null
                else UiLibraryCategory(
                    categoryKey = categoryKey,
                    categoryName = localizedString(categoryKey, fallback = categoryKey),
                    routines = inCategory.sortedBy { it.title.lowercase() }
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private fun estimateMinutes(exercises: List<ExerciseEstimate>): Int {
        // A working set is estimated at ~40 seconds when not timed; rest times are known exactly
        val totalSeconds = exercises.sumOf { estimate ->
            val setTime =
                if (estimate.setMode == SetMode.DURATION && estimate.elapsedTime > 0) {
                    estimate.elapsedTime
                } else 40
            estimate.setsCount * setTime + estimate.restTime * estimate.setsCount
        }
        return (totalSeconds + 59) / 60
    }

    private data class ExerciseEstimate(
        val setMode: SetMode,
        val restTime: Int,
        val setsCount: Int,
        val elapsedTime: Int
    )

    private val _justAddedRoutineIds = MutableStateFlow<Set<String>>(emptySet())
    val justAddedRoutineIds = _justAddedRoutineIds.asStateFlow()

    /**
     * Creates an independent copy of the routine template with the passed [templateId] into the
     * user's routines.
     */
    fun addToMyRoutines(templateId: String) {
        viewModelScope.launch(ioDispatcher) {
            val created = routineTemplateRepository.addTemplateAsRoutine(templateId)

            if (created) {
                _justAddedRoutineIds.update { it + templateId }
            }
        }
    }

    /**
     * Removes the routine template from the library. It will be restored on the next app update.
     */
    fun deleteLibraryRoutine(workout: UiLibraryRoutine) {
        viewModelScope.launch(ioDispatcher) {
            workoutRepository.deleteWorkout(
                Workout(id = workout.workoutId, state = WorkoutState.LIBRARY)
            )
        }
    }
}
