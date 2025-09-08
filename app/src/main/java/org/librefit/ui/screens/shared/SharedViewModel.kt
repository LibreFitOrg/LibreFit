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
import kotlinx.coroutines.launch
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.repository.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private var selectedExercisesList = listOf<ExerciseDC>()

    fun getSelectedExercisesList(): List<ExerciseDC> {
        val list = selectedExercisesList
        selectedExercisesList = emptyList()
        return list
    }

    fun setSelectedExercisesList(exerciseList: List<ExerciseDC>) {
        selectedExercisesList = exerciseList
    }


    val showWelcomeScreen = userPreferencesRepository.showWelcomeScreen

    fun doNotShowWelcomeScreenAgain() {
        viewModelScope.launch {
            userPreferencesRepository.savePreference(
                key = UserPreferencesRepository.showWelcomeScreenKey,
                value = false
            )
        }
    }
}