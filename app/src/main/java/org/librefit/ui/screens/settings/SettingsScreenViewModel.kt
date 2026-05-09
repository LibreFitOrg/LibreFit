/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.repository.ImportExportRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.userPreferences.DialogPreference
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.models.BackupEvent
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val userPreferences: UserPreferencesRepository,
    private val importExportRepository: ImportExportRepository
) : ViewModel() {
    val themeMode = userPreferences.themeMode
    val materialMode = userPreferences.materialMode
    val keepScreenOn = userPreferences.workoutScreenOn
    val language = userPreferences.language
    val restTimerSoundOn = userPreferences.restTimerSoundOn
    val isSupporter = userPreferences.isSupporter
    val isWorkoutHeaderSticky = userPreferences.isWorkoutHeaderSticky

    private val _isImporting = MutableStateFlow(false)
    val isImporting = _isImporting.asStateFlow()

    fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            userPreferences.savePreference(
                key = key,
                value = value
            )
        }
    }


    private val _preferences = MutableStateFlow<List<DialogPreference>?>(null)
    val preferences = _preferences.asStateFlow()

    fun updatePreferences(preferences: List<DialogPreference>?) {
        _preferences.update { current ->
            preferences?.ifEmpty { current }
        }
    }

    val currentPreference: StateFlow<DialogPreference?> = combine(
        preferences,
        language,
        themeMode
    ) { p, l, t ->
        p?.let {
            when (p.first()) {
                is Language -> l
                is ThemeMode -> t
            }
        }
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun updateDialogPreference(newPreference: DialogPreference) {
        when (newPreference) {
            is Language -> savePreference(
                UserPreferencesRepository.languageKey,
                newPreference.code
            )
            is ThemeMode -> savePreference(
                UserPreferencesRepository.themeModeKey,
                newPreference.value
            )
        }
    }

    fun backupExport(uri: Uri) {
        viewModelScope.launch {
            try {
                importExportRepository.exportTo(uri)
            } catch (e: Exception) {
                _events.emit(
                    BackupEvent.Error(e.message ?: "Data backup export failed")
                )
            }
        }
    }

    fun backupImport(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                importExportRepository.importFrom(uri)
            } catch (e: Exception) {
                _events.emit(
                    BackupEvent.Error(e.message ?: "Data backup restore failed")
                )
            } finally {
                _isImporting.value = false
            }
        }
    }
}