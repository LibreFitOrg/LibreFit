/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.librefit.db.AppDatabase
import org.librefit.db.dao.DatasetDao
import org.librefit.db.dao.MeasurementDao
import org.librefit.db.dao.WorkoutDao

/**
 * Provides the Room database and its DAOs as application-wide singletons.
 */
val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.NAME,
        )
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .build()
    }

    // DAOs
    single<WorkoutDao> { get<AppDatabase>().getWorkoutDao() }
    single<MeasurementDao> { get<AppDatabase>().getMeasurementDao() }
    single<DatasetDao> { get<AppDatabase>().getDatasetDao() }
}
