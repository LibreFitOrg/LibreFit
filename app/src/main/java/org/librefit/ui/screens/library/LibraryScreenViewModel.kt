/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.library

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.repository.DatasetRepository
import org.librefit.db.repository.RoutineTemplateRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.di.qualifiers.IoDispatcher
import org.librefit.enums.SetMode
import org.librefit.models.RoutineCatalog
import org.librefit.models.RoutineCategory
import org.librefit.models.RoutineTemplate
import org.librefit.models.RoutineTemplateExercise
import org.librefit.ui.models.UiLibraryCategory
import org.librefit.ui.models.UiLibraryRoutine

class LibraryScreenViewModel(
    private val routineTemplateRepository: RoutineTemplateRepository,
    private val datasetRepository: DatasetRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    // Application rather than Context: it is the application lifetime, so the ViewModel never
    // outlives it.
    private val application: Application,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    /**
     * Routines of the library grouped by category, following [RoutineCategory] order. Categories
     * without routines are omitted. Localized strings are resolved at emission time and
     * re-resolved on language change (the ViewModel outlives activity recreations).
     */
    val libraryCategories: StateFlow<List<UiLibraryCategory>> =
        combine(
            datasetRepository.dataset,
            userPreferencesRepository.language,
            userPreferencesRepository.hiddenRoutineTemplateIds
        ) { dataset, _, hiddenTemplateIds ->
            // The library is a static catalog: only the templates the user hid are filtered out.
            val visibleTemplates = RoutineCatalog.all.filter { it.id !in hiddenTemplateIds }
            if (visibleTemplates.isEmpty()) return@combine emptyList()

            val exerciseNamesById = dataset.associate { it.id to it.name }

            RoutineCategory.entries.mapNotNull { category ->
                val routines = visibleTemplates
                    .filter { it.category == category }
                    .map { template -> template.toUiRoutine(exerciseNamesById) }
                if (routines.isEmpty()) null
                else UiLibraryCategory(
                    category = category,
                    categoryName = application.getString(category.labelRes),
                    routines = routines.toImmutableList()
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
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
     * Hides the routine template with the passed [templateId] from the library. The template is not
     * deleted, only hidden from the library screen.
     */
    fun setRoutineHidden(templateId: String, isHidden: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            userPreferencesRepository.setRoutineTemplateHidden(templateId, isHidden)
        }
    }

    private fun RoutineTemplate.toUiRoutine(exerciseNamesById: Map<String, String>): UiLibraryRoutine {
        return UiLibraryRoutine(
            templateId = id,
            title = application.getString(titleRes),
            description = application.getString(descriptionRes),
            category = category,
            // Exercises missing from the dataset are skipped, consistently with the copy made by
            // RoutineTemplateRepository.addTemplateAsRoutine.
            exerciseNames = exercises.mapNotNull { exerciseNamesById[it.idExerciseDC] }.toImmutableList(),
            estimatedMinutes = exercises.estimatedMinutes()
        )
    }

    /**
     * A working set is estimated at ~40 seconds when not timed; rest times are known exactly.
     */
    private fun List<RoutineTemplateExercise>.estimatedMinutes(): Int {
        val totalSeconds = sumOf { exercise ->
            val setTime =
                if (exercise.setMode == SetMode.DURATION && exercise.elapsedTime > 0) {
                    exercise.elapsedTime
                } else 40
            exercise.sets * setTime + exercise.restTime * exercise.sets
        }
        return (totalSeconds + 59) / 60
    }
}
