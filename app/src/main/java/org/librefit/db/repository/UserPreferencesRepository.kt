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

package org.librefit.db.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.librefit.di.qualifiers.ApplicationScope
import org.librefit.enums.Language
import org.librefit.enums.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A repository to handle user preferences using [androidx.datastore.core.DataStore].
 *
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:ApplicationScope private val applicationScope: CoroutineScope
) {
    companion object {
        val themeModeKey = intPreferencesKey("theme_mode")
        val materialModeKey = booleanPreferencesKey("material_mode")
        val keepOnWorkoutScreenKey = booleanPreferencesKey("workout_screen_on")
        val requestPermissionsNextTimeKey = booleanPreferencesKey("ask_permission_again")
        val languageKey = stringPreferencesKey("language")
    }

    val themeMode: StateFlow<ThemeMode> = dataStore.data
        .map { preferences ->
            ThemeMode.entries.find { it.value == preferences[themeModeKey] } ?: ThemeMode.SYSTEM
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Companion.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

    val materialMode: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[materialModeKey] == true }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Companion.Eagerly,
            initialValue = false
        )

    val workoutScreenOn: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[keepOnWorkoutScreenKey] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Companion.Eagerly,
            initialValue = false
        )

    val requestPermissionsNextTime: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[requestPermissionsNextTimeKey] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Companion.Eagerly,
            initialValue = false
        )

    val language: StateFlow<Language> = dataStore.data
        .map { preferences ->
            Language.entries.find { it.code == preferences[languageKey] } ?: Language.SYSTEM
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Companion.Eagerly,
            initialValue = Language.SYSTEM
        )

    suspend fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}