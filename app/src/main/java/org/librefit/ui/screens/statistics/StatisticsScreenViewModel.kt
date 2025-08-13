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

package org.librefit.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.librefit.R
import org.librefit.db.repository.WorkoutRepository
import org.librefit.enums.chart.StatisticsChart
import org.librefit.helpers.DataHelper
import org.librefit.ui.components.charts.Point
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class StatisticsScreenViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
    dataHelper: DataHelper
) : ViewModel() {
    private val _muscleDistributionStatisticsChart = MutableStateFlow(StatisticsChart.LOAD)
    val muscleDistributionStatisticsChart = _muscleDistributionStatisticsChart.asStateFlow()

    fun updateMuscleDistributionStatisticsChart(statisticsChart: StatisticsChart) {
        _muscleDistributionStatisticsChart.update {
            statisticsChart
        }
    }


    val defaultCutoffs = listOf(
        LocalDateTime.now().minusDays(7),
        LocalDateTime.now().minusDays(30),
        LocalDateTime.now().minusDays(365)
    )
    private val _cutoffs = MutableStateFlow(defaultCutoffs)
    val cutoffs = _cutoffs.asStateFlow()


    val muscleDistributionPoints: StateFlow<List<Point>> = combine(
        workoutRepository.completedWorkoutsWithExercisesAndSets,
        muscleDistributionStatisticsChart,
        cutoffs
    ) { workouts, statisticsChart, cutoffs ->
        dataHelper.fetchMuscleDistributionWithTimeBuckets(
            muscleDistributionStatisticsChart = statisticsChart,
            workoutsWithExercises = workouts,
            muscleDistributionCutoffs = cutoffs,
            includeOlderWorkouts = cutoffs.size <= 3
        )
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val muscleDistributionLegendIds: StateFlow<List<Pair<Int, Long?>>> = combine(
        cutoffs,
        muscleDistributionPoints
    ) { cutoffs, points ->
        var ids = cutoffs.mapIndexedNotNull { index, cutoff ->
            val cutoffDate = cutoff.toLocalDate()
            val differenceDays = ChronoUnit.DAYS.between(
                cutoffDate, LocalDate.now()
            )

            when (differenceDays) {
                7L -> R.string.past_week to null
                30L -> R.string.past_month to null
                365L -> R.string.past_year to null
                else -> {
                    if (differenceDays <= 30) {
                        R.string.days to differenceDays
                    } else if (differenceDays <= 365) {
                        R.string.months to (differenceDays / 30)
                    } else {
                        R.string.year to (differenceDays / 365)
                    }
                }
            }
        }

        if (cutoffs.size <= 3) {
            ids = ids + (R.string.history to null)
        }

        ids.take(points.firstOrNull()?.yValues?.size ?: 0)
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    private val _exercisesDistributionStatisticsChart = MutableStateFlow(StatisticsChart.LOAD)
    val exercisesDistributionStatisticsChart = _exercisesDistributionStatisticsChart.asStateFlow()

    fun updateExercisesDistributionStatisticsChart(statisticsChart: StatisticsChart) {
        _exercisesDistributionStatisticsChart.update {
            statisticsChart
        }
    }

    val exercisesDistributionPoints: StateFlow<List<Point>> = combine(
        workoutRepository.completedWorkoutsWithExercisesAndSets,
        exercisesDistributionStatisticsChart,
        cutoffs
    ) { workouts, statisticsChart, cutoffs ->
        dataHelper.fetchExercisesDistributionWithTimeBuckets(
            exerciseDistributionStatisticsChart = statisticsChart,
            workoutsWithExercises = workouts,
            exerciseDistributionCutoffs = cutoffs,
            includeOlderWorkouts = cutoffs.size <= 3
        )
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val exercisesDistributionLegendIds: StateFlow<List<Pair<Int, Long?>>> = combine(
        cutoffs,
        exercisesDistributionPoints
    ) { cutoffs, points ->
        var ids = cutoffs.mapIndexedNotNull { index, cutoff ->
            val cutoffDate = cutoff.toLocalDate()
            val differenceDays = ChronoUnit.DAYS.between(
                cutoffDate, LocalDate.now()
            )

            when (differenceDays) {
                7L -> R.string.past_week to null
                30L -> R.string.past_month to null
                365L -> R.string.past_year to null
                else -> {
                    if (differenceDays <= 30) {
                        R.string.days to differenceDays
                    } else if (differenceDays <= 365) {
                        R.string.months to (differenceDays / 30)
                    } else {
                        R.string.year to (differenceDays / 365)
                    }
                }
            }
        }

        if (cutoffs.size <= 3) {
            ids = ids + (R.string.history to null)
        }

        ids.take(points.firstOrNull()?.yValues?.size ?: 0)
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}