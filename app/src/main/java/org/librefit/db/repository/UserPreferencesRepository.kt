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
 *
 * LibreFit is subject to additional terms covering author attribution and
 * trademark usage, as found in the accompanying ADDITIONAL_TERMS.md file.
 */

package org.librefit.db.repository

import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.librefit.di.qualifiers.ApplicationScope
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A repository to handle user preferences using [androidx.datastore.core.DataStore].
 *
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    private val application: Application
) {
    companion object {
        val themeModeKey = intPreferencesKey("theme_mode")
        val materialModeKey = booleanPreferencesKey("material_mode")
        val keepOnWorkoutScreenKey = booleanPreferencesKey("workout_screen_on")
        val requestPermissionsNextTimeKey = booleanPreferencesKey("ask_permission_again")
        val languageKey = stringPreferencesKey("language")
        val restTimerSoundKey = booleanPreferencesKey("alert_sound")
        val showWelcomeScreenKey = booleanPreferencesKey("show_welcome_screen")
        val isSupporterKey = booleanPreferencesKey("is_supporter")
        val pastVersionCodeKey = longPreferencesKey("pastVersionCode")
    }

    val themeMode: StateFlow<ThemeMode> = dataStore.data
        .map { preferences ->
            ThemeMode.entries.find { it.value == preferences[themeModeKey] } ?: ThemeMode.SYSTEM
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

    val materialMode: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[materialModeKey] == true }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val workoutScreenOn: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[keepOnWorkoutScreenKey] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val requestPermissionsNextTime: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[requestPermissionsNextTimeKey] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val restTimerSoundOn: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[restTimerSoundKey] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val showWelcomeScreen: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[showWelcomeScreenKey] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val isSupporter: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[isSupporterKey] == true }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val pastVersionCode: StateFlow<Long> = dataStore.data
        .map { preferences -> preferences[pastVersionCodeKey] ?: -1L }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = -1L
        )

    /**
     * A Flow that emits the new Locale whenever the app's configuration changes.
     */
    private val currentLocale: Flow<Locale?> = callbackFlow {
        val callback = object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                // It's null when no app-specific locales are set so LANGUAGE.SYSTEM is chosen
                val currentLocale = AppCompatDelegate.getApplicationLocales()[0]
                // Offer the new locale to the channel
                trySend(currentLocale)
            }

            override fun onLowMemory() {}
        }

        // Register the callback
        application.registerComponentCallbacks(callback)

        // Unregister the callback when the flow is cancelled
        awaitClose {
            application.unregisterComponentCallbacks(callback)
        }
    }.conflate()


    val language: StateFlow<Language> = currentLocale
        .map { newLocale ->
            // If newLanguage is null, follow system otherwise find the associated enum
            newLocale?.language?.let { newLanguage ->
                Language.entries.find { it.code == newLanguage }
                    ?: error("Unknown language: $newLanguage")
            } ?: Language.SYSTEM
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = Language.SYSTEM
        )

    suspend fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        if (key == languageKey && value is String) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(value)
            )
        } else {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }
}