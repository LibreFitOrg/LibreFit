/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.librefit.db.repository.DatasetRepository
import org.librefit.db.repository.RoutineTemplateRepository
import org.librefit.di.qualifiers.ApplicationScope
import org.librefit.util.GlobalExceptionHandler
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {
    @Inject
    lateinit var globalExceptionHandler: GlobalExceptionHandler

    @Inject
    lateinit var datasetRepository: DatasetRepository

    @Inject
    lateinit var routineTemplateRepository: RoutineTemplateRepository

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // Setup global exception handler
        globalExceptionHandler.initialize()

        // Update dataset and routine templates on each app update. They are sequenced because
        // routine templates reference exercises from the dataset by id.
        applicationScope.launch(Dispatchers.IO) {
            datasetRepository.updateDatasetOnAppUpdateSynchronously()
            routineTemplateRepository.updateRoutinesOnAppUpdateSynchronously()
        }
    }
}
