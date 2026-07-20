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
import android.icu.util.LocaleData
import android.icu.util.ULocale
import android.os.Build
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
import org.librefit.enums.userPreferences.UnitSystem
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
private val MATERIAL_MODE_KEY = booleanPreferencesKey("material_mode")
private val KEEP_ON_WORKOUT_SCREEN_KEY = booleanPreferencesKey("workout_screen_on")
private val REQUEST_PERMISSIONS_NEXT_TIME_KEY = booleanPreferencesKey("ask_permission_again")
private val REST_TIMER_SOUND_KEY = booleanPreferencesKey("alert_sound")
private val SHOW_WELCOME_SCREEN_KEY = booleanPreferencesKey("show_welcome_screen")
private val IS_SUPPORTER_KEY = booleanPreferencesKey("is_supporter")
private val PAST_VERSION_CODE_KEY = longPreferencesKey("pastVersionCode")
private val IS_WORKOUT_HEADER_STICKY_KEY = booleanPreferencesKey("is_workout_header_sticky")
private val SHOW_KEEP_ANDROID_OPEN_KEY = booleanPreferencesKey("showKeepAndroidOpenKey")
private val USE_SCROLL_WHEEL_FOR_INPUT_KEY = booleanPreferencesKey("use_number_picker")
private val DISMISS_SCROLL_WHELL_INPUT_AUTOMATICALLY =
    booleanPreferencesKey("dismiss_input_modal_bottom_sheet_automatically_key")
private val SHOW_EXERCISES_IMAGES_KEY = booleanPreferencesKey("show_exercises_images_key")
private val UNIT_SYSTEM_KEY = stringPreferencesKey("unit_system")
/**
 * Central repository managing application-level preferences, including theme, unit systems, and language.
 *
 * It utilizes [DataStore] for persistent storage of user settings and integrates with [AppCompatDelegate]
 * for reactive, system-compliant per-app language management. For unit systems, it leverages
 * [android.icu.util.LocaleData.MeasurementSystem] to infer the user's preferred standard (Metric/Imperial)
 * based on their locale.
 *
 * ### Language Management
 * - **Persistence**: Language tags are managed via [AppCompatDelegate.setApplicationLocales], which
 *   automatically syncs with Android's per-app language system settings.
 * - **Observation**: Current language state is monitored reactively through the [currentLocale]
 *   [Flow], triggered by configuration changes.
 *
 * @see <a href="https://developer.android.com/reference/android/icu/util/LocaleData.MeasurementSystem">LocaleData.MeasurementSystem</a>
 * @see <a href="https://developer.android.com/guide/topics/resources/app-languages">Per-app languages in system settings</a>
 * @see <a href="https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate">AppCompatDelegate</a>
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

    val showExercisesImages: StateFlow<Boolean?> = dataStore.data
        .map { preferences -> preferences[SHOW_EXERCISES_IMAGES_KEY] }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val dismissScrollWheelInputAutomatically: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[DISMISS_SCROLL_WHELL_INPUT_AUTOMATICALLY] == true }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val unitSystem: StateFlow<UnitSystem> = dataStore.data
        .map { preferences ->
            runCatching {
                UnitSystem.valueOf(preferences[UNIT_SYSTEM_KEY]!!)
            }.getOrDefault(resolveDefaultUnitSystem())
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = resolveDefaultUnitSystem()
        )

    private fun resolveDefaultUnitSystem(): UnitSystem {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Use the ICU LocaleData API to get the measurement system for this locale
            when (LocaleData.getMeasurementSystem(ULocale.getDefault())) {
                LocaleData.MeasurementSystem.US -> UnitSystem.IMPERIAL
                LocaleData.MeasurementSystem.UK -> UnitSystem.IMPERIAL
                LocaleData.MeasurementSystem.SI -> UnitSystem.METRIC
                else -> UnitSystem.METRIC
            }
        } else {
            // These countries are the primary users of the Imperial system.
            // US: United States
            // UK: United Kingdom
            // MM: Myanmar
            // LR: Liberia
            val imperialCountries = setOf("US", "UK", "MM", "LR")

            if (Locale.getDefault().country in imperialCountries) {
                UnitSystem.IMPERIAL
            } else {
                UnitSystem.METRIC
            }
        }
    }

    /**
     * Resolves the current Application Locale into our [Language] enum.
     */
    private fun resolveLanguage(locale: Locale?): Language {
        if (locale == null) return Language.SYSTEM

        val tag = locale.toLanguageTag()
        return Language.entries.find { it.code.equals(tag, ignoreCase = true) }
            ?: Language.entries.find { it.code.equals(locale.language, ignoreCase = true) }
            ?: Language.SYSTEM
    }

    /**
     * Helper to read the exact synchronous language state.
     */
    private fun getCurrentLanguage(): Language {
        val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)
        return resolveLanguage(currentLocale)
    }

    /**
     * A Flow that emits the new Locale whenever the app's configuration changes.
     */
    private val currentLocale: Flow<Locale?> = callbackFlow {
        // Emit current state
        trySend(AppCompatDelegate.getApplicationLocales()[0])

        val callback = object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                trySend(AppCompatDelegate.getApplicationLocales()[0])
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
        .map { resolveLanguage(it) }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = getCurrentLanguage()
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

    fun saveLanguage(language: Language) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))
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

    suspend fun saveShowExercisesImages(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_EXERCISES_IMAGES_KEY] = show
        }
    }

    suspend fun saveDismissScrollWheelInputAutomatically(dismissAutomatically: Boolean) {
        dataStore.edit { preferences ->
            preferences[DISMISS_SCROLL_WHELL_INPUT_AUTOMATICALLY] = dismissAutomatically
        }
    }

    suspend fun saveUnitSystem(system: UnitSystem) {
        dataStore.edit { preferences -> preferences[UNIT_SYSTEM_KEY] = system.name }
    }
}
