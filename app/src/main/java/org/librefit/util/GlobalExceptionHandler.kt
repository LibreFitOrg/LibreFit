/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.util

import android.content.Context
import android.content.Intent
import android.os.Process
import org.librefit.activities.ErrorActivity
import org.librefit.db.repository.UserPreferencesRepository
import kotlin.system.exitProcess


class GlobalExceptionHandler(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
) : Thread.UncaughtExceptionHandler {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    fun initialize() {
        // Get the current existing handler
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        // Set this class as the new handler
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        // Get user preferences to match current theme
        val materialMode = userPreferencesRepository.materialMode.value
        val themeMode = userPreferencesRepository.themeMode.value

        // Launch the ErrorActivity, passing the stack trace and the restart action
        val errorIntent = Intent(context, ErrorActivity::class.java).apply {
            putExtra(ErrorActivity.EXTRA_STACK_TRACE, getStackTrace(exception))
            putExtra(ErrorActivity.EXTRA_THEME_MODE, themeMode)
            putExtra(ErrorActivity.EXTRA_MATERIAL_MODE, materialMode)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(errorIntent)

        defaultHandler?.uncaughtException(thread, exception)

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