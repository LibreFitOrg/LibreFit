/*
 * Copyright (c) 2025. LibreFit
 *
 * This file is part of LibreFit
 *
 * LibreFit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreFit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreFit.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.librefit.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
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
import org.librefit.enums.exercise.FilterValue
import org.librefit.ui.models.UiExerciseDC
import org.librefit.util.fuzzySearch.FuzzySearch
import javax.inject.Inject

@HiltViewModel
class ExercisesScreenViewModel @Inject constructor(
    datasetRepository: DatasetRepository
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    fun updateQuery(newQuery: String) {
        _query.update { newQuery }
    }

    @OptIn(FlowPreview::class)
    val debouncedQuery: StateFlow<String> = _query
        .debounce(300L)
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )



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
            .distinctUntilChanged()
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
        .distinctUntilChanged()
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
}