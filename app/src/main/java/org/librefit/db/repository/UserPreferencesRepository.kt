/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
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

private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
private val MATERIAL_MODE_KEY = booleanPreferencesKey("material_mode")
private val KEEP_ON_WORKOUT_SCREEN_KEY = booleanPreferencesKey("workout_screen_on")
private val REQUEST_PERMISSIONS_NEXT_TIME_KEY = booleanPreferencesKey("ask_permission_again")
private val LANGUAGE_KEY = stringPreferencesKey("language")
private val REST_TIMER_SOUND_KEY = booleanPreferencesKey("alert_sound")
private val SHOW_WELCOME_SCREEN_KEY = booleanPreferencesKey("show_welcome_screen")
private val IS_SUPPORTER_KEY = booleanPreferencesKey("is_supporter")
private val PAST_VERSION_CODE_KEY = longPreferencesKey("pastVersionCode")
private val IS_WORKOUT_HEADER_STICKY_KEY = booleanPreferencesKey("is_workout_header_sticky")
private val SHOW_KEEP_ANDROID_OPEN_KEY = booleanPreferencesKey("showKeepAndroidOpenKey")
private val USE_SCROLL_WHEEL_FOR_INPUT_KEY = booleanPreferencesKey("use_number_picker")
private val DISMISS_SCROLL_WHELL_INPUT_AUTOMATICALLY =
    booleanPreferencesKey("dismiss_input_modal_bottom_sheet_automatically_key")

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

    val themeMode: StateFlow<ThemeMode> = dataStore.data
        .map { preferences ->
            ThemeMode.entries.find { it.value == preferences[THEME_MODE_KEY] } ?: ThemeMode.SYSTEM
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

    val materialMode: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[MATERIAL_MODE_KEY] == true }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val workoutScreenOn: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[KEEP_ON_WORKOUT_SCREEN_KEY] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val requestPermissionsNextTime: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[REQUEST_PERMISSIONS_NEXT_TIME_KEY] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val restTimerSoundOn: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[REST_TIMER_SOUND_KEY] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val showWelcomeScreen: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[SHOW_WELCOME_SCREEN_KEY] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val isSupporter: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[IS_SUPPORTER_KEY] == true }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val pastVersionCode: StateFlow<Long> = dataStore.data
        .map { preferences -> preferences[PAST_VERSION_CODE_KEY] ?: -1L }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = -1L
        )

    val isWorkoutHeaderSticky: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[IS_WORKOUT_HEADER_STICKY_KEY] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val showKeepAndroidOpen: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[SHOW_KEEP_ANDROID_OPEN_KEY] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val useScrollWheelForInput: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[USE_SCROLL_WHEEL_FOR_INPUT_KEY] != false }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val dismissScrollWheelInputAutomatically: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[DISMISS_SCROLL_WHELL_INPUT_AUTOMATICALLY] == true }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    /**
     * A Flow that emits the new Locale whenever the app's configuration changes.
     */
    private val currentLocale: Flow<Locale?> = callbackFlow {
        // Emit current state
        trySend(AppCompatDelegate.getApplicationLocales()[0])

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

        // Unregister the callback when the flow is canceled
        awaitClose {
            application.unregisterComponentCallbacks(callback)
        }
    }.conflate()


    val language: StateFlow<Language> = currentLocale
        .map { newLocale ->
            // If newLanguage is null, follow system otherwise find the associated enum
            newLocale?.language?.let { newLanguage ->
                Language.entries.find { it.code == newLanguage }
            } ?: Language.SYSTEM
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = Language.SYSTEM
        )

    suspend fun saveThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences -> preferences[THEME_MODE_KEY] = mode.value }
    }

    suspend fun saveMaterialMode(isEnabled: Boolean) {
        dataStore.edit { preferences -> preferences[MATERIAL_MODE_KEY] = isEnabled }
    }

    suspend fun saveWorkoutScreenOn(isOn: Boolean) {
        dataStore.edit { preferences -> preferences[KEEP_ON_WORKOUT_SCREEN_KEY] = isOn }
    }

    suspend fun saveRequestPermissionsNextTime(shouldAsk: Boolean) {
        dataStore.edit { preferences -> preferences[REQUEST_PERMISSIONS_NEXT_TIME_KEY] = shouldAsk }
    }

    suspend fun saveLanguage(languageCode: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
        dataStore.edit { preferences -> preferences[LANGUAGE_KEY] = languageCode }
    }

    suspend fun saveRestTimerSoundOn(isOn: Boolean) {
        dataStore.edit { preferences -> preferences[REST_TIMER_SOUND_KEY] = isOn }
    }

    suspend fun saveShowWelcomeScreen(show: Boolean) {
        dataStore.edit { preferences -> preferences[SHOW_WELCOME_SCREEN_KEY] = show }
    }

    suspend fun saveIsSupporter(isSupporter: Boolean) {
        dataStore.edit { preferences -> preferences[IS_SUPPORTER_KEY] = isSupporter }
    }

    suspend fun savePastVersionCode(versionCode: Long) {
        dataStore.edit { preferences -> preferences[PAST_VERSION_CODE_KEY] = versionCode }
    }

    suspend fun saveIsWorkoutHeaderSticky(isSticky: Boolean) {
        dataStore.edit { preferences -> preferences[IS_WORKOUT_HEADER_STICKY_KEY] = isSticky }
    }

    suspend fun saveShowKeepAndroidOpen(show: Boolean) {
        dataStore.edit { preferences -> preferences[SHOW_KEEP_ANDROID_OPEN_KEY] = show }
    }

    suspend fun saveUseScrollWheelForInput(useScroll: Boolean) {
        dataStore.edit { preferences -> preferences[USE_SCROLL_WHEEL_FOR_INPUT_KEY] = useScroll }
    }

    suspend fun saveDismissScrollWheelInputAutomatically(dismissAutomatically: Boolean) {
        dataStore.edit { preferences ->
            preferences[DISMISS_SCROLL_WHELL_INPUT_AUTOMATICALLY] = dismissAutomatically
        }
    }
}
