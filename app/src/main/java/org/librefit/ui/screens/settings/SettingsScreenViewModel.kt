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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.healthConnect.HealthConnectSyncOption
import org.librefit.enums.userPreferences.DialogPreference
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.enums.userPreferences.UnitSystem
import org.librefit.health.HealthConnectRepository
import org.librefit.health.HealthConnectSyncManager
import org.librefit.ui.models.HealthConnectState
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val userPreferences: UserPreferencesRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val healthConnectSyncManager: HealthConnectSyncManager
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
    val allHealthConnectPermissions = healthConnectRepository.allPermissions
    private val _healthConnectState = MutableStateFlow(HealthConnectState())
    private val enabledHealthConnectOptions = combine(
        HealthConnectSyncOption.entries.map { option ->
            userPreferences.healthConnectSyncEnabled(option.preferenceId)
        }
    ) { enabled ->
        HealthConnectSyncOption.entries
            .filterIndexedTo(mutableSetOf()) { index, _ -> enabled[index] }
    }
    val healthConnectState: StateFlow<HealthConnectState> = combine(
        _healthConnectState,
        userPreferences.healthConnectEnabled,
        enabledHealthConnectOptions
    ) { state, isEnabled, enabledOptions ->
        state.copy(isEnabled = isEnabled, enabledOptions = enabledOptions)
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HealthConnectState()
        )

    init {
        refreshHealthConnectState()
    }

    val unitSystem = userPreferences.unitSystem

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

    fun refreshHealthConnectState() {
        viewModelScope.launch {
            val isAvailable = healthConnectRepository.isAvailable()
            val grantedPermissions = if (isAvailable) {
                healthConnectRepository.grantedPermissions()
            } else {
                emptySet()
            }

            _healthConnectState.update {
                it.copy(
                    isAvailable = isAvailable,
                    grantedPermissions = grantedPermissions
                )
            }

            configuredHealthConnectOptions().forEach { option ->
                if (!grantedPermissions.containsAll(permissionsFor(option))) {
                    userPreferences.saveHealthConnectSyncEnabled(option.preferenceId, false)
                }
            }
        }
    }

    fun updateHealthConnectPermissions(
        option: HealthConnectSyncOption?,
        grantedPermissions: Set<String>
    ) {
        viewModelScope.launch {
            healthConnectSyncManager.invalidatePendingSyncs()
            val updatedGrantedPermissions =
                _healthConnectState.value.grantedPermissions + grantedPermissions
            _healthConnectState.update {
                it.copy(grantedPermissions = updatedGrantedPermissions)
            }
            if (option == null) {
                val hasAnyPermission =
                    updatedGrantedPermissions.any { it in allHealthConnectPermissions }
                userPreferences.saveHealthConnectEnabled(hasAnyPermission)
            } else if (updatedGrantedPermissions.containsAll(permissionsFor(option))) {
                userPreferences.saveHealthConnectEnabled(true)
                userPreferences.saveHealthConnectSyncEnabled(option.preferenceId, true)
                syncEnabledData(configuredHealthConnectOptions() + option)
            }
        }
    }

    fun permissionsFor(option: HealthConnectSyncOption): Set<String> =
        healthConnectRepository.permissionsFor(option)

    fun updateHealthConnectEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            healthConnectSyncManager.invalidatePendingSyncs()
            userPreferences.saveHealthConnectEnabled(isEnabled)

            if (isEnabled) {
                val enabledOptions = configuredHealthConnectOptions()
                if (enabledOptions.isNotEmpty()) {
                    syncEnabledData(enabledOptions)
                }
            }
        }
    }

    fun updateHealthConnectSyncOption(option: HealthConnectSyncOption, isEnabled: Boolean) {
        viewModelScope.launch {
            healthConnectSyncManager.invalidatePendingSyncs()
            userPreferences.saveHealthConnectSyncEnabled(option.preferenceId, isEnabled)
            if (isEnabled) {
                userPreferences.saveHealthConnectEnabled(true)
                syncEnabledData(configuredHealthConnectOptions() + option)
            }
        }
    }

    private suspend fun configuredHealthConnectOptions(): Set<HealthConnectSyncOption> = buildSet {
        for (option in HealthConnectSyncOption.entries) {
            if (userPreferences.isHealthConnectSyncEnabled(option.preferenceId)) add(option)
        }
    }

    private suspend fun syncEnabledData(options: Set<HealthConnectSyncOption>) {
        if (options.isEmpty()) return
        runCatching { healthConnectSyncManager.syncEnabledData(options) }
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
