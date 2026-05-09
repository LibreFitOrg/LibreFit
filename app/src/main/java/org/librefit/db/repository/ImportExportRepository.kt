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
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.di.qualifiers.IoDispatcher
import java.time.LocalDateTime
import kotlin.system.exitProcess

class ImportExportRepository @Inject constructor(
    private val db: AppDatabase,
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val workoutDao = db.getWorkoutDao()
        val measurementDao = db.getMeasurementDao()

        val workouts = workoutDao.getAllWorkouts()

        val measurements = measurementDao.getAllMeasurementsOnce()

        val workoutsWithExercisesAndSets = workouts.map { workout ->
            val exercisesWithSets = workoutDao.getExercisesFromWorkout(workout.id)

            WorkoutWithExercisesAndSets(
                workout = workout,
                exercisesWithSets = exercisesWithSets
            )
        }

        val payload = ExportPayload(
            schemaVersion = db.openHelper.readableDatabase.version,
            data = ExportData(
                workoutsWithExercisesAndSets = workoutsWithExercisesAndSets,
                measurements = measurements,
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

    fun upgradeToLatest(data: Any, version: Int): ImportFileV5 {
        return when (version) {
            2 -> v4ToV5(v3ToV4(v2ToV3(data)))
            3 -> v4ToV5(v3ToV4(data))
            4 -> v4ToV5(data)
            5 -> data
            else -> error("Unsupported version")
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

            // 1. UPSERT WORKOUTS WITH EXERCISES AND SETS
            payload.data.workoutsWithExercisesAndSets.forEach {
                workoutDao.addWorkoutWithExercisesAndSets(it)
            }

            // 2. UPSERT MEASUREMENTS
            payload.data.measurements.forEach {
                measurementDao.upsertMeasurement(it)
            }
        }

        // TODO: remove the restart logic and handle it with better UX
        // restart app process cleanly
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)

        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        context.startActivity(intent)
        exitProcess(0)
    }
}