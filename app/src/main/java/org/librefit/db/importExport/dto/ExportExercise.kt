package org.librefit.db.importExport.dto

import kotlinx.serialization.Serializable
import org.librefit.enums.SetMode

@Serializable
data class ExportExercise(
    val id: Long,
    val idExerciseDC: String,
    val notes: String,
    val setMode: SetMode,
    val restTime: Int,
    val position: Int,
    val workoutId: Long,
    val sets: List<ExportSet>
)