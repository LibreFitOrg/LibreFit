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
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.librefit.di.qualifiers.ApplicationScope
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import java.time.Instant
import java.time.temporal.ChronoUnit
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
private val HEALTH_CONNECT_ENABLED_KEY = booleanPreferencesKey("health_connect_enabled")
private val IGNORED_HEALTH_CONNECT_RECORD_IDS_KEY =
    stringSetPreferencesKey("ignored_health_connect_record_ids")
private fun healthConnectSyncKey(id: String) = booleanPreferencesKey("health_connect_sync_$id")
private const val IGNORED_RECORD_SEPARATOR = "|"
private const val IGNORED_RECORD_DAYS = 30L

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
    private val healthConnectSyncStates = mutableMapOf<String, StateFlow<Boolean>>()

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

    val healthConnectEnabled: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[HEALTH_CONNECT_ENABLED_KEY] == true }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    fun healthConnectSyncEnabled(id: String): StateFlow<Boolean> =
        synchronized(healthConnectSyncStates) {
            healthConnectSyncStates.getOrPut(id) {
                dataStore.data
                    .map { preferences -> preferences[healthConnectSyncKey(id)] == true }
                    .stateIn(
                        scope = applicationScope,
                        started = SharingStarted.Eagerly,
                        initialValue = false
                    )
            }
        }

    suspend fun isHealthConnectSyncEnabled(id: String): Boolean =
        dataStore.data.first()[healthConnectSyncKey(id)] == true

    suspend fun isHealthConnectEnabled(): Boolean =
        dataStore.data.first()[HEALTH_CONNECT_ENABLED_KEY] == true

    suspend fun getIgnoredHealthConnectRecordIds(): Set<String> {
        val entries = dataStore.data.first()[IGNORED_HEALTH_CONNECT_RECORD_IDS_KEY].orEmpty()
        val cutoff = Instant.now().minus(IGNORED_RECORD_DAYS, ChronoUnit.DAYS).toEpochMilli()
        val activeEntries = entries.filterTo(mutableSetOf()) { it.isActiveIgnoredRecord(cutoff) }

        if (activeEntries.size != entries.size) {
            dataStore.edit { preferences ->
                preferences[IGNORED_HEALTH_CONNECT_RECORD_IDS_KEY] = activeEntries
            }
        }

        return activeEntries.mapTo(mutableSetOf()) { it.substringAfter(IGNORED_RECORD_SEPARATOR) }
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

    suspend fun saveDismissScrollWheelInputAutomatically(dismissAutomatically: Boolean) {
        dataStore.edit { preferences ->
            preferences[DISMISS_SCROLL_WHELL_INPUT_AUTOMATICALLY] = dismissAutomatically
        }
    }

    suspend fun saveHealthConnectEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences -> preferences[HEALTH_CONNECT_ENABLED_KEY] = isEnabled }
    }

    suspend fun saveHealthConnectSyncEnabled(id: String, isEnabled: Boolean) {
        dataStore.edit { preferences -> preferences[healthConnectSyncKey(id)] = isEnabled }
    }

    suspend fun ignoreHealthConnectRecordIds(ids: Set<String>) {
        if (ids.isEmpty()) return
        val now = Instant.now()
        val cutoff = now.minus(IGNORED_RECORD_DAYS, ChronoUnit.DAYS).toEpochMilli()
        dataStore.edit { preferences ->
            val activeEntries = preferences[IGNORED_HEALTH_CONNECT_RECORD_IDS_KEY]
                .orEmpty()
                .filterTo(mutableSetOf()) { entry ->
                    entry.isActiveIgnoredRecord(cutoff) &&
                        entry.substringAfter(IGNORED_RECORD_SEPARATOR) !in ids
                }
            // The sync only covers thirty days, so older ignored IDs are no longer needed.
            preferences[IGNORED_HEALTH_CONNECT_RECORD_IDS_KEY] =
                activeEntries + ids.map { "${now.toEpochMilli()}$IGNORED_RECORD_SEPARATOR$it" }
        }
    }

    private fun String.isActiveIgnoredRecord(cutoff: Long): Boolean {
        val ignoredAt = substringBefore(IGNORED_RECORD_SEPARATOR).toLongOrNull() ?: return false
        val recordId = substringAfter(IGNORED_RECORD_SEPARATOR, missingDelimiterValue = "")
        return ignoredAt >= cutoff && recordId.isNotEmpty()
    }
}
