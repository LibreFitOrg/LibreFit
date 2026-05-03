package org.librefit.db.converters

import kotlinx.serialization.Serializable
import org.librefit.db.entity.Exercise
import org.librefit.db.entity.ExerciseDC
import org.librefit.db.entity.Measurement
import org.librefit.db.entity.Workout
import org.librefit.db.entity.Set

@Serializable
data class ExportPayload(
    val version: Int,
    val data: ExportData
)

@Serializable
data class ExportData(
    val workouts: List<Workout>,
    val exercises: List<Exercise>,
    val sets: List<Set>,
    val measurements: List<Measurement>,
    val exerciseDCs: List<ExerciseDC>
)