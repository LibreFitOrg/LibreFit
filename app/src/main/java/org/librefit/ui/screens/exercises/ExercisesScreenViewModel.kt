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

import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.librefit.data.ExerciseDC
import org.librefit.enums.exercise.FilterValue
import org.librefit.util.fuzzySearch.FuzzySearch
import javax.inject.Inject

@HiltViewModel
class ExercisesScreenViewModel @Inject constructor(
    exercisesList: List<ExerciseDC>
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    @OptIn(FlowPreview::class)
    val debouncedQuery: StateFlow<String> = _query
        .debounce(300L)
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }


    private var _filterValue = MutableStateFlow(FilterValue())
    var filterValue = _filterValue.asStateFlow()

    val filteredExerciseList: StateFlow<List<ExerciseDC>> =
        combine(
            debouncedQuery,
            filterValue
        ) { q, _ ->
            exercisesList
                .fastMap { e -> e to fuzzySearch(e.name, q) }
                .fastFilter { (e, score) -> score > 60 && filterExercise(e) }
                .sortedByDescending { it.second }
                .fastMap { it.first }
        }
            .flowOn(Dispatchers.IO)
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = exercisesList
            )


    /**
     * Refer to [FuzzySearch.partialRatio]
     */
    private fun fuzzySearch(name: String, query: String): Int {
        if (query == "") return 100
        return FuzzySearch.partialRatio(name.lowercase(), query.lowercase().trim())
    }

    fun updateFilter(newFilterValue: FilterValue) {
        _filterValue.value = newFilterValue
    }

    private fun filterExercise(exercise: ExerciseDC): Boolean = with(filterValue.value) {
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
}