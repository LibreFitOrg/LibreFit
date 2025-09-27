/*
 * Copyright (c) 2024-2025. LibreFit
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
            userPreferencesRepository.savePreference(
                key = UserPreferencesRepository.showWelcomeScreenKey,
                value = false
            )
        }
    }


    // Used by RequestPermissionScreen
    val requestPermissionNextTime: StateFlow<Boolean> =
        userPreferencesRepository.requestPermissionsNextTime

    fun saveRequestPermissionAgainPreference(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.savePreference(
                key = UserPreferencesRepository.requestPermissionsNextTimeKey,
                value = value
            )
        }
    }


    // Used by SupporterScreen
    val isSupporter: StateFlow<Boolean> = userPreferencesRepository.isSupporter

    fun updateIsSupporter(value: Boolean) {
        viewModelScope.launch {
            // A delay to le the user visualize the successful result
            delay(1000)
            userPreferencesRepository.savePreference(
                key = UserPreferencesRepository.isSupporterKey,
                value = value
            )
        }
    }
}