package org.librefit.db.importExport.mapper

import org.librefit.db.ExportSchema
import org.librefit.db.importExport.dto.ExportExercise
import org.librefit.db.importExport.dto.ExportPayload

class UnsupportedExportSchemaVersionException(
    val version: Int,
) : IllegalArgumentException("Unsupported export schema version: $version")

object ExportPayloadMigrator {
    fun migrate(payload: ExportPayload): ExportPayload {
        if (payload.schemaVersion < 1 || payload.schemaVersion > ExportSchema.VERSION) {
            throw UnsupportedExportSchemaVersionException(payload.schemaVersion)
        }

        var current = payload
        while (current.schemaVersion < ExportSchema.VERSION) {
            current = when (current.schemaVersion) {
                1 -> current.copy(schemaVersion = 2)
                2 -> migrateV2ToV3(current)
                3 -> current.copy(schemaVersion = 4)
                else -> throw UnsupportedExportSchemaVersionException(current.schemaVersion)
            }
        }
        return current
    }

    private fun migrateV2ToV3(payload: ExportPayload): ExportPayload {
        return payload.copy(
            schemaVersion = 3,
            data = payload.data.copy(
                workouts = payload.data.workouts.map { workout ->
                    workout.copy(
                        exercises = migrateExercisePositions(workout.exercises)
                    )
                },
            ),
        )
    }

    private fun migrateExercisePositions(exercises: List<ExportExercise>) =
        if (exercises.map { it.position }.let { positions ->
                positions.size == positions.distinct().size &&
                    positions.sorted() == (0 until positions.size).toList()
            }
        ) {
            exercises
        } else {
            exercises
                .sortedBy { it.id }
                .mapIndexed { index, exercise -> exercise.copy(position = index) }
        }
}
