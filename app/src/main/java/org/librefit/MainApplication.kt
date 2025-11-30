/*
 * Copyright (c) 2024-2025. LibreFit
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

package org.librefit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.librefit.db.repository.DatasetRepository
import org.librefit.util.GlobalExceptionHandler
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {
    @Inject
    lateinit var globalExceptionHandler: GlobalExceptionHandler

    @Inject
    lateinit var datasetRepository: DatasetRepository

    override fun onCreate() {
        super.onCreate()
        // Setup global exception handler
        Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler)

        // Update dataset on each app update
        datasetRepository.updateDatasetOnAppUpdate()
    }
}