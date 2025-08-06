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

package org.librefit.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.Language
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {
    val themeMode = userPreferences.themeMode
    val materialMode = userPreferences.materialMode
    val keepScreenOn = userPreferences.workoutScreenOn
    val language = userPreferences.language

    fun changeLanguage(language: Language) {
        savePreference(UserPreferencesRepository.languageKey, language.code)
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }


    fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            userPreferences.savePreference(
                key = key,
                value = value
            )
        }
    }
}