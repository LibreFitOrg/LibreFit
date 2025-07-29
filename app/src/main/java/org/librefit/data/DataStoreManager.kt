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

package org.librefit.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.librefit.enums.ThemeMode

private const val USER_PREFERENCES_NAME = "user_preferences"

private val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

/**
 * A manager class for handling user preferences using [androidx.datastore.core.DataStore].
 *
 * This class instance is provided by [org.librefit.di.DataStoreModule].
 */
class DataStoreManager(private val context: Context) {
    companion object {
        val themeModeKey = intPreferencesKey("theme_mode")
        val materialModeKey = booleanPreferencesKey("material_mode")
        val keepOnWorkoutScreenKey = booleanPreferencesKey("workout_screen_on")
        val requestPermissionsAgainKey = booleanPreferencesKey("ask_permission_again")
        val languageKey = stringPreferencesKey("language")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        ThemeMode.entries.find { it.value == preferences[themeModeKey] } ?: ThemeMode.SYSTEM
    }

    val materialMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[materialModeKey] == true
    }

    val workoutScreenOn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[keepOnWorkoutScreenKey] != false
    }

    val requestPermissionsAgain: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[requestPermissionsAgainKey] != false
    }

    suspend fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}