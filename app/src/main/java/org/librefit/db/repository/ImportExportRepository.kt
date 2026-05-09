package org.librefit.db.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.librefit.db.AppDatabase
import org.librefit.db.converters.ExportData
import org.librefit.db.converters.ExportPayload
import javax.inject.Inject
import org.librefit.db.entity.Exercise
import org.librefit.db.entity.LocalDateTimeSerializer
import org.librefit.di.qualifiers.IoDispatcher
import java.time.LocalDateTime
import kotlin.system.exitProcess

class ImportExportRepository @Inject constructor(
    private val db: AppDatabase,
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        // 1. note the current db migration version
        // 2. serialize the JSON
        val workouts = db.getWorkoutDao().getAllWorkouts()

        val workoutIds = workouts.map { it.id }
        val exercises = db.getWorkoutDao().getAllExercises(workoutIds)

        val exerciseIds = exercises.map { it.id }
        val sets = db.getWorkoutDao().getAllSets(exerciseIds)

        val measurements = db.getMeasurementDao().getAllMeasurementsForBackup()

        val exerciseDCs = db.getDatasetDao().getAllExerciseDCs()

        val payload = ExportPayload(
            version = 3,
            data = ExportData(
                workouts = workouts,
                exercises = exercises,
                sets = sets,
                measurements = measurements,
                exerciseDCs = exerciseDCs
            )
        )

        val outputStream = context.contentResolver.openOutputStream(uri)
            ?: error("Cannot open output stream for export URI")

        outputStream.use { out ->
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
                serializersModule = SerializersModule {
                    contextual(LocalDateTime::class, LocalDateTimeSerializer)
                }
            }
            out.write(json.encodeToString(payload).toByteArray())
            out.flush()
        }

        outputStream.close()
    }

    private fun normalizeExercises(exercises: List<Exercise>): List<Exercise> {
        return exercises
            .groupBy { it.workoutId }
            .flatMap { (_, group) ->
                group
                    .sortedBy { it.id }
                    .mapIndexed { index, exercise ->
                        exercise.copy(position = index)
                    }
            }
    }

    suspend fun importFrom(uri: Uri) = withContext(ioDispatcher) {
        val json = Json { ignoreUnknownKeys = true }

        val payload = context.contentResolver.openInputStream(uri)?.use { input ->
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val text = input.bufferedReader().readText()
            json.decodeFromString<ExportPayload>(text)
        } ?: return@withContext

        db.withTransaction {

            val workoutDao = db.getWorkoutDao()
            val measurementDao = db.getMeasurementDao()
            val datasetDao = db.getDatasetDao()

            // 1. UPSERT WORKOUTS
            workoutDao.upsertWorkouts(payload.data.workouts)

            // 2. UPSERT EXERCISES
            // normalizing because of the migration to V3 of the DB schema
            val normalizedExercises = if (payload.version < 3)
                normalizeExercises(payload.data.exercises)
            else
                payload.data.exercises
            workoutDao.upsertExercises(normalizedExercises)

            // 3. UPSERT SETS
            workoutDao.upsertSets(payload.data.sets)

            // 4. UPSERT MEASUREMENTS
            payload.data.measurements.forEach {
                measurementDao.upsertMeasurement(it)
            }

            // 5. UPSERT DATASETS
            payload.data.exerciseDCs.forEach {
                datasetDao.upsertExercise(it)
            }
        }

        // restart app process cleanly
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)

        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        context.startActivity(intent)
        exitProcess(0)
    }
}