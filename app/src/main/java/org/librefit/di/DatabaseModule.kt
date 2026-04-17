/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.librefit.db.AppDatabase
import org.librefit.db.dao.DatasetDao
import org.librefit.db.dao.MeasurementDao
import org.librefit.db.dao.WorkoutDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext appContext: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            AppDatabase.NAME
        )
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .build()
    }

    // DAOs
    @Provides
    @Singleton
    fun provideWorkoutDao(database: AppDatabase): WorkoutDao {
        return database.getWorkoutDao()
    }

    @Provides
    @Singleton
    fun provideMeasurementDao(database: AppDatabase): MeasurementDao {
        return database.getMeasurementDao()
    }

    @Provides
    @Singleton
    fun provideDatasetDao(database: AppDatabase): DatasetDao {
        return database.getDatasetDao()
    }
}
