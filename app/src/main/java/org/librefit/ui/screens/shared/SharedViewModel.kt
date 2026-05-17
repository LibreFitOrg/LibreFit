/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.repository.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    // Used by ExercisesScreen and EditWorkout/WorkoutScreen
    private var selectedExercisesList = listOf<ExerciseDC>()

    fun getSelectedExercisesList(): List<ExerciseDC> {
        val list = selectedExercisesList
        selectedExercisesList = emptyList()
        return list
    }

    fun setSelectedExercisesList(exerciseList: List<ExerciseDC>) {
        selectedExercisesList = exerciseList
    }


    // Used by WelcomeScreen
    val showWelcomeScreen = userPreferencesRepository.showWelcomeScreen

    fun doNotShowWelcomeScreenAgain() {
        viewModelScope.launch {
            userPreferencesRepository.saveShowWelcomeScreen(false)
        }
    }


    // Used by RequestPermissionScreen
    val requestPermissionNextTime: StateFlow<Boolean> =
        userPreferencesRepository.requestPermissionsNextTime

    fun saveRequestPermissionAgainPreference(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveRequestPermissionsNextTime(value)
        }
    }


    // Used by SupporterScreen
    val isSupporter: StateFlow<Boolean> = userPreferencesRepository.isSupporter

    fun updateIsSupporter(value: Boolean) {
        viewModelScope.launch {
            // A delay to le the user visualize the successful result
            delay(1000)
            userPreferencesRepository.saveIsSupporter(value)
        }
    }
}