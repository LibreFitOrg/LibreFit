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

package org.librefit.ui.screens.requestPermission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.librefit.db.repository.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class RequestPermissionScreenViewModel @Inject constructor(
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {
    val requestPermissionAgain: StateFlow<Boolean> = userPreferences.requestPermissionsAgain


    fun saveRequestPermissionAgainPreference(value: Boolean) {
        viewModelScope.launch {
            userPreferences.savePreference(
                key = UserPreferencesRepository.requestPermissionsAgainKey,
                value = value
            )
        }
    }
}