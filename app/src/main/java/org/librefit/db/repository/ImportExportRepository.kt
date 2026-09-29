package org.librefit.db.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.librefit.db.ExportSchema
import org.librefit.db.dao.MeasurementDao
import org.librefit.db.dao.WorkoutDao
import org.librefit.db.importExport.dto.ExportData
import org.librefit.db.importExport.dto.ExportPayload
import org.librefit.db.importExport.mapper.ExportPayloadMigrator
import org.librefit.db.importExport.mapper.importIdentity
import org.librefit.db.importExport.mapper.toExport
import org.librefit.db.importExport.mapper.toRelation
import org.librefit.db.importExport.mapper.toEntity
import java.nio.charset.StandardCharsets
import java.io.InputStream
import java.io.OutputStream

class ImportExportRepository(
    private val workoutDao: WorkoutDao,
    private val measurementDao: MeasurementDao,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun exportTo(outputStream: OutputStream) = outputStream.use { output ->
        withContext(ioDispatcher) {
            val payload = ExportPayload(
                schemaVersion = ExportSchema.VERSION,
                data = ExportData(
                    workouts = workoutDao.getAllWorkoutsWithExercisesAndSetsOnce().map {
                        it.toExport()
                    },
                    measurements = measurementDao.getAllMeasurementsOnce().map {
                        it.toExport()
                    },
                ),
            )

            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
            output.write(json.encodeToString(payload).toByteArray(StandardCharsets.UTF_8))
        }
    }

    suspend fun importFrom(inputStream: InputStream) = withContext(ioDispatcher) {
        val json = Json { ignoreUnknownKeys = true }
        val rawPayload = inputStream.use { input ->
            val text = input.bufferedReader(StandardCharsets.UTF_8).readText()
            json.decodeFromString<ExportPayload>(text)
        }
        val payload = ExportPayloadMigrator.migrate(rawPayload)

        val existingWorkouts = workoutDao.getAllWorkoutsWithExercisesAndSetsOnce()
            .asSequence()
            .map { it.toExport().importIdentity() }
            .toMutableSet()

        payload.data.workouts.forEach { workout ->
            if (existingWorkouts.add(workout.importIdentity())) {
                workoutDao.addWorkoutWithExercisesAndSets(workout.toRelation())
            }
        }

        val existingMeasurements = measurementDao.getAllMeasurementsOnce()
            .asSequence()
            .map { it.toExport().importIdentity() }
            .toMutableSet()
        payload.data.measurements.forEach { measurement ->
            if (existingMeasurements.add(measurement.importIdentity())) {
                measurementDao.upsertMeasurement(measurement.toEntity())
            }
        }
    }
}