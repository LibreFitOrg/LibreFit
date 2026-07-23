/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.infoExercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librefit.db.repository.DatasetRepository
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.chart.BodyweightChart
import org.librefit.enums.chart.ExerciseChart
import org.librefit.enums.chart.LoadChart
import org.librefit.enums.chart.TimeChart
import org.librefit.enums.chart.WeightedBodyweightChart
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.helpers.DataHelper
import org.librefit.nav.Route
import org.librefit.ui.components.charts.Point
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiWorkoutWithExercisesAndSets
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.models.mappers.toUi
import javax.inject.Inject

@HiltViewModel
class InfoExerciseScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    workoutRepository: WorkoutRepository,
    dataHelper: DataHelper,
    private val datasetRepository: DatasetRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val showExercisesImages = userPreferencesRepository.showExercisesImages

    fun setTrueShowExercisesImages() {
        onShowExercisesImagesChange(true)
    }

    fun setFalseShowExercisesImages() {
        onShowExercisesImagesChange(false)
    }

    fun onShowExercisesImagesChange(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveShowExercisesImages(value)
        }
    }


    private val idExerciseDC = savedStateHandle.toRoute<Route.InfoExerciseScreen>().idExerciseDC

    // Keeps track of changes (e.g. the user edits the exercise)
    val uiExerciseDC = datasetRepository.getExerciseFlowFromId(idExerciseDC)
        .filterNotNull()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiExerciseDC()
        )

    fun deleteExercise() {
        viewModelScope.launch(Dispatchers.IO) {
            datasetRepository.deleteExercise(uiExerciseDC.value.toEntity())
        }
    }

    val workoutsWithExercises: StateFlow<List<UiWorkoutWithExercisesAndSets>> = workoutRepository
        .getCompletedWorkoutsWithExercisesWithIdExerciseDC(idExerciseDC)
        .distinctUntilChanged()
        .map { list -> list.map { it.toUi() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Arbitrary ExerciseChart
    private val _exerciseChart = MutableStateFlow<ExerciseChart>(LoadChart.HEAVIEST_WEIGHT)
    val exerciseChart = _exerciseChart.asStateFlow()

    init {
        viewModelScope.launch {
            val exerciseDC = uiExerciseDC.first()
            val defaultExerciseChart: ExerciseChart = when (exerciseDC.category) { // TODO: Assign & save this to room entries
                Category.STRETCHING, Category.CARDIO -> TimeChart.BEST_TIME
                else -> when (exerciseDC.equipment) {
                    Equipment.BODY_ONLY, Equipment.FOAM_ROLL, Equipment.EXERCISE_BALL,
                    Equipment.MEDICINE_BALL, Equipment.BANDS ->
                        if (exerciseDC.name.contains("Weighted", true))
                            WeightedBodyweightChart.HEAVIEST_WEIGHT else BodyweightChart.MOST_REPS

                    else -> LoadChart.HEAVIEST_WEIGHT
                }
            }

            _exerciseChart.update {
                defaultExerciseChart
            }
        }
    }

    fun updateExerciseChart(newValue: ExerciseChart) {
        _exerciseChart.update {
            newValue
        }
    }

    val points: StateFlow<List<Point>> = combine(
        exerciseChart,
        workoutRepository.getCompletedWorkoutsWithExercisesWithIdExerciseDC(idExerciseDC)
    ) { c, workouts ->
        dataHelper.fetchPointsForExercisesChart(c, workouts)
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}