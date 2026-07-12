/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.repository.MeasurementRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.healthConnect.HealthConnectClickAction
import org.librefit.enums.healthConnect.HealthConnectStatus
import org.librefit.enums.userPreferences.DialogPreference
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.health.HealthConnectRepository
import org.librefit.ui.models.HealthConnectState
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val userPreferences: UserPreferencesRepository,
    private val measurementRepository: MeasurementRepository,
    private val healthConnectRepository: HealthConnectRepository
) : ViewModel() {
    val themeMode = userPreferences.themeMode
    val materialMode = userPreferences.materialMode
    val keepScreenOn = userPreferences.workoutScreenOn
    val language = userPreferences.language
    val restTimerSoundOn = userPreferences.restTimerSoundOn
    val isSupporter = userPreferences.isSupporter
    val isWorkoutHeaderSticky = userPreferences.isWorkoutHeaderSticky
    val useScrollWheelForInput = userPreferences.useScrollWheelForInput
    val dismissScrollWheelInputAutomatically = userPreferences.dismissScrollWheelInputAutomatically
    val healthConnectPermissions = healthConnectRepository.writePermissions

    private val _healthConnectState = MutableStateFlow(HealthConnectState())
    val healthConnectState: StateFlow<HealthConnectState> = combine(
        _healthConnectState,
        userPreferences.healthConnectEnabled
    ) { state, isEnabled ->
        state.copy(isEnabled = isEnabled)
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = HealthConnectState()
        )

    init {
        refreshHealthConnectState()
    }

    fun saveThemeMode(mode: ThemeMode) {
        viewModelScope.launch { userPreferences.saveThemeMode(mode) }
    }

    fun saveLanguage(language: Language) {
        viewModelScope.launch { userPreferences.saveLanguage(language.code) }
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

    fun refreshHealthConnectState() {
        viewModelScope.launch {
            val isAvailable = healthConnectRepository.isAvailable()
            val hasPermissions = isAvailable && healthConnectRepository.hasWritePermissions()

            _healthConnectState.update {
                it.copy(
                    status = when {
                        !isAvailable -> HealthConnectStatus.UNAVAILABLE
                        hasPermissions -> HealthConnectStatus.READY
                        else -> HealthConnectStatus.NEEDS_PERMISSIONS
                    }
                )
            }
        }
    }

    fun onHealthConnectClick(): HealthConnectClickAction {
        val state = healthConnectState.value

        return when {
            !state.isAvailable || state.isExporting -> HealthConnectClickAction.NONE
            state.isChecked -> {
                updateHealthConnectEnabled(false)
                HealthConnectClickAction.NONE
            }
            !state.hasPermissions -> HealthConnectClickAction.REQUEST_PERMISSIONS
            else -> {
                updateHealthConnectEnabled(true)
                HealthConnectClickAction.NONE
            }
        }
    }

    fun updateHealthConnectPermissions(grantedPermissions: Set<String>) {
        viewModelScope.launch {
            val hasPermissions = grantedPermissions.containsAll(healthConnectPermissions)
            if (hasPermissions) {
                userPreferences.saveHealthConnectEnabled(true)
                _healthConnectState.update {
                    it.copy(
                        status = HealthConnectStatus.READY,
                        exportedRecords = null
                    )
                }
                exportMeasurementsToHealthConnect()
            } else {
                userPreferences.saveHealthConnectEnabled(false)
                _healthConnectState.update {
                    it.copy(
                        status = HealthConnectStatus.NEEDS_PERMISSIONS,
                        exportedRecords = null
                    )
                }
            }
        }
    }

    fun updateHealthConnectEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            userPreferences.saveHealthConnectEnabled(isEnabled)

            if (isEnabled) {
                _healthConnectState.update { it.copy(exportedRecords = null) }
                exportMeasurementsToHealthConnect()
            } else {
                _healthConnectState.update {
                    it.copy(
                        status = if (it.hasPermissions) {
                            HealthConnectStatus.READY
                        } else {
                            it.status
                        },
                        exportedRecords = null
                    )
                }
            }
        }
    }

    private suspend fun exportMeasurementsToHealthConnect() {
        _healthConnectState.update {
            it.copy(
                status = HealthConnectStatus.EXPORTING,
                exportedRecords = null
            )
        }

        runCatching {
            healthConnectRepository.exportMeasurements(measurementRepository.measurements.first())
        }.onSuccess { exportedRecords ->
            _healthConnectState.update {
                it.copy(
                    status = HealthConnectStatus.READY,
                    exportedRecords = exportedRecords
                )
            }
        }.onFailure {
            userPreferences.saveHealthConnectEnabled(false)
            _healthConnectState.update {
                it.copy(
                    status = if (healthConnectRepository.isAvailable()) {
                        HealthConnectStatus.NEEDS_PERMISSIONS
                    } else {
                        HealthConnectStatus.UNAVAILABLE
                    },
                    exportedRecords = null
                )
            }
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
            is Language -> saveLanguage(newPreference)
            is ThemeMode -> saveThemeMode(newPreference)
        }
    }
}
