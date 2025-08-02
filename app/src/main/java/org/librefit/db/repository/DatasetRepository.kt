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

package org.librefit.db.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.librefit.db.dao.DatasetDao
import org.librefit.db.entity.ExerciseDC
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.mappers.toUi

/**
 * Repository class to provide `res/raw/exercises.json` as a [List] of [ExerciseDC].
 *
 * This class serves as a mediator between [DatasetDao] and the
 * application, providing a clean API for data access.
 *
 * This class is provided by [org.librefit.di.RepositoryModule].
 *
 * @param datasetDao The [DatasetDao] instance used to access the dataset from the database.
 * @param applicationScope A long-lived coroutine on the application scope in order to update the dataset.
 * @property dataset It provides the latest dataset saved in Room
 */
class DatasetRepository(
    datasetDao: DatasetDao,
    applicationScope: CoroutineScope
) {
    // The public, read-only StateFlow for consumers to collect.
    val dataset: StateFlow<List<UiExerciseDC>> = datasetDao.getDataset()
        .map { dbList ->
            dbList.map { it.toUi() }
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}