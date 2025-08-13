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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.enums.chart.StatisticsChart
import org.librefit.enums.exercise.Muscle
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.charts.LibreFitCartesianChart
import org.librefit.ui.components.charts.Point
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import java.text.DecimalFormat
import kotlin.random.Random

@Composable
fun StatisticsScreen(
    navController: NavHostController
) {
    val viewModel: StatisticsScreenViewModel = hiltViewModel()

    val muscleDistributionPoints by viewModel.muscleDistributionPoints.collectAsState()

    val muscleDistributionLegendIds by viewModel.muscleDistributionLegendIds.collectAsState()

    val muscleDistributionStatisticsChart by viewModel.muscleDistributionStatisticsChart.collectAsState()

    val exercisesDistributionPoints by viewModel.exercisesDistributionPoints.collectAsState()

    val exercisesDistributionLegendIds by viewModel.exercisesDistributionLegendIds.collectAsState()

    val exercisesDistributionStatisticsChart by viewModel.exercisesDistributionStatisticsChart.collectAsState()

    StatisticsScreenContent(
        navController = navController,
        muscleDistributionPoints = muscleDistributionPoints,
        muscleDistributionLegendIds = muscleDistributionLegendIds,
        muscleDistributionStatisticsChart = muscleDistributionStatisticsChart,
        exercisesDistributionPoints = exercisesDistributionPoints,
        exercisesDistributionLegendIds = exercisesDistributionLegendIds,
        exercisesDistributionStatisticsChart = exercisesDistributionStatisticsChart,
        updateMuscleDistributionStatisticsChart = viewModel::updateMuscleDistributionStatisticsChart,
        updateExercisesDistributionStatisticsChart = viewModel::updateExercisesDistributionStatisticsChart,
    )
}

@Composable
private fun StatisticsScreenContent(
    navController: NavHostController,
    muscleDistributionPoints: List<Point>,
    muscleDistributionLegendIds: List<Pair<Int, Long?>>,
    muscleDistributionStatisticsChart: StatisticsChart,
    exercisesDistributionPoints: List<Point>,
    exercisesDistributionLegendIds: List<Pair<Int, Long?>>,
    exercisesDistributionStatisticsChart: StatisticsChart,
    updateMuscleDistributionStatisticsChart: (StatisticsChart) -> Unit,
    updateExercisesDistributionStatisticsChart: (StatisticsChart) -> Unit
) {
    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.statistics)),
        navigateBack = navController::popBackStack
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding = innerPadding) {
            item {
                HeadlineText(
                    stringResource(R.string.muscles_distribution),
                    InfoMode.MUSCLE_DISTRIBUTION
                )
            }
            item {
                LibreFitCartesianChart(
                    format = when (muscleDistributionStatisticsChart) {
                        StatisticsChart.LOAD -> DecimalFormat("#.## " + stringResource(R.string.kg))
                        StatisticsChart.REPS -> DecimalFormat()
                        StatisticsChart.VOLUME -> DecimalFormat("#.## " + stringResource(R.string.kg))
                        StatisticsChart.DURATION -> DecimalFormat("# " + stringResource(R.string.min))
                    },
                    points = muscleDistributionPoints,
                    legendList = muscleDistributionLegendIds.map { pair ->
                        stringResource(pair.first) + if (pair.second != null) {
                            ": " + pair.second
                        } else ""
                    },
                    chartMode = muscleDistributionStatisticsChart,
                    useColumns = true,
                    updateChartMode = { updateMuscleDistributionStatisticsChart(it as StatisticsChart) }
                )
            }

            item {
                HeadlineText(
                    stringResource(R.string.exercises_distribution),
                    InfoMode.EXERCISES_DISTRIBUTION
                )
            }
            item {
                LibreFitCartesianChart(
                    format = when (exercisesDistributionStatisticsChart) {
                        StatisticsChart.LOAD -> DecimalFormat("#.## " + stringResource(R.string.kg))
                        StatisticsChart.REPS -> DecimalFormat()
                        StatisticsChart.VOLUME -> DecimalFormat("#.## " + stringResource(R.string.kg))
                        StatisticsChart.DURATION -> DecimalFormat("# " + stringResource(R.string.min))
                    },
                    points = exercisesDistributionPoints,
                    legendList = exercisesDistributionLegendIds.map { pair ->
                        stringResource(pair.first) + if (pair.second != null) {
                            ": " + pair.second
                        } else ""
                    },
                    chartMode = exercisesDistributionStatisticsChart,
                    useColumns = true,
                    updateChartMode = { updateExercisesDistributionStatisticsChart(it as StatisticsChart) }
                )
            }
        }
    }
}

@Preview
@Composable
fun StatisticsScreenPreview() {
    var muscleDistributionStatisticsChart by remember { mutableStateOf(StatisticsChart.entries.random()) }
    var exerciseDistributionStatisticsChart by remember { mutableStateOf(StatisticsChart.entries.random()) }

    val musclesNames = listOf(
        stringResource(Formatter.exerciseEnumToStringId(Muscle.BICEPS)),
        stringResource(Formatter.exerciseEnumToStringId(Muscle.TRICEPS))
    )

    val cutoffsIds: List<Pair<Int, Long?>> = listOf(
        R.string.past_week to null, R.string.past_month to null, R.string.history to null
    )

    key(muscleDistributionStatisticsChart) {
        val muscleDistributionPoints = listOf(
            Point(
                yValues = (0..2).map { Random.nextFloat() },
                xValue = musclesNames.first()
            ),
            Point(
                yValues = (0..2).map { Random.nextFloat() },
                xValue = musclesNames[1]
            )
        )

        val exerciseDistributionPoints = listOf(
            Point(
                yValues = (0..2).map { Random.nextFloat() },
                xValue = musclesNames.first()
            ),
            Point(
                yValues = (0..2).map { Random.nextFloat() },
                xValue = musclesNames[1]
            )
        )

        LibreFitTheme(dynamicColor = false, darkTheme = true) {
            StatisticsScreenContent(
                navController = rememberNavController(),
                muscleDistributionPoints = muscleDistributionPoints,
                muscleDistributionLegendIds = cutoffsIds,
                muscleDistributionStatisticsChart = muscleDistributionStatisticsChart,
                updateMuscleDistributionStatisticsChart = {
                    muscleDistributionStatisticsChart = it
                },
                exercisesDistributionPoints = exerciseDistributionPoints,
                exercisesDistributionLegendIds = emptyList(),
                exercisesDistributionStatisticsChart = exerciseDistributionStatisticsChart,
                updateExercisesDistributionStatisticsChart = {
                    exerciseDistributionStatisticsChart = it
                }
            )
        }
    }
}