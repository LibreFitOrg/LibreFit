/*
 * Copyright (c) 2025. LibreFit
 *
 * This file is part of LibreFit
 *
 * LibreFit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreFit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreFit.  If not, see <https://www.gnu.org/licenses/>.
 *
 * LibreFit is subject to additional terms covering author attribution and
 * trademark usage, as found in the accompanying ADDITIONAL_TERMS.md file.
 */

package org.librefit.util

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.librefit.activities.ErrorActivity
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.di.qualifiers.MainActivityClass
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.exitProcess


@Singleton
class GlobalExceptionHandler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:MainActivityClass private val mainActivityClass: Class<out Activity>,
    private val userPreferencesRepository: UserPreferencesRepository
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        Log.d("Handler", "Exception: $exception ")
        // Create a PendingIntent to restart the app
        val restartIntent = Intent(context, mainActivityClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0, // Request code
            restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get user preferences to match current theme
        val materialMode = userPreferencesRepository.materialMode.value
        val themeMode = userPreferencesRepository.themeMode.value

        // Launch the ErrorActivity, passing the stack trace and the restart action
        val errorIntent = Intent(context, ErrorActivity::class.java).apply {
            putExtra(ErrorActivity.EXTRA_STACK_TRACE, getStackTrace(exception))
            putExtra(ErrorActivity.EXTRA_RESTART_PENDING_INTENT, pendingIntent)
            putExtra(ErrorActivity.EXTRA_THEME_MODE, themeMode)
            putExtra(ErrorActivity.EXTRA_MATERIAL_MODE, materialMode)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(errorIntent)

        // Terminate the current process
        Process.killProcess(Process.myPid())
        exitProcess(10)
    }

    private fun getStackTrace(exception: Throwable): String {
        return java.io.StringWriter().also {
            exception.printStackTrace(java.io.PrintWriter(it))
        }.toString()
    }
}