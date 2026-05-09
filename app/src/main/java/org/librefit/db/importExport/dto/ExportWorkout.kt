package org.librefit.db.importExport.dto

import kotlinx.serialization.Serializable
import org.librefit.db.entity.LocalDateTimeSerializer
import org.librefit.enums.WorkoutState
import java.time.LocalDateTime

@Serializable
data class ExportWorkout(
    val id: Long,
    val routineId: Long,
    val notes: String,
    val title: String,
    val state: WorkoutState,
    val timeElapsed: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val created: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val completed: LocalDateTime,
    val exercises: List<ExportExercise>
)