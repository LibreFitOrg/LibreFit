package org.librefit.db.importExport.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExportData(
    val workouts: List<ExportWorkout>,
    val measurements: List<ExportMeasurement>,
)