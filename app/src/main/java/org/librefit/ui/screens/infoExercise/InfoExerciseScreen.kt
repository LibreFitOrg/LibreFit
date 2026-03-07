/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.infoExercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.enums.SetMode
import org.librefit.enums.chart.BodyweightChart
import org.librefit.enums.chart.ExerciseChart
import org.librefit.enums.chart.LoadChart
import org.librefit.enums.chart.TimeChart
import org.librefit.enums.chart.WeightedBodyweightChart
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle
import org.librefit.enums.pages.InfoExercisePages
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.nav.Route
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.EmptyLottie
import org.librefit.ui.components.charts.LibreFitCartesianChart
import org.librefit.ui.components.charts.Point
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.components.rememberAssetAspectRatio
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.models.UiWorkoutWithExercisesAndSets
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import org.librefit.util.Formatter.formatDetails
import org.librefit.util.Formatter.formatTime
import kotlin.random.Random

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.InfoExerciseScreen(
    id: Long,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navController: NavHostController
) {
    val viewModel: InfoExerciseScreenViewModel = hiltViewModel()

    val workoutsWithExercises by viewModel.workoutsWithExercises.collectAsStateWithLifecycle()

    val points by viewModel.points.collectAsStateWithLifecycle()

    val exerciseChart by viewModel.exerciseChart.collectAsStateWithLifecycle()

    val uiExerciseDC by viewModel.uiExerciseDC.collectAsStateWithLifecycle()

    InfoExerciseScreenContent(
        id = id,
        exerciseDC = uiExerciseDC,
        animatedVisibilityScope = animatedVisibilityScope,
        workoutsWithExercises = workoutsWithExercises,
        points = points,
        exerciseChart = exerciseChart,
        navController = navController,
        updateExerciseChart = viewModel::updateExerciseChart,
        deleteExercise = viewModel::deleteExercise
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InfoExerciseScreenContent(
    id: Long,
    exerciseDC: UiExerciseDC,
    animatedVisibilityScope: AnimatedVisibilityScope,
    workoutsWithExercises: List<UiWorkoutWithExercisesAndSets>,
    points: List<Point>,
    exerciseChart: ExerciseChart,
    navController: NavHostController,
    updateExerciseChart: (ExerciseChart) -> Unit,
    deleteExercise: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { InfoExercisePages.entries.size })
    val coroutineScope = rememberCoroutineScope()

    val stringId = remember(id) { if (id == 0L) "" else id.toString() }

    val showConfirmDeleteDialog = remember { mutableStateOf(false) }

    if (showConfirmDeleteDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.delete_exercise_from_dataset),
            text = stringResource(R.string.delete_exercise_from_dataset_desc),
            confirmText = stringResource(R.string.delete),
            onConfirm = {
                navController.navigateUp()
                showConfirmDeleteDialog.value = false
                deleteExercise()
            },
            onDismiss = {
                showConfirmDeleteDialog.value = false
            }
        )
    }

    LibreFitScaffold(
        navigateBack = navController::navigateUp,
        actions = if (exerciseDC.isCustomExercise) {
            listOf(
                {
                    navController.navigate(
                        Route.EditExerciseScreen(
                            id = id,
                            exerciseDCid = exerciseDC.id
                        )
                    ) {
                        launchSingleTop = true
                    }
                },
                {
                    showConfirmDeleteDialog.value = true
                }
            )
        } else {
            emptyList()
        },
        actionsIcons = if (exerciseDC.isCustomExercise) {
            listOf(painterResource(R.drawable.ic_edit), painterResource(R.drawable.ic_delete))
        } else {
            emptyList()
        },
        actionsElevated = listOf(false, false)
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier.padding(innerPadding)
        ) {
            LibreFitLazyColumn(startEndPadding = 0.dp, bottomSpacer = false) {
                item {
                    Text(
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                        text = exerciseDC.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
                item {
                    AlternatingImages(stringId, exerciseDC, animatedVisibilityScope)
                }
                stickyHeader {
                    PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                        InfoExercisePages.entries.forEachIndexed { i, enum ->
                            Tab(
                                selected = pagerState.currentPage == i,
                                onClick = {
                                    if (pagerState.currentPage != i) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(i)
                                        }
                                    }
                                },
                                text = {
                                    Text(
                                        text = stringResource(
                                            id = when (enum) {
                                                InfoExercisePages.DETAILS -> R.string.details
                                                InfoExercisePages.INSTRUCTIONS -> R.string.instructions
                                                InfoExercisePages.HISTORY -> R.string.history
                                            }
                                        )
                                    )
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(
                                            id = when (enum) {
                                                InfoExercisePages.DETAILS -> R.drawable.ic_badge
                                                InfoExercisePages.INSTRUCTIONS -> R.drawable.ic_reference
                                                InfoExercisePages.HISTORY -> R.drawable.ic_history
                                            }
                                        ),
                                        contentDescription = stringResource(
                                            id = when (enum) {
                                                InfoExercisePages.DETAILS -> R.string.details
                                                InfoExercisePages.INSTRUCTIONS -> R.string.instructions
                                                InfoExercisePages.HISTORY -> R.string.history
                                            }
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
                item {
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(start = 15.dp, end = 15.dp),
                        pageSpacing = 20.dp,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.wrapContentHeight()
                    ) { pageIndex ->
                        when (InfoExercisePages.entries.getOrNull(pageIndex)) {
                            InfoExercisePages.DETAILS -> DetailsPage(maxHeight, exerciseDC)
                            InfoExercisePages.HISTORY -> HistoryPage(
                                workoutsWithExercises = workoutsWithExercises,
                                points = points,
                                exerciseChart = exerciseChart,
                                navController = navController,
                                updateExerciseChart = updateExerciseChart,
                                maxHeight = maxHeight,
                                animatedVisibilityScope = animatedVisibilityScope
                            )

                            InfoExercisePages.INSTRUCTIONS -> InstructionsPage(
                                maxHeight,
                                exerciseDC.instructions
                            )

                            null -> error("Invalid page index: $pageIndex. Expected: ${0..InfoExercisePages.entries.size}")
                        }

                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsPage(
    maxHeight: Dp,
    exercise: UiExerciseDC
) {
    LazyColumn(
        modifier = Modifier.height(maxHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (exercise.force != null) {
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.weight(1f),
                        value = stringResource(Formatter.exerciseEnumToStringId(exercise.force)),
                        label = { Text(stringResource(R.string.force)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.weight(1f),
                    value = stringResource(Formatter.exerciseEnumToStringId(exercise.level)),
                    label = { Text(stringResource(R.string.level)) },
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                )
                if (exercise.mechanic != null) {
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.weight(1f),
                        value = stringResource(Formatter.exerciseEnumToStringId(exercise.mechanic)),
                        label = { Text(stringResource(R.string.mechanic)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
                }
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (exercise.equipment != null) {
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.weight(1f),
                        value = stringResource(Formatter.exerciseEnumToStringId(exercise.equipment)),
                        label = { Text(stringResource(R.string.equipment)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.weight(1f),
                    value = stringResource(Formatter.exerciseEnumToStringId(exercise.category)),
                    label = { Text(stringResource(R.string.category)) },
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                )
            }
        }

        if (exercise.primaryMuscles.isNotEmpty()) {
            item {
                HeadlineText(text = stringResource(id = R.string.primary_muscles))
                LazyRow(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(exercise.primaryMuscles) { muscle ->
                        ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = Formatter.muscleToVectorId(
                                            muscle
                                        )
                                    ),
                                    contentDescription = stringResource(
                                        Formatter.exerciseEnumToStringId(
                                            muscle
                                        )
                                    ),
                                    modifier = Modifier.size(150.dp)
                                )
                                Spacer(Modifier.height(15.dp))

                                Text(
                                    text = stringResource(Formatter.exerciseEnumToStringId(muscle)),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (exercise.secondaryMuscles.isNotEmpty()) {
            item {
                HeadlineText(text = stringResource(id = R.string.secondary_muscles))
                LazyRow(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(exercise.secondaryMuscles) { muscle ->
                        ElevatedCard {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = Formatter.muscleToVectorId(
                                            muscle
                                        )
                                    ),
                                    contentDescription = stringResource(
                                        Formatter.exerciseEnumToStringId(
                                            muscle
                                        )
                                    ),
                                    modifier = Modifier.size(150.dp)
                                )
                                Spacer(Modifier.height(15.dp))

                                Text(
                                    text = stringResource(Formatter.exerciseEnumToStringId(muscle)),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionsPage(
    maxHeight: Dp,
    instructions: List<String>,
) {
    LazyColumn(
        modifier = Modifier.height(maxHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = buildString {
                    instructions.forEachIndexed { index, instruction ->
                        // For all items except the first, add the separator BEFORE the item.
                        if (index > 0) {
                            append("\n\n")
                        }
                        append("${index + 1}. $instruction")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.HistoryPage(
    workoutsWithExercises: List<UiWorkoutWithExercisesAndSets>,
    points: List<Point>,
    exerciseChart: ExerciseChart,
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    maxHeight: Dp,
    updateExerciseChart: (ExerciseChart) -> Unit
) {
    LazyColumn(
        modifier = Modifier.height(maxHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            LibreFitCartesianChart(
                decimalCount = when (exerciseChart) {
                    BodyweightChart.MOST_REPS, BodyweightChart.SESSION_REPS, LoadChart.TOTAL_REPS,
                    WeightedBodyweightChart.TOTAL_REPS -> 2
                    TimeChart.BEST_TIME, TimeChart.TOTAL_TIME -> 0
                    else -> 1
                },
                suffix = when (exerciseChart) {
                    BodyweightChart.MOST_REPS, BodyweightChart.SESSION_REPS, LoadChart.TOTAL_REPS,
                    WeightedBodyweightChart.TOTAL_REPS -> null
                    TimeChart.BEST_TIME, TimeChart.TOTAL_TIME ->  stringResource(R.string.second_abbreviation)
                    else -> stringResource(R.string.kg)
                },
                points = points,
                chartMode = exerciseChart,
                chartModes = when(exerciseChart) {
                    is BodyweightChart -> BodyweightChart.entries
                    is LoadChart -> LoadChart.entries
                    is WeightedBodyweightChart -> WeightedBodyweightChart.entries
                    is TimeChart -> TimeChart.entries
                },
                updateChartMode = updateExerciseChart,
                onEntrySelection = {
                    navController.navigate(Route.InfoWorkoutScreen(it))
                }
            )
        }
        item {
            HeadlineText(stringResource(R.string.past_workouts))
        }

        if (workoutsWithExercises.isEmpty()) {
            item {
                EmptyLottie()
                Text(
                    text = stringResource(R.string.nothing_to_show),
                    textAlign = TextAlign.Center
                )
            }
        }
        items(workoutsWithExercises, key = { it.workout.id }) { workoutWithExercisesAndSets ->
            val workout = workoutWithExercisesAndSets.workout
            ElevatedCard(
                onClick = {
                    navController.navigate(Route.InfoWorkoutScreen(workout.id))
                },
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(workout.id),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .sharedElement(
                                    sharedContentState = rememberSharedContentState(
                                        key = workout.id.toString() + workout.title
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope
                                ),
                            text = workout.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        IconButton(
                            onClick = {
                                navController.navigate(Route.InfoWorkoutScreen(workout.id))
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.info)
                            )
                        }
                    }
                    Text(
                        text = formatDetails(
                            stringResource(R.string.label_when),
                            Formatter.getFullDateFromLocalDate(workout.completed)
                        )
                    )

                    workoutWithExercisesAndSets.exercisesWithSets.forEach { exerciseWithSets ->
                        OutlinedCard(
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (exerciseWithSets.exercise.notes.isNotBlank()) {
                                    Text(
                                        formatDetails(
                                            stringResource(R.string.notes),
                                            exerciseWithSets.exercise.notes
                                        )
                                    )
                                    HorizontalDivider()
                                }

                                Text(
                                    text = formatDetails(
                                        stringResource(R.string.type_of_set),
                                        stringResource(
                                            Formatter.setModeToStringId(exerciseWithSets.exercise.setMode)
                                        )
                                    )
                                )

                                if (exerciseWithSets.exercise.restTime != 0) {
                                    Text(
                                        formatDetails(
                                            stringResource(R.string.rest_time),
                                            exerciseWithSets.exercise.restTime.toString()
                                                    + " " + stringResource(R.string.seconds).replaceFirstChar { it.lowercase() })
                                    )
                                }


                                if (exerciseWithSets.sets.isNotEmpty()) {
                                    HorizontalDivider()

                                    val setMode = exerciseWithSets.exercise.setMode
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Text(stringResource(R.string.set))
                                        if (setMode == SetMode.DURATION) {
                                            Text(stringResource(R.string.time))
                                        } else {
                                            Text(stringResource(R.string.reps))
                                            if (setMode == SetMode.LOAD || setMode == SetMode.BODYWEIGHT_WITH_LOAD) {
                                                Text(
                                                    stringResource(R.string.load) + " (" + stringResource(
                                                        R.string.kg
                                                    ) + ")"
                                                )
                                            }
                                        }
                                        Icon(
                                            painter = painterResource(R.drawable.ic_check),
                                            contentDescription = stringResource(R.string.done)
                                        )

                                    }

                                    Column {
                                        exerciseWithSets.sets.forEachIndexed { index, set ->
                                            val backgroundColor by animateColorAsState(
                                                targetValue = if (set.completed) {
                                                    MaterialTheme.colorScheme.tertiaryContainer
                                                } else {
                                                    Color.Unspecified
                                                },
                                                label = "animated_color_for_set_background"
                                            )
                                            val contentColor by animateColorAsState(
                                                targetValue = if (set.completed) {
                                                    MaterialTheme.colorScheme.onTertiaryContainer
                                                } else {
                                                    Color.Unspecified
                                                },
                                                label = "animated_color_for_set_content"
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(
                                                        RoundedCornerShape(
                                                            topStart = CornerSize(if (index == 0) 45 else 0),
                                                            topEnd = CornerSize(if (index == 0) 45 else 0),
                                                            bottomEnd = CornerSize(
                                                                if (index == exerciseWithSets.sets.lastIndex) 45 else 0
                                                            ),
                                                            bottomStart = CornerSize(
                                                                if (index == exerciseWithSets.sets.lastIndex) 45 else 0
                                                            ),
                                                        )
                                                    )
                                                    .background(backgroundColor)
                                                    .padding(5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceAround
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = contentColor
                                                )
                                                if (setMode == SetMode.DURATION) {
                                                    Text(
                                                        text=formatTime(set.elapsedTime).substring(3),
                                                        color=contentColor
                                                    )
                                                } else {
                                                    Text(text="${set.reps}", color=contentColor)
                                                    if (setMode == SetMode.LOAD || setMode == SetMode.BODYWEIGHT_WITH_LOAD) {
                                                        Text(text = "${set.load}", color = contentColor)
                                                    }
                                                }
                                                Checkbox(
                                                    checked = set.completed,
                                                    onCheckedChange = null
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharedTransitionScope.AlternatingImages(
    stringId: String,
    exercise: UiExerciseDC,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var currentImageIndex by remember { mutableIntStateOf(0) }

    var isRunning by rememberSaveable { mutableStateOf(true) }

    var showWarning by rememberSaveable { mutableStateOf(false) }


    LaunchedEffect(exercise.images) {
        while (exercise.images.isNotEmpty()) {
            if (isRunning) {
                currentImageIndex = (currentImageIndex + 1) % exercise.images.size
            }
            delay(1000)
        }
    }

    Column {
        Box(
            modifier = Modifier.padding(15.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            val model = remember(currentImageIndex) { exercise.images.getOrNull(currentImageIndex) }
            AsyncImage(
                model = model?.let { "file:///android_asset/${it}" },
                fallback = painterResource(R.drawable.no_image),
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                colorFilter = if (model == null) ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant) else null,
                filterQuality = FilterQuality.High,
                modifier = Modifier
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(stringId + exercise.id),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .aspectRatio(
                        ratio = rememberAssetAspectRatio(model, 16f / 9)
                    )
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        MaterialTheme.shapes.extraLarge
                    ),
            )

            if (exercise.images.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilledTonalIconToggleButton(
                        checked = showWarning,
                        modifier = Modifier.padding(10.dp),
                        onCheckedChange = { showWarning = it },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_warning),
                            contentDescription = stringResource(R.string.warning),
                        )
                    }

                    ToggleButton(
                        checked = isRunning,
                        modifier = Modifier.padding(10.dp),
                        onCheckedChange = { isRunning = it },
                        shapes = ToggleButtonDefaults.shapes()
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isRunning) R.drawable.ic_pause else R.drawable.ic_play_arrow
                            ),
                            contentDescription = stringResource(if (isRunning) R.string.pause else R.string.resume),
                        )
                    }
                }
            }
        }
        if (exercise.images.isNotEmpty()) {
            AnimatedVisibility(visible = showWarning) {
                OutlinedCard(
                    modifier = Modifier.padding(start= 15.dp, end= 15.dp),
                    shape = MaterialTheme.shapes.large
                ){
                    Column(Modifier.padding(10.dp)) {
                        Text(
                            text = stringResource(R.string.ai_images_warning),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun InfoExercisePreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                InfoExerciseScreenContent(
                    id = 0L,
                    exerciseChart = TimeChart.BEST_TIME,
                    exerciseDC = UiExerciseDC(
                        name = "3/4 Sit-Up",
                        force = Force.PULL,
                        level = Level.BEGINNER,
                        mechanic = Mechanic.COMPOUND,
                        equipment = Equipment.BODY_ONLY,
                        primaryMuscles = persistentListOf(Muscle.ABDOMINALS),
                        instructions = persistentListOf(
                            "Lie down on the floor and secure your feet. Your legs should be bent at the knees.",
                            "Place your hands behind or to the side of your head. You will begin with your back on the ground. This will be your starting position.",
                            "Flex your hips and spine to raise your torso toward your knees.",
                            "At the top of the contraction your torso should be perpendicular to the ground. Reverse the motion, going only ¾ of the way down.",
                            "Repeat for the recommended amount of repetitions."
                        ),
                        category = Category.STRENGTH,
                        images = persistentListOf("3_4_Sit-Up/0.jpg", "3_4_Sit-Up/1.jpg")
                    ),
                    animatedVisibilityScope = this,
                    workoutsWithExercises = listOf(
                        UiWorkoutWithExercisesAndSets(
                            workout = UiWorkout(id = Random.nextLong(), title = "My first workout", notes = "Very funny"),
                            exercisesWithSets = persistentListOf(
                                UiExerciseWithSets(
                                    exercise = UiExercise(
                                        setMode = SetMode.DURATION,
                                        restTime = 120,
                                        notes = "This is the first exercise",
                                        workoutId = Random.nextLong()
                                    ),
                                    sets = persistentListOf(
                                        UiSet(
                                            completed = true
                                        )
                                    )
                                ),
                                UiExerciseWithSets()
                            )
                        ),
                        UiWorkoutWithExercisesAndSets(
                            workout = UiWorkout(id = Random.nextLong(), title = "My second workout"),
                            exercisesWithSets = persistentListOf(
                                UiExerciseWithSets(
                                    exercise = UiExercise(
                                        setMode = SetMode.DURATION,
                                        notes = "This is the second exercise",
                                        workoutId = Random.nextLong()
                                    ),
                                    sets = persistentListOf()
                                )
                            )
                        )
                    ),
                    points = emptyList(),
                    navController = rememberNavController(),
                    updateExerciseChart = {},
                    deleteExercise = {}
                )
            }
        }
    }
}