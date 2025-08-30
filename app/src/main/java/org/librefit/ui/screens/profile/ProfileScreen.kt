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

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import org.librefit.R
import org.librefit.enums.chart.WorkoutChart
import org.librefit.nav.Route
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.EmptyLottie
import org.librefit.ui.components.animations.StreakLottie
import org.librefit.ui.components.charts.LibreFitCartesianChart
import org.librefit.ui.components.charts.Point
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.models.UiWorkoutWithExercisesAndSets
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import org.librefit.util.Formatter.formatTime
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ProfileScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel: ProfileScreenViewModel = hiltViewModel()

    val points by viewModel.points.collectAsState()

    val workoutChart by viewModel.workoutChart.collectAsState()

    val workoutsWithExercises by viewModel.workoutsWithExercisesUi.collectAsState()

    val weekStreak by viewModel.weekStreak.collectAsState()


    ProfileScreenContent(
        animatedVisibilityScope = animatedVisibilityScope,
        innerPadding = innerPadding,
        navController = navController,
        weekStreak = weekStreak,
        points = points,
        workoutsWithExercises = workoutsWithExercises,
        workoutChart = workoutChart,
        updateChartMode = viewModel::updateChartMode
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharedTransitionScope.ProfileScreenContent(
    animatedVisibilityScope: AnimatedVisibilityScope,
    innerPadding: PaddingValues,
    navController: NavHostController,
    weekStreak: Int,
    points: List<Point>,
    workoutChart: WorkoutChart,
    workoutsWithExercises: List<UiWorkoutWithExercisesAndSets>,
    updateChartMode: (WorkoutChart) -> Unit
) {

    LibreFitLazyColumn(innerPadding) {
        item {
            StreakCard(weekStreak)
        }

        item {
            val interactionSources = remember { List(2) { MutableInteractionSource() } }
            ButtonGroup(
                overflowIndicator = {}
            ) {
                customItem(
                    buttonGroupContent = {
                        LibreFitButton(
                            text = stringResource(R.string.statistics),
                            icon = painterResource(R.drawable.ic_chart),
                            modifier = Modifier
                                .weight(0.5f)
                                .animateWidth(interactionSources[0]),
                            elevated = false,
                            interactionSource = interactionSources[0]
                        ) {
                            navController.navigate(Route.StatisticsScreen) {
                                launchSingleTop = true
                            }
                        }
                    },
                    menuContent = {}
                )
                customItem(
                    buttonGroupContent = {
                        LibreFitButton(
                            text = stringResource(R.string.exercises),
                            icon = painterResource(R.drawable.ic_search),
                            modifier = Modifier
                                .weight(0.5f)
                                .animateWidth(interactionSources[1]),
                            elevated = false,
                            interactionSource = interactionSources[1]
                        ) {
                            navController.navigate(Route.ExercisesScreen(addExercises = false)) {
                                launchSingleTop = true
                            }
                        }
                    },
                    menuContent = {}
                )
            }
        }

        item {
            val interactionSources = remember { List(2) { MutableInteractionSource() } }
            ButtonGroup(
                overflowIndicator = {}
            ) {
                customItem(
                    buttonGroupContent = {
                        LibreFitButton(
                            text = stringResource(R.string.measurements),
                            icon = painterResource(R.drawable.ic_monitor),
                            modifier = Modifier
                                .weight(0.5f)
                                .animateWidth(interactionSources[0]),
                            elevated = false,
                            interactionSource = interactionSources[0]
                        ) {
                            navController.navigate(Route.MeasurementScreen) {
                                launchSingleTop = true
                            }
                        }
                    },
                    menuContent = {}
                )
                customItem(
                    buttonGroupContent = {
                        LibreFitButton(
                            text = stringResource(R.string.calendar),
                            icon = painterResource(R.drawable.ic_date_range),
                            modifier = Modifier
                                .weight(0.5f)
                                .animateWidth(interactionSources[1]),
                            elevated = false,
                            interactionSource = interactionSources[1]
                        ) {
                            navController.navigate(Route.CalendarScreen) { launchSingleTop = true }
                        }
                    },
                    menuContent = {}
                )
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
                points = points,
                useColumns = true,
                chartMode = workoutChart,
                updateChartMode = { updateChartMode(it as WorkoutChart) },
                navController = navController
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
                onClick = {
                    navController.navigate(Route.InfoWorkoutScreen(workoutId = workout.id)) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(workout.id),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
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
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(
                                        workout.id.toString() + workout.title
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
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
                                navController.navigate(Route.InfoWorkoutScreen(workoutId = workout.id)) {
                                    launchSingleTop = true
                                }
                            },
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_info),
                                stringResource(R.string.about)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun StreakCard(weekStreak: Int) {
    /**
     * It counts how many times the user clicks the card. Higher the value, higher the speed animations
     * It decreased of 1 every second until reaching 0.
     */
    val clicks = rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            clicks.intValue = clicks.intValue.coerceIn(1, 40) - 1
        }
    }

    val speed = weekStreak.coerceIn(0, 52) + clicks.intValue

    var animationProgress by rememberSaveable { mutableFloatStateOf(0f) }

    // It animates the transition applying the current speed every time it changes
    LaunchedEffect(speed) {
        while (true) {
            animate(
                initialValue = animationProgress,
                targetValue = animationProgress + 1f,
                animationSpec = tween(
                    durationMillis = (32000 / (speed + 1)).coerceIn(1000, 15000),
                    easing = LinearEasing
                )
            ) { value, _ ->
                if (speed != 0) {
                    animationProgress = value
                }
            }
        }
    }

    BoxWithConstraints {
        val spaceMaxWidth = with(LocalDensity.current) { maxWidth.toPx() }
        val spaceMaxHeight = with(LocalDensity.current) { maxHeight.toPx() }

        val shimmerWidthPercentage = 0.3f

        val translateAnim = if (speed == 0) 0f else {
            (animationProgress % 1f) * spaceMaxWidth * (1 + shimmerWidthPercentage)
        }

        val brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.outlineVariant,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f + 0.5f * (speed / 52f)),
                MaterialTheme.colorScheme.outlineVariant
            ),
            start = Offset(
                translateAnim - (spaceMaxWidth * shimmerWidthPercentage),
                spaceMaxHeight
            ),
            end = Offset(translateAnim, spaceMaxHeight)
        )

        OutlinedButton(
            onClick = {
                clicks.intValue = clicks.intValue.coerceIn(0, 39) + 1
            },
            shapes = ButtonDefaults.shapes(
                shape = MaterialTheme.shapes.extraLarge,
                pressedShape = MaterialTheme.shapes.small
            ),
            border = BorderStroke(
                width = (if (speed < 17) 1 else if (speed < 34) 2 else 3).dp,
                brush = brush
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(0.25f)) {
                    StreakLottie(speed)
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
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun ProfileScreenPreview() {
    val workoutsWithExercises = (0..5).map {
        UiWorkoutWithExercisesAndSets(
            workout = UiWorkout(
                id = Random.nextLong(),
                title = "Workout $it",
                completed = LocalDateTime.now().minusDays(it.toLong() * 2),
                timeElapsed = Random.nextInt(0, 90)
            ),
            exercisesWithSets = persistentListOf()
        )
    }

    val listChartData = workoutsWithExercises.map {
        Point(
            yValues = listOf(it.workout.timeElapsed.toDouble()),
            xValue = Formatter.getShortDateFromLocalDate(it.workout.completed),
            workoutId = it.workout.id
        )
    }

    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SharedTransitionLayout {
            LibreFitScaffold(
                title = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(stringResource(id = R.string.app_name).removeRange(5, 8))
                    }
                    append(stringResource(id = R.string.app_name).removeRange(0, 5))
                },
                actions = listOf {},
                actionsIcons = listOf(painterResource(R.drawable.ic_settings)),
                actionsElevated = listOf(false),
                fabIcon = painterResource(R.drawable.ic_add),
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = false,
                            onClick = { },
                            icon = {
                                Icon(
                                    painterResource(R.drawable.ic_home),
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
                                    painterResource(R.drawable.ic_person),
                                    stringResource(R.string.profile)
                                )
                            },
                            label = { Text(stringResource(R.string.profile)) }
                        )
                    }
                }
            ) { innerPadding ->
                AnimatedVisibility(visible = true) {
                    ProfileScreenContent(
                        innerPadding = innerPadding,
                        navController = rememberNavController(),
                        weekStreak = 2,
                        points = listChartData,
                        workoutChart = WorkoutChart.DURATION,
                        workoutsWithExercises = workoutsWithExercises,
                        updateChartMode = {},
                        animatedVisibilityScope = this
                    )
                }

            }
        }
    }
}