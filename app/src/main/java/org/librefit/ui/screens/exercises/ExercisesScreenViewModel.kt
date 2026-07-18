/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.librefit.db.repository.DatasetRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.exercise.FilterValue
import org.librefit.ui.models.UiExerciseDC
import org.librefit.util.fuzzySearch.FuzzySearch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ExercisesScreenViewModel @Inject constructor(
    datasetRepository: DatasetRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val showExercisesImages = userPreferencesRepository.showExercisesImages

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    fun updateQuery(newQuery: String) {
        _query.update { newQuery }
    }

    @OptIn(FlowPreview::class)
    val debouncedQuery: Flow<String> = _query
        .debounce(300.milliseconds)
        .distinctUntilChanged()



    private var _filterValue = MutableStateFlow(FilterValue())
    var filterValue = _filterValue.asStateFlow()

    fun updateFilter(newFilterValue: FilterValue) {
        _filterValue.update { newFilterValue }
    }

    val dataset: StateFlow<List<UiExerciseDC>> = datasetRepository.dataset

    val filteredExerciseList: StateFlow<List<UiExerciseDC>> =
        combine(
            debouncedQuery,
            filterValue,
            dataset
        ) { q, f, dataset ->
            dataset
                .map { e -> e to fuzzySearch(e.name, q) }
                .filter { (e, score) -> score > 60 && filterExercise(e, f) }
                .sortedByDescending { it.second }
                .map { it.first }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = dataset.value
            )


    /**
     * Refer to [FuzzySearch.partialRatio]
     */
    private fun fuzzySearch(name: String, query: String): Int {
        if (query == "") return 100
        return FuzzySearch.partialRatio(name.lowercase(), query.lowercase().trim())
    }

    private fun filterExercise(exercise: UiExerciseDC, filterValue: FilterValue): Boolean =
        with(filterValue) {
        when {
            (level != null && level != exercise.level) -> false
            (force != null && force != exercise.force) -> false
            (mechanic != null && mechanic != exercise.mechanic) -> false
            (equipment != null && equipment != exercise.equipment) -> false
            (muscles != null && muscles !in exercise.primaryMuscles
                    && muscles !in exercise.secondaryMuscles) -> false
            (category != null && category != exercise.category) -> false
            (showOnlyCustomExercises && !exercise.isCustomExercise) -> false
            else -> true
        }
    }

    // Store IDs in a Set for fast lookups
    private val _selectedExerciseIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedExerciseIds = _selectedExerciseIds.asStateFlow()

    val selectedExercises: StateFlow<List<UiExerciseDC>> = combine(
        selectedExerciseIds,
        dataset
    ) { ids, list -> list.filter { it.id in ids } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleSelectedExercise(id: String) {
        _selectedExerciseIds.update { currentIds ->
            if (id in currentIds) {
                currentIds - id
            } else {
                currentIds + id
            }
        }
    }


    val isSupporter: StateFlow<Boolean> = userPreferencesRepository.isSupporter
}