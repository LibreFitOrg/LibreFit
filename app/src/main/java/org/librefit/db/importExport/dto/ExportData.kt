package org.librefit.db.importExport.dto

import kotlinx.serialization.Serializable
import org.librefit.db.entity.Measurement

@Serializable
data class ExportData(
    val workouts: List<ExportWorkout>,
    val measurements: List<Measurement>,
)