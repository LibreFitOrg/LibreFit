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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.remember
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
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.coroutines.delay
import org.librefit.R
import org.librefit.nav.Destination
import org.librefit.ui.components.CustomTextButton
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.animations.EmptyLottie
import org.librefit.ui.components.animations.StatsLottie
import org.librefit.ui.components.animations.StreakLottie
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.rememberMarker
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.util.formatTime
import java.text.DecimalFormat
import java.time.LocalDateTime

@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    val viewModel: ProfileScreenViewModel = hiltViewModel()

    val labelListKey = ExtraStore.Key<List<String>>()
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(viewModel.getChartMode()) {
        modelProducer.runTransaction {
            columnSeries { series(viewModel.getYAxisDataChart()) }
            extras { it[labelListKey] = viewModel.getXAxisDataChart() }
        }
    }

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues = innerPadding)
            .padding(start = 15.dp, end = 15.dp)
            .fillMaxSize(),
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
                CustomTextButton(
                    text = stringResource(R.string.statistics),
                    icon = ImageVector.vectorResource(R.drawable.ic_chart),
                    modifier = Modifier.weight(0.5f),
                    elevated = false
                ) {
                    //TODO: statistics view
                }
                CustomTextButton(
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
                CustomTextButton(
                    text = stringResource(R.string.measurements),
                    icon = ImageVector.vectorResource(R.drawable.ic_monitor),
                    modifier = Modifier.weight(0.5f),
                    elevated = false
                ) {
                    //TODO: body measurements
                }
                CustomTextButton(
                    text = stringResource(R.string.calendar),
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(0.5f),
                    elevated = false
                ) {
                    //TODO: calendar view
                }
            }
        }

        item { HeadlineText(stringResource(R.string.overview)) }

        if (viewModel.workoutList.isNotEmpty()) {

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(3) { index ->
                        FilterChip(
                            selected = viewModel.getChartMode() == index,
                            onClick = { viewModel.updateChartMode(index) },
                            label = {
                                Text(
                                    stringResource(
                                        when (index) {
                                            0 -> R.string.duration
                                            1 -> R.string.volume
                                            else -> R.string.reps
                                        }
                                    )
                                )
                            },
                            leadingIcon = {
                                if (viewModel.getChartMode() == index) {
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
                ProvideVicoTheme(rememberM3VicoTheme()) {
                    val format = when (viewModel.getChartMode()) {
                        0 -> DecimalFormat("# " + stringResource(R.string.min))
                        1 -> DecimalFormat("#.## " + stringResource(R.string.kg))
                        else -> DecimalFormat()
                    }
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberColumnCartesianLayer(
                                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                    rememberLineComponent(
                                        fill = fill(MaterialTheme.colorScheme.primary),
                                        thickness = 32.dp,
                                        shape = CorneredShape.rounded(32, 32)
                                    )
                                ),
                                columnCollectionSpacing = 64.dp
                            ),
                            marker = rememberMarker(
                                DefaultCartesianMarker.ValueFormatter.default(format)

                            ),
                            startAxis = VerticalAxis.rememberStart(
                                valueFormatter = CartesianValueFormatter.decimal(format)
                            ),
                            bottomAxis = HorizontalAxis.rememberBottom(
                                valueFormatter = CartesianValueFormatter { context, x, _ ->
                                    context.model.extraStore.getOrNull(labelListKey)?.get(x.toInt())
                                        ?: LocalDateTime.now().format(viewModel.shortFormatter)
                                }
                            ),
                        ),
                        zoomState = rememberVicoZoomState(
                            zoomEnabled = false,
                            minZoom = Zoom.fixed(),
                            maxZoom = Zoom.fixed()
                        ),
                        modelProducer = modelProducer,
                    )
                }
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    StatsLottie()
                    Text(
                        text = stringResource(R.string.complete_workout_to_display_chart),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }



        item { HeadlineText(stringResource(R.string.your_workouts)) }

        if (viewModel.workoutList.isEmpty()) {
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
            items = viewModel.workoutList,
            key = { it.id }
        ) { workout ->
            ElevatedCard(
                modifier = Modifier.animateItem()
            ) {
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
                                navController.navigate(Destination.InfoRoutineScreen)
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