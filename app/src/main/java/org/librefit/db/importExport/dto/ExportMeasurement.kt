package org.librefit.db.importExport.dto

import kotlinx.serialization.Serializable
import org.librefit.db.entity.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class ExportMeasurement(
    val id: Long,
    val bodyWeight: Double,
    val bodyFatPercentage: Int,
    val muscleMassPercentage: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime,
    val notes: String,
)
