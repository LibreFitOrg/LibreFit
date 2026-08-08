package org.librefit.di.stringProvider

interface StringProvider {
    fun importDataFailed(): String
    val unsupportedSchemaVersion: String
    val importSuccessToast: String
    val importFailedToast: String
    val exportSuccessToast: String
    val exportFailedToast: String
}