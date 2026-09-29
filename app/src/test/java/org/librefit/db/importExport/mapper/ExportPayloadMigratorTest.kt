package org.librefit.db.importExport.mapper

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.librefit.db.ExportSchema
import org.librefit.db.importExport.dto.ExportData
import org.librefit.db.importExport.dto.ExportExercise
import org.librefit.db.importExport.dto.ExportMeasurement
import org.librefit.db.importExport.dto.ExportPayload
import org.librefit.db.importExport.dto.ExportSet
import org.librefit.db.importExport.dto.ExportWorkout
import org.librefit.enums.SetMode
import org.librefit.enums.WorkoutState
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExportPayloadMigratorTest {
    @Test
    fun `migrates legacy exercise positions through each supported version`() {
        val payload = ExportPayload(
            schemaVersion = 2,
            data = ExportData(
                workouts = listOf(
                    ExportWorkout(
                        id = 4,
                        routineId = 3,
                        notes = "",
                        title = "Workout",
                        state = WorkoutState.COMPLETED,
                        timeElapsed = 0,
                        created = LocalDateTime.parse("2025-01-01T10:00:00"),
                        completed = LocalDateTime.parse("2025-01-01T11:00:00"),
                        exercises = listOf(
                            exercise(id = 20, position = 7),
                            exercise(id = 10, position = 7),
                        ),
                    ),
                ),
                measurements = emptyList(),
            ),
        )

        val migrated = ExportPayloadMigrator.migrate(payload)

        assertEquals(ExportSchema.VERSION, migrated.schemaVersion)
        assertEquals(
            listOf(10L to 0, 20L to 1),
            migrated.data.workouts.single().exercises.map { it.id to it.position },
        )
    }

    @Test
    fun `rejects unsupported export schema versions`() {
        val payload = ExportPayload(
            schemaVersion = ExportSchema.VERSION + 1,
            data = ExportData(workouts = emptyList(), measurements = emptyList()),
        )

        assertFailsWith<UnsupportedExportSchemaVersionException> {
            ExportPayloadMigrator.migrate(payload)
        }
    }

    @Test
    fun `migrates schema three files that predate workout positions`() {
        val legacyJson = """
            {
              "schemaVersion": 3,
              "data": {
                "workouts": [{
                  "id": 4,
                  "routineId": 3,
                  "notes": "",
                  "title": "Workout",
                  "state": "COMPLETED",
                  "timeElapsed": 0,
                  "created": "2025-01-01T10:00:00",
                  "completed": "2025-01-01T11:00:00",
                  "exercises": []
                }],
                "measurements": []
              }
            }
        """.trimIndent()

        val migrated = ExportPayloadMigrator.migrate(
            Json.decodeFromString<ExportPayload>(legacyJson)
        )

        assertEquals(ExportSchema.VERSION, migrated.schemaVersion)
        assertEquals(0, migrated.data.workouts.single().position)
    }

    @Test
    fun `measurement DTO preserves the numeric weight contract`() {
        val measurement = ExportMeasurement(
            id = 8,
            bodyWeight = 72.5,
            bodyFatPercentage = 20,
            muscleMassPercentage = 40,
            date = LocalDateTime.parse("2025-01-01T10:00:00"),
            notes = "test",
        )

        val encoded = Json.encodeToString(measurement)
        val decoded = Json.decodeFromString<ExportMeasurement>(encoded)

        assertTrue("\"bodyWeight\":72.5" in encoded)
        assertEquals(measurement, decoded)
        assertEquals(72.5, decoded.toEntity().bodyWeight.inKilograms)
    }

    private fun exercise(id: Long, position: Int) = ExportExercise(
        id = id,
        idExerciseDC = "exercise",
        notes = "",
        setMode = SetMode.LOAD,
        restTime = 0,
        position = position,
        workoutId = 4,
        sets = listOf(
            ExportSet(
                id = id + 100,
                load = 30.0,
                reps = 10,
                elapsedTime = 0,
                completed = true,
                exerciseId = id,
            ),
        ),
    )
}
