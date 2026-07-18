/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.editExercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.repository.DatasetRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.ExerciseProperty
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle
import org.librefit.nav.Route
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.mappers.toEntity
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@HiltViewModel
class EditExerciseScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val datasetRepository: DatasetRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val showExercisesImages = userPreferencesRepository.showExercisesImages

    val exerciseDCid = savedStateHandle.toRoute<Route.EditExerciseScreen>().exerciseDCid

    @OptIn(ExperimentalUuidApi::class)
    private val isCustomExercise = if (exerciseDCid.isBlank()) true else runCatching {
        Uuid.parseHexDash(exerciseDCid)
    }.fold(
        onFailure = { false },
        onSuccess = { true }
    )

    private val _exerciseDC = MutableStateFlow(UiExerciseDC())
    val exerciseDC = _exerciseDC.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            datasetRepository.getExerciseFromId(id = exerciseDCid)?.let {
                _exerciseDC.update { _ -> it }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveExercise() {
        viewModelScope.launch(Dispatchers.IO) {
            datasetRepository.upsertExercise(
                exerciseDC.value.toEntity().let {
                    it.copy(
                        id = exerciseDCid.ifBlank { Uuid.random().toString() },
                        isCustomExercise = isCustomExercise,
                        instructions = it.instructions  // Each new line becomes an element of the list
                            .firstOrNull()
                            ?.split("\n")
                            ?.filter { string -> string.isNotBlank() } ?: emptyList()
                    )
                }
            )
        }
    }

    fun updateValue(exerciseProperty: ExerciseProperty) {
        when (exerciseProperty) {
            is Force -> _exerciseDC.update { it.copy(force = exerciseProperty) }
            is Level -> _exerciseDC.update { it.copy(level = exerciseProperty) }
            is Category -> _exerciseDC.update { it.copy(category = exerciseProperty) }
            is Mechanic -> _exerciseDC.update { it.copy(mechanic = exerciseProperty) }
            is Equipment -> _exerciseDC.update { it.copy(equipment = exerciseProperty) }
            is Muscle -> {}
        }
    }

    fun updatePrimaryMuscles(muscle: Muscle) {
        _exerciseDC.update { e ->
            e.copy(
                primaryMuscles = if (muscle in e.primaryMuscles) {
                    e.primaryMuscles.filter { it != muscle }
                } else {
                    e.primaryMuscles + muscle
                }.toImmutableList()
            )
        }
    }

    fun updateSecondaryMuscles(muscle: Muscle) {
        _exerciseDC.update { e ->
            e.copy(
                secondaryMuscles = if (muscle in e.secondaryMuscles) {
                    e.secondaryMuscles.filter { it != muscle }
                } else {
                    e.secondaryMuscles + muscle
                }.toImmutableList()
            )
        }
    }

    fun updateInstructions(newInstructions: String) {
        _exerciseDC.update {
            it.copy(instructions = persistentListOf(newInstructions))
        }
    }

    fun updateName(newName: String) {
        _exerciseDC.update { it.copy(name = newName) }
    }
}