package org.librefit.db.converters

import kotlinx.serialization.Serializable
import org.librefit.db.entity.Measurement
import org.librefit.db.relations.WorkoutWithExercisesAndSets

@Serializable
data class ExportPayload(
    val schemaVersion: Int,
    val data: ExportData
)

@Serializable
data class ExportData(
    val workoutsWithExercisesAndSets: List<WorkoutWithExercisesAndSets>,
    val measurements: List<Measurement>,
)