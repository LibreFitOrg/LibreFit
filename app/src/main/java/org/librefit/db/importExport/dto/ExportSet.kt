package org.librefit.db.importExport.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExportSet(
    val id: Long,
    val load: Double,
    val reps: Int,
    val elapsedTime: Int,
    val completed: Boolean,
    val exerciseId: Long
)