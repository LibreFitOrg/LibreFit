/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.serialization.SerializationException
import org.librefit.db.repository.ImportExportRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.di.streamProvider.StreamProvider
import org.librefit.di.uriAccess.UriAccess
import org.librefit.enums.userPreferences.DialogPreference
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.enums.userPreferences.UnitSystem
import java.io.IOException

class SettingsScreenViewModel(
    private val userPreferences: UserPreferencesRepository,
    private val importExportRepository: ImportExportRepository,
    private val streamProvider: StreamProvider,
    private val uriAccess: UriAccess,
) : ViewModel() {
    val themeMode = userPreferences.themeMode
    val materialMode = userPreferences.materialMode
    val keepScreenOn = userPreferences.workoutScreenOn
    val language = userPreferences.language
    val restTimerSoundOn = userPreferences.restTimerSoundOn
    val isSupporter = userPreferences.isSupporter
    val isWorkoutHeaderSticky = userPreferences.isWorkoutHeaderSticky
    val useScrollWheelForInput = userPreferences.useScrollWheelForInput
    val showExercisesImages = userPreferences.showExercisesImages
    val dismissScrollWheelInputAutomatically = userPreferences.dismissScrollWheelInputAutomatically
    val unitSystem = userPreferences.unitSystem

    private val _isImporting = MutableStateFlow(false)
    val isImporting = _isImporting.asStateFlow()

    private val _dialogMessage = MutableStateFlow<String?>(null)
    val dialogMessage = _dialogMessage.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    fun showDialog(message: String) {
        _dialogMessage.value = message
    }

    fun dismissDialog() {
        _dialogMessage.value = null
    }

    fun backupExport(uri: Uri) {
        viewModelScope.launch {
            try {
                val outputStream = streamProvider.getOutputStream(uri)
                if (outputStream == null) {
                    _events.emit(SettingsEvent.ExportFailed)
                    return@launch
                }
                importExportRepository.exportTo(outputStream)
                _events.emit(SettingsEvent.ExportSuccess)
            } catch (_: IOException) {
                _events.emit(SettingsEvent.ExportFailed)
            } catch (_: SecurityException) {
                _events.emit(SettingsEvent.ExportFailed)
            }
        }
    }

    fun backupImport(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                uriAccess.takePersistableReadPermission(uri)
                val inputStream = streamProvider.getInputStream(uri)
                if (inputStream == null) {
                    _events.emit(SettingsEvent.ImportFailed)
                    return@launch
                }

                importExportRepository.importFrom(inputStream)
                _events.emit(SettingsEvent.ImportSuccess)
            } catch (_: IOException) {
                _events.emit(SettingsEvent.ImportFailed)
            } catch (_: SecurityException) {
                _events.emit(SettingsEvent.ImportFailed)
            } catch (_: SerializationException) {
                _events.emit(SettingsEvent.ImportFailed)
            } catch (_: IllegalArgumentException) {
                _events.emit(SettingsEvent.ImportFailed)
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun saveThemeMode(mode: ThemeMode) {
        viewModelScope.launch { userPreferences.saveThemeMode(mode) }
    }

    fun saveLanguage(language: Language) {
        viewModelScope.launch { userPreferences.saveLanguage(language) }
    }

    fun saveMaterialMode(isEnabled: Boolean) {
        viewModelScope.launch { userPreferences.saveMaterialMode(isEnabled) }
    }

    fun saveWorkoutScreenOn(isOn: Boolean) {
        viewModelScope.launch { userPreferences.saveWorkoutScreenOn(isOn) }
    }

    fun saveRestTimerSoundOn(isOn: Boolean) {
        viewModelScope.launch { userPreferences.saveRestTimerSoundOn(isOn) }
    }

    fun saveIsWorkoutHeaderSticky(isSticky: Boolean) {
        viewModelScope.launch { userPreferences.saveIsWorkoutHeaderSticky(isSticky) }
    }

    fun saveUseScrollWheelForInput(useScroll: Boolean) {
        viewModelScope.launch { userPreferences.saveUseScrollWheelForInput(useScroll) }
    }

    fun saveDismissScrollWheelInputAutomatically(dismissAutomatically: Boolean) {
        viewModelScope.launch {
            userPreferences.saveDismissScrollWheelInputAutomatically(dismissAutomatically)
        }
    }

    fun saveShowExercisesImages(display: Boolean) {
        viewModelScope.launch {
            userPreferences.saveShowExercisesImages(display)
        }
    }

    fun saveUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch { userPreferences.saveUnitSystem(unitSystem) }
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
        themeMode,
        unitSystem
    ) { p, l, t, u ->
        p?.let {
            when (p.first()) {
                is Language -> l
                is ThemeMode -> t
                is UnitSystem -> u
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
            is Language -> saveLanguage(newPreference)
            is ThemeMode -> saveThemeMode(newPreference)
            is UnitSystem -> saveUnitSystem(newPreference)
        }
    }
}