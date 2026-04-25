package org.librefit.ui.models

sealed interface BackupEvent {
    object BackupSuccess : BackupEvent
    object RestoreSuccess : BackupEvent
    data class Error(val message: String) : BackupEvent
}