package org.librefit.db.importExport.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExportPayload(
    val schemaVersion: Int,
    val data: ExportData
)