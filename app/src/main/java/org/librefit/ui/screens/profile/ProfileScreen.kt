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
 */

package org.librefit.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import org.librefit.R
import org.librefit.enums.ChartMode
import org.librefit.nav.Destination
import org.librefit.ui.components.CustomButton
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.animations.EmptyLottie
import org.librefit.ui.components.animations.StreakLottie
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.charts.CustomCartesianChart
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.util.Formatter.formatTime
import java.text.DecimalFormat

@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    val viewModel: ProfileScreenViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.getWorkoutListFromDB()
    }

    LazyColumn(
        contentPadding = innerPadding,
        modifier = Modifier.padding(start = 15.dp, end = 15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            var clicks = rememberSaveable { mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(500)
                    clicks.intValue = clicks.intValue.dec().coerceAtLeast(0)
                }
            }
            OutlinedCard(
                onClick = {
                    clicks.intValue++
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(0.25f)) {
                        StreakLottie(viewModel.getWeekStreak() + clicks.intValue)
                    }
                    Column(
                        modifier = Modifier.weight(0.75f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.week_streak) + " " + viewModel.getWeekStreak(),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CustomButton(
                    text = stringResource(R.string.statistics),
                    icon = ImageVector.vectorResource(R.drawable.ic_chart),
                    modifier = Modifier.weight(0.5f),
                    elevated = false
                ) {
                    //TODO: statistics view
                }
                CustomButton(
                    text = stringResource(R.string.explore_exercises),
                    icon = Icons.Default.Search,
                    modifier = Modifier.weight(0.5f),
                    elevated = false
                ) {
                    navController.navigate(Destination.ExercisesScreen(addExercises = false))
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CustomButton(
                    text = stringResource(R.string.measurements),
                    icon = ImageVector.vectorResource(R.drawable.ic_monitor),
                    modifier = Modifier.weight(0.5f),
                    elevated = false
                ) {
                    navController.navigate(Destination.MeasurementScreen)
                }
                CustomButton(
                    text = stringResource(R.string.calendar),
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(0.5f),
                    elevated = false
                ) {
                    navController.navigate(Destination.CalendarScreen)
                }
            }
        }

        item { HeadlineText(stringResource(R.string.overview)) }

        if (viewModel.getYAxisDataChart().isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ChartMode.entries) { chartMode ->
                        FilterChip(
                            selected = viewModel.getChartMode() == chartMode,
                            onClick = { viewModel.updateChartMode(chartMode) },
                            label = {
                                Text(
                                    stringResource(
                                        when (chartMode) {
                                            ChartMode.DURATION -> R.string.duration
                                            ChartMode.VOLUME -> R.string.volume
                                            ChartMode.REPS -> R.string.reps
                                        }
                                    )
                                )
                            },
                            leadingIcon = {
                                if (viewModel.getChartMode() == chartMode) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                }
            }

            item {
                CustomCartesianChart(
                    format = when (viewModel.getChartMode()) {
                        ChartMode.DURATION -> DecimalFormat("# " + stringResource(R.string.min))
                        ChartMode.VOLUME -> DecimalFormat("#.## " + stringResource(R.string.kg))
                        ChartMode.REPS -> DecimalFormat()
                    },
                    yAxisData = viewModel.getYAxisDataChart(),
                    xAxisLabels = viewModel.getXAxisDataChart(),
                    columns = true
                )
            }

            item { HeadlineText(stringResource(R.string.your_workouts)) }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyLottie()
                    Text(
                        text = stringResource(R.string.nothing_to_show),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        items(
            items = viewModel.workoutsWithExercises.map { it.workout },
            key = { it.id }
        ) { workout ->
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = workout.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = stringResource(R.string.finished_on) + ": "
                                        + workout.completed.format(viewModel.longFormatter),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(R.string.duration) + ": "
                                        + formatTime(workout.timeElapsed),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(
                            onClick = {
                                sharedViewModel.updateWorkoutId(workout.id)
                                navController.navigate(Destination.InfoWorkoutScreen)
                            },
                        ) {
                            Icon(Icons.Default.Info, stringResource(R.string.about))
                        }
                    }
                }
            }
        }
        bottomMargin()
    }
}


@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(
        navController = rememberNavController(),
        innerPadding = PaddingValues(20.dp),
        sharedViewModel = viewModel()
    )
}