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

package org.librefit.db.repository

import android.content.Context
import android.os.Build
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.db.dao.DatasetDao
import org.librefit.db.entity.ExerciseDC
import org.librefit.di.qualifiers.ApplicationScope
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.mappers.toUi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class to provide `res/raw/exercises.json` as a [List] of [ExerciseDC].
 *
 * This class serves as a mediator between [DatasetDao] and the
 * application, providing a clean API for data access.
 *
 *
 * @param datasetDao The [DatasetDao] instance used to access the dataset from the database.
 * @param applicationScope A long-lived coroutine on the application scope in order to update the dataset.
 * @property dataset It provides the latest dataset saved in Room
 */
@Singleton
class DatasetRepository @Inject constructor(
    private val datasetDao: DatasetDao,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    private val userPreferencesRepository: UserPreferencesRepository,
    @param:ApplicationContext private val context: Context
) {
    val dataset: StateFlow<List<UiExerciseDC>> = datasetDao.getDataset()
        .map { dataset -> dataset.map { it.toUi() } }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun updateDatasetOnAppUpdate() {
        applicationScope.launch(Dispatchers.IO) {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersion =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else pInfo.versionCode.toLong()
            val pastVersion = userPreferencesRepository.pastVersionCode.value

            // Update dataset only on app update
            if (pastVersion != currentVersion) {
                try {
                    val jsonFile =
                        context.resources.openRawResource(R.raw.exercises).bufferedReader().use {
                            it.readText()
                        }

                    val moshi = Moshi.Builder().build()
                    val listType =
                        Types.newParameterizedType(List::class.java, ExerciseDC::class.java)
                    val adapter = moshi.adapter<List<ExerciseDC>>(listType)

                    // ExerciseDC adapter is auto generated. All entries of all
                    // enums must be annotated with @Json with its corresponding value in json file
                    val exercises = adapter.fromJson(jsonFile)
                        ?: throw JsonDataException("Failed to parse `exercises.json` file. Resource ID: ${R.raw.exercises}")

                    // Set the dataset into the database using the DAO
                    datasetDao.setDataset(exercises)

                    // Save version
                    userPreferencesRepository.savePreference(
                        key = UserPreferencesRepository.pastVersionCodeKey,
                        value = currentVersion
                    )

                } catch (e: Exception) {
                    error(e.message.toString())
                }
            }
        }
    }
}