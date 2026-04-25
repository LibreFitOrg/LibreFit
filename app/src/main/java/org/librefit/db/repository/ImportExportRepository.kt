package org.librefit.db.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.librefit.db.AppDatabase
import java.io.File
import javax.inject.Inject
import kotlin.system.exitProcess

class ImportExportRepository @Inject constructor(
    private val db: AppDatabase,
    @ApplicationContext private val context: Context
) {
    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val sqliteDb = db.openHelper.writableDatabase

        val tempFile = File(context.cacheDir, "backup.db")

        val path = tempFile.absolutePath.replace("'", "''")
        sqliteDb.execSQL("VACUUM INTO '$path'")

        context.contentResolver.openOutputStream(uri)?.use { out ->
            tempFile.inputStream().use { input ->
                input.copyTo(out)
            }
        }

        tempFile.delete()
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