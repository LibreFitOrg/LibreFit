package org.librefit.db.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.librefit.db.AppDatabase
import org.librefit.db.converters.ExportData
import org.librefit.db.converters.ExportPayload
import java.io.File
import javax.inject.Inject
import kotlin.system.exitProcess

class ImportExportRepository @Inject constructor(
    private val db: AppDatabase,
    @ApplicationContext private val context: Context
) {
    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        // 1. note the current db migration version
        // 2. serialize the JSON
        val workouts = db.getWorkoutDao().getAllWorkouts().first()
        val workoutIds = workouts.map { it.id }
        val exercises = db.getWorkoutDao().getAllExercises(workoutIds).first()
        val payload = ExportPayload(
            version = 3,
            data = ExportData(
                workouts = workouts,
                exercises = exercises,
                // TODO
                sets = db.getWorkoutDao().getAllSets(),
                measurements = db.getMeasurementDao().getAll(),
                exerciseDCs = db.getDatasetDao().getAll()
            )
        )

        context.contentResolver.openOutputStream(uri)?.use { out ->
            val json = /* your serializer, e.g. kotlinx.serialization */
                out.write(json.encodeToString(payload).toByteArray())
        }
    }

    suspend fun importFrom(uri: Uri): Nothing = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(AppDatabase.NAME)

        AppDatabase.closeInstance()

        val tempFile = File(context.cacheDir, "restore.db")

        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val wal = File(dbFile.path + "-wal")
        val shm = File(dbFile.path + "-shm")

        wal.delete()
        shm.delete()

        tempFile.copyTo(dbFile, overwrite = true)
        tempFile.delete()

        AppDatabase.getInstance(context) // Reinitialize with the new DB file
        // restart app process cleanly
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)

        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        context.startActivity(intent)
        exitProcess(0)
    }
}