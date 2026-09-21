/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.librefit.db.entity.Workout
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.models.mappers.toUi
import org.librefit.ui.models.moveWorkout

class HomeScreenViewModel(
    private val userPreferences: UserPreferencesRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    val requestPermissionNextTime: StateFlow<Boolean> = userPreferences.requestPermissionsNextTime


    init {
        viewModelScope.launch {
            workoutRepository.routines.collect { newValue ->
                _routines.value = newValue
            }
        }
    }

    val _routines = MutableStateFlow<List<Workout>>(emptyList())
    val routines = _routines.asStateFlow()
        .map { list -> list.map { it.toUi() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val runningWorkout = workoutRepository.runningWorkoutsWithExercisesAndSets
        .map { list -> list.firstOrNull()?.workout?.toUi() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val showKeepAndroidOpen = userPreferences.showKeepAndroidOpen

    fun saveKeepOpenAndroidCheckbox(showAgain : Boolean) {
        viewModelScope.launch {
            userPreferences.saveShowKeepAndroidOpen(!showAgain)
        }
    }


    fun deleteRunningWorkout() {
        viewModelScope.launch {
            runningWorkout.value?.let {
                workoutRepository.deleteWorkout(it.toEntity())
            }
        }
    }

    fun moveRoutine(fromIndex: Int, toIndex: Int) {
        val currentRoutines = routines.value
        val reordered = currentRoutines.moveWorkout(fromIndex, toIndex)

        _routines.value = reordered.map { it.toEntity() }

        viewModelScope.launch {
            workoutRepository.updateWorkoutPositions(reordered.map { it.toEntity() })
        }
    }

}