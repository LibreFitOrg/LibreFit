/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit

import android.app.Application
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.librefit.db.repository.DatasetRepository
import org.librefit.di.libreFitModules
import org.librefit.util.GlobalExceptionHandler

class MainApplication : Application() {
    private val globalExceptionHandler: GlobalExceptionHandler by inject()
    private val datasetRepository: DatasetRepository by inject()

    override fun onCreate() {
        super.onCreate()

        // Initialize the Koin container first: every component (Activities, Services,
        // Compose screens) resolves its dependencies from it.
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(libreFitModules)
        }

        // Setup global exception handler
        globalExceptionHandler.initialize()

        // Update dataset on each app update
        datasetRepository.updateDatasetOnAppUpdate()
    }
}
