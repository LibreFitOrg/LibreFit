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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import org.librefit.R
import org.librefit.data.ChartData
import org.librefit.db.entity.Workout
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.enums.chart.WorkoutChart
import org.librefit.nav.Route
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.EmptyLottie
import org.librefit.ui.components.animations.StreakLottie
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.charts.LibreFitCartesianChart
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter.formatTime
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.random.Random

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

    ProfileScreenContent(
        innerPadding = innerPadding,
        navController = navController,
        weekStreak = viewModel.getWeekStreak(),
        listChartData = viewModel.getListChartData(),
        workoutsWithExercises = viewModel.workoutsWithExercises,
        workoutChart = viewModel.getChartMode(),
        updateChartMode = viewModel::updateChartMode,
        updateWorkoutId = sharedViewModel::updateWorkoutId,
    )
}

@Composable
private fun ProfileScreenContent(
    innerPadding: PaddingValues,
    navController: NavHostController,
    weekStreak: Int,
    listChartData: List<ChartData>,
    workoutChart: WorkoutChart,
    workoutsWithExercises: SnapshotStateList<WorkoutWithExercisesAndSets>,
    updateChartMode: (WorkoutChart) -> Unit,
    updateWorkoutId: (Long) -> Unit
) {

    LibreFitLazyColumn(innerPadding) {
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
                        StreakLottie(weekStreak + clicks.intValue)
                    }
                    Column(
                        modifier = Modifier.weight(0.75f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.week_streak) + " " + weekStreak,
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
                LibreFitButton(
                    text = stringResource(R.string.statistics),
                    icon = ImageVector.vectorResource(R.drawable.ic_chart),
                    modifier = Modifier.weight(0.5f),
                    elevated = false
                ) {
                    //TODO: statistics view
                }
                LibreFitButton(
                    text = stringResource(R.string.explore_exercises),
                    icon = ImageVector.vectorResource(R.drawable.ic_search),
                    modifier = Modifier.weight(0.5f),
                    elevated = false
                ) {
                    navController.navigate(Route.ExercisesScreen(addExercises = false))
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LibreFitButton(
                    text = stringResource(R.string.measurements),
                    icon = ImageVector.vectorResource(R.drawable.ic_monitor),
                    modifier = Modifier.weight(0.5f),
                    elevated = false
                ) {
                    navController.navigate(Route.MeasurementScreen)
                }
                LibreFitButton(
                    text = stringResource(R.string.calendar),
                    icon = ImageVector.vectorResource(R.drawable.ic_date_range),
                    modifier = Modifier.weight(0.5f),
                    elevated = false
                ) {
                    navController.navigate(Route.CalendarScreen)
                }
            }
        }

        item { HeadlineText(stringResource(R.string.overview)) }

        item {
            LibreFitCartesianChart(
                format = when (workoutChart) {
                    WorkoutChart.DURATION -> DecimalFormat("# " + stringResource(R.string.min))
                    WorkoutChart.VOLUME -> DecimalFormat("#.## " + stringResource(R.string.kg))
                    WorkoutChart.REPS -> DecimalFormat()
                },
                listChartData = listChartData,
                useColumns = true,
                chartMode = workoutChart,
                updateChartMode = { updateChartMode(it as WorkoutChart) }
            )
        }

        item { HeadlineText(stringResource(R.string.your_workouts)) }
        if (workoutsWithExercises.isEmpty()) {
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
            items = workoutsWithExercises.map { it.workout },
            key = { it.id }
        ) { workout ->
            ElevatedCard(
                modifier = Modifier
                    .padding(5.dp)
                    .clip(CardDefaults.elevatedShape)
                    .clickable {
                        updateWorkoutId(workout.id)
                        navController.navigate(Route.InfoWorkoutScreen)
                    }
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
                                        + workout.completed.format(
                                    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                                        .withLocale(
                                            Locale.getDefault()
                                        )
                                ),
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
                                updateWorkoutId(workout.id)
                                navController.navigate(Route.InfoWorkoutScreen)
                            },
                        ) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_info),
                                stringResource(R.string.about)
                            )
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
    LibreFitTheme(false, true) {
        LibreFitScaffold(
            title = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(stringResource(id = R.string.app_name).removeRange(5, 8))
                }
                append(stringResource(id = R.string.app_name).removeRange(0, 5))
            },
            actions = listOf {},
            actionsIcons = listOf(ImageVector.vectorResource(R.drawable.ic_settings)),
            actionsElevated = listOf(false),
            fabIcon = ImageVector.vectorResource(R.drawable.ic_add),
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = false,
                        onClick = { },
                        icon = {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_home),
                                stringResource(R.string.home)
                            )
                        },
                        label = { Text(stringResource(R.string.home)) }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_person),
                                stringResource(R.string.profile)
                            )
                        },
                        label = { Text(stringResource(R.string.profile)) }
                    )
                }
            }
        ) {
            ProfileScreenContent(
                innerPadding = it,
                navController = rememberNavController(),
                weekStreak = 90,
                listChartData = (0..10).map { ChartData(Random.nextFloat()) },
                workoutChart = WorkoutChart.DURATION,
                workoutsWithExercises = remember {
                    mutableStateListOf(
                        WorkoutWithExercisesAndSets(Workout(title = "Workout 1"), listOf())
                    )
                },
                updateChartMode = {},
                updateWorkoutId = {},
            )
        }
    }
}