package org.librefit.db.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.Database
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.librefit.R
import org.librefit.db.AppDatabase
import org.librefit.db.Schema
import org.librefit.db.dao.MeasurementDao
import org.librefit.db.dao.WorkoutDao
import javax.inject.Inject
import org.librefit.db.importExport.dto.ExportData
import org.librefit.db.importExport.dto.ExportExercise
import org.librefit.db.importExport.dto.ExportPayload
import org.librefit.db.importExport.mapper.toExport
import org.librefit.db.importExport.mapper.toRelation
import org.librefit.di.qualifiers.IoDispatcher
import org.librefit.di.stringProvider.StringProvider
import java.io.InputStream
import java.io.OutputStream

class ImportExportRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val measurementDao: MeasurementDao,
    private val stringProvider: StringProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun exportTo(outputStream: OutputStream) = withContext(ioDispatcher) {
        val workouts = workoutDao.getAllWorkoutsWithExercisesAndSetsOnce()
        val exportWorkouts = workouts.map {
            it.toExport()
        }

        val measurements = measurementDao.getAllMeasurementsOnce()

        val payload = ExportPayload(
            schemaVersion = 3,
            data = ExportData(
                workouts = exportWorkouts,
                measurements = measurements
            )
        )

        outputStream.use { output ->
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
            output.write(json.encodeToString(payload).toByteArray())
        }
    }

    suspend fun importFrom(inputStream: InputStream) = withContext(ioDispatcher) {
        val json = Json { ignoreUnknownKeys = true }

        val rawPayload = inputStream.use { input ->
            val text = input.bufferedReader().readText()
            json.decodeFromString<ExportPayload>(text)
        }

        val payload = migratePayload(rawPayload)

        // 1. CONVERT INTO ENTITIES AND UPSERT WORKOUTS
        val relations = payload.data.workouts.map {
            it.toRelation()
        }
        relations.forEach {
            workoutDao.addWorkoutWithExercisesAndSets(it)
        }

        // 2. UPSERT MEASUREMENTS
        payload.data.measurements.forEach {
            measurementDao.upsertMeasurement(it)
        }
    }

    private val currentSchemaVersion = Schema.VERSION

    private fun migratePayload(payload: ExportPayload): ExportPayload {
        var current = payload

        while (current.schemaVersion < currentSchemaVersion) {
            current = when (current.schemaVersion) {
                1 -> migrateV1ToV2(current)
                2 -> migrateV2ToV3(current)
                else -> error("${stringProvider.unsupportedSchemaVersion}: ${current.schemaVersion}")
            }
        }

        return current
    }

    private fun migrateV1ToV2(payload: ExportPayload): ExportPayload {
        return payload.copy(schemaVersion = 2)
    }

    private fun migrateV2ToV3(payload: ExportPayload): ExportPayload {
        fun isValidOrder(list: List<ExportExercise>): Boolean {
            val pos = list.map { it.position }
            if (pos.size != pos.distinct().size) return false
            val sortedPos = pos.sorted()
            return sortedPos == (0 until pos.size).toList()
        }

        return payload.copy(
            schemaVersion = Schema.VERSION,
            data = payload.data.copy(
                workouts = payload.data.workouts.map { workout ->
                    val exercises = workout.exercises

                    val reordered = if (isValidOrder(exercises)) {
                        exercises
                    } else {
                        exercises
                            .sortedBy { it.id }
                            .mapIndexed { index, ex -> ex.copy(position = index) }
                    }

                    workout.copy(exercises = reordered)
                }
            )
        )
    }
}