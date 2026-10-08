/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.calendar

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class CalendarScreenViewModel(
    workoutRepository: WorkoutRepository,
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