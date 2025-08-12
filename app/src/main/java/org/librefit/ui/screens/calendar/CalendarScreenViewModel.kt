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

package org.librefit.ui.screens.calendar

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.librefit.db.repository.WorkoutRepository
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.models.mappers.toUi
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class CalendarScreenViewModel @Inject constructor(
    workoutRepository: WorkoutRepository
) : ViewModel() {
    private val workoutsList = workoutRepository.completedWorkouts
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedDateInMillis = MutableStateFlow<Long?>(null)
    val selectedDateInMillis = _selectedDateInMillis.asStateFlow()

    fun updateSelectedDateInMillis(newSelectedDateInMillis: Long?) {
        _selectedDateInMillis.update { newSelectedDateInMillis }
    }


    val workoutsFromDate: StateFlow<List<UiWorkout>> = combine(
        workoutsList,
        selectedDateInMillis
    ) { workouts, dateInMillis ->
        if (dateInMillis == null) {
            emptyList()
        } else {
            val date = Instant.ofEpochMilli(dateInMillis).atZone(ZoneOffset.UTC).toLocalDate()

            workouts.filter { it.completed.toLocalDate() == date }.map { it.toUi() }
        }
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    @OptIn(ExperimentalMaterial3Api::class)
    val selectableDates: StateFlow<SelectableDates> = workoutsList
        .map { list ->
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date =
                        Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    return list.map { it.completed.toLocalDate() }.contains(date)
                }
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatePickerDefaults.AllDates
        )

    @OptIn(ExperimentalMaterial3Api::class)
    val yearRange: StateFlow<IntRange> = workoutsList
        .map { list ->
            if (list.isEmpty()) {
                DatePickerDefaults.YearRange
            } else {
                val maxYear = list.maxOf { it.completed.toLocalDate().year }
                val minYear = list.minOf { it.completed.toLocalDate().year }
                (minYear..maxYear)
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatePickerDefaults.YearRange
        )
}