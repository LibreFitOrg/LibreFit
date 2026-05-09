package org.librefit.db.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.librefit.db.AppDatabase
import javax.inject.Inject
import org.librefit.db.importExport.dto.ExportData
import org.librefit.db.importExport.dto.ExportPayload
import org.librefit.db.importExport.mapper.toExport
import org.librefit.db.importExport.mapper.toRelation
import org.librefit.di.qualifiers.IoDispatcher

class ImportExportRepository @Inject constructor(
    private val db: AppDatabase,
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun exportTo(uri: Uri) = withContext(ioDispatcher) {
        val workoutDao = db.getWorkoutDao()
        val measurementDao = db.getMeasurementDao()

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

        context.contentResolver.openOutputStream(uri)?.use { output ->
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
            output.write(json.encodeToString(payload).toByteArray())
        } ?: return@withContext
    }

    suspend fun importFrom(uri: Uri): ImportResult = withContext(ioDispatcher) {
        val json = Json { ignoreUnknownKeys = true }

        val payload = context.contentResolver.openInputStream(uri)?.use { input ->
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val text = input.bufferedReader().readText()
            json.decodeFromString<ExportPayload>(text)
        } ?: return@withContext ImportResult.Error("Error reading from file")

        db.withTransaction {
            val workoutDao = db.getWorkoutDao()
            val measurementDao = db.getMeasurementDao()

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

        return@withContext ImportResult.Success
    }
}

sealed class ImportResult {
    data object Success : ImportResult()
    data class Error(val message: String) : ImportResult()
}