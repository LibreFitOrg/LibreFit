package org.librefit.ui.screens.settings

sealed class SettingsEvent {
    data object ExportSuccess : SettingsEvent()
    data object ExportFailed : SettingsEvent()
    data object ImportSuccess : SettingsEvent()
    data object ImportFailed : SettingsEvent()
}