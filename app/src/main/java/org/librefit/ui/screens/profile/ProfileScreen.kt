/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.librefit.R
import org.librefit.enums.chart.WorkoutChart
import org.librefit.enums.pages.MainScreenPages
import org.librefit.enums.pages.TutorialContent
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.nav.Route
import org.librefit.ui.components.GetAppNameInAnnotatedBuilder
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
import kotlin.random.Random

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ProfileScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel: ProfileScreenViewModel = hiltViewModel()

    val points by viewModel.points.collectAsStateWithLifecycle()

    val workoutChart by viewModel.workoutChart.collectAsStateWithLifecycle()

    val workoutsWithExercises by viewModel.workoutsWithExercisesUi.collectAsStateWithLifecycle()

    val weekStreak by viewModel.weekStreak.collectAsStateWithLifecycle()


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
            Spacer(Modifier.height(10.dp))
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
                            icon = painterResource(R.drawable.ic_monitor_weight),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f, false),
                            text = stringResource(R.string.start_completing_workout),
                            textAlign = TextAlign.Center,
                        )
                        IconButton(
                            onClick = {
                                navController.navigate(Route.TutorialScreen(TutorialContent.COMPLETE_WORKOUT)) {
                                    launchSingleTop = true
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_help),
                                contentDescription = stringResource(R.string.help)
                            )
                        }
                    }
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
                shape = MaterialTheme.shapes.extraLargeIncreased,
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(workout.id),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
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
                                            LocalLocale.current.platformLocale
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
@Composable
fun StreakCard(weekStreak: Int) {
    /**
     * It counts how many times the user clicks the card. Higher the value, higher the speed animations
     * It decreased of 1 every second until reaching 0.
     */
    var clicks by rememberSaveable { mutableIntStateOf(120) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            clicks = clicks.dec().coerceIn(0, 40)
        }
    }

    val speed = weekStreak.coerceIn(0, 52) + clicks

    val progressAnim = remember { Animatable(0f) }

    // It animates the transition applying the current speed every time it changes
    LaunchedEffect(speed) {
        if (speed > 0) {
            val duration = (32000 / (speed + 1)).coerceIn(1000, 15000)
            // Safe infinite loop respecting coroutine lifecycle
            while (this.isActive) {
                progressAnim.animateTo(
                    targetValue = progressAnim.value + 1f,
                    animationSpec = tween(
                        durationMillis = duration,
                        easing = LinearEasing
                    )
                )
            }
        }
    }


    val shimmerWidthPercentage = 0.3f
    val color1 = MaterialTheme.colorScheme.outlineVariant
    val color2 = MaterialTheme.colorScheme.primary

    val shape = MaterialTheme.shapes.extraLarge
    val pressedShape = MaterialTheme.shapes.small

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val strokeWidthDp = (if (speed < 17) 1 else if (speed < 34) 2 else 3).dp

    OutlinedButton(
        onClick = {
            clicks = clicks.inc().coerceIn(0, 40)
        },
        interactionSource = interactionSource,
        shapes = ButtonDefaults.shapes(
            shape = shape,
            pressedShape = pressedShape
        ),
        modifier = Modifier.drawWithCache {
            val strokeWidthPx = strokeWidthDp.toPx()

            val currentShape = if(isPressed) pressedShape else shape

            val colors = listOf(
                color1,
                color2.copy(alpha = 0.5f + 0.5f * (speed / 52f)),
                color1
            )
            onDrawWithContent {
                drawContent()
                val progress = progressAnim.value % 1f

                // Map the 0..1 progress to actual pixel bounds
                // The total distance travels from outside the left edge to outside the right edge
                val totalTravelDistance = this.size.width * (1f + shimmerWidthPercentage)
                val currentX = if (speed == 0) 0f else (progress * totalTravelDistance)
                val brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(
                        x = currentX - (this.size.width * shimmerWidthPercentage),
                        y = 0f
                    ),
                    end = Offset(
                        x = currentX,
                        y = this.size.height
                    )
                )

                drawOutline(
                    outline = currentShape.createOutline(this.size, this.layoutDirection, this),
                    brush = brush,
                    style = Stroke(width = strokeWidthPx)
                )
            }
        },
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


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview(device = "id:medium_phone", locale = "en")
@Composable
private fun ProfileScreenPreview() {
    val workoutsWithExercises = listOf(
        UiWorkoutWithExercisesAndSets(
            workout = UiWorkout(
                id = Random.nextLong(),
                title = "\uD83C\uDFCB Upper body",
                completed = LocalDateTime.now(),
                timeElapsed = 3689
            ),
            exercisesWithSets = persistentListOf()
        ),
        UiWorkoutWithExercisesAndSets(
            workout = UiWorkout(
                id = Random.nextLong(),
                title = "\uD83C\uDFC3 Tempo run",
                completed = LocalDateTime.now().minusDays(2L),
                timeElapsed = 1245
            ),
            exercisesWithSets = persistentListOf()
        ),
        UiWorkoutWithExercisesAndSets(
            workout = UiWorkout(
                id = Random.nextLong(),
                title = "\uD83D\uDD31 Lower body",
                completed = LocalDateTime.now().minusDays(4L),
                timeElapsed = 3984
            ),
            exercisesWithSets = persistentListOf()
        ),
    )

    val listChartData = workoutsWithExercises.map {
        Point(
            yValues = listOf(it.workout.timeElapsed.toDouble() / 60),
            xValue = Formatter.getShortDateFromLocalDate(it.workout.completed),
            workoutId = it.workout.id
        )
    }

    val pagerState = rememberPagerState(
        initialPage = MainScreenPages.PROFILE.ordinal,
        pageCount = { MainScreenPages.entries.size }
    )

    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        LibreFitScaffold(
            title = buildAnnotatedString {
                GetAppNameInAnnotatedBuilder(MaterialTheme.typography.titleLargeEmphasized)
            },
            actions = listOf({ }, { }, { }),
            actionsIcons = listOf(
                painterResource(R.drawable.ic_favorite),
                painterResource(R.drawable.ic_info),
                painterResource(R.drawable.ic_settings)
            ),
            actionsElevated = listOf(false, false, false),
            fabIcon = painterResource(R.drawable.ic_add),
            bottomBar = {
                NavigationBar {
                    MainScreenPages.entries.forEach { page ->
                        NavigationBarItem(
                            selected = pagerState.currentPage == page.ordinal,
                            onClick = { },
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        id = when (page) {
                                            MainScreenPages.LIBRARY -> R.drawable.ic_library
                                            MainScreenPages.HOME -> R.drawable.ic_home
                                            MainScreenPages.PROFILE -> R.drawable.ic_person
                                        }
                                    ),
                                    contentDescription = stringResource(
                                        id = when (page) {
                                            MainScreenPages.LIBRARY -> R.string.library
                                            MainScreenPages.HOME -> R.string.home
                                            MainScreenPages.PROFILE -> R.string.profile
                                        }
                                    )
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(
                                        id = when (page) {
                                            MainScreenPages.LIBRARY -> R.string.library
                                            MainScreenPages.HOME -> R.string.home
                                            MainScreenPages.PROFILE -> R.string.profile
                                        }
                                    )
                                )
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    ProfileScreenContent(
                        innerPadding = innerPadding,
                        navController = rememberNavController(),
                        weekStreak = 0,
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