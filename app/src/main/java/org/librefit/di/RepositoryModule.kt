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
 */

package org.librefit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import org.librefit.db.dao.DatasetDao
import org.librefit.db.dao.MeasurementDao
import org.librefit.db.dao.WorkoutDao
import org.librefit.db.repository.DatasetRepository
import org.librefit.db.repository.MeasurementRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.di.qualifiers.ApplicationScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideDatasetRepository(
        datasetDao: DatasetDao,
        @ApplicationScope applicationScope: CoroutineScope
    ): DatasetRepository {
        return DatasetRepository(datasetDao, applicationScope)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(workoutDao: WorkoutDao): WorkoutRepository {
        return WorkoutRepository(workoutDao)
    }

    @Provides
    @Singleton
    fun provideMeasurementRepository(measurementDao: MeasurementDao): MeasurementRepository {
        return MeasurementRepository(measurementDao)
    }
}