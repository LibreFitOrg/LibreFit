/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.infoWorkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.enums.chart.WorkoutChart
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.nav.Route
import org.librefit.ui.components.ExerciseCardSmall
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.charts.LibreFitCartesianChart
import org.librefit.ui.components.charts.Point
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import org.librefit.util.Formatter.formatDetails
import org.librefit.util.Formatter.formatTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.random.Random

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.InfoWorkoutScreen(
    navController: NavHostController,
    workoutId: Long,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel: InfoWorkoutScreenViewModel = hiltViewModel()

    val points by viewModel.points.collectAsStateWithLifecycle()

    val workout by viewModel.workout.collectAsStateWithLifecycle()

    val routine by viewModel.routine.collectAsStateWithLifecycle()

    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    val workoutChartMode by viewModel.workoutChart.collectAsStateWithLifecycle()

    val volume by viewModel.volume.collectAsStateWithLifecycle()


    InfoWorkoutScreenContent(
        workoutId = workoutId,
        animatedVisibilityScope = animatedVisibilityScope,
        navController = navController,
        workout = workout,
        routine = routine,
        isRoutine = viewModel.isRoutine(),
        workoutDate = viewModel.getDate(),
        volumeExercises = volume,
        workoutChart = workoutChartMode,
        exercises = exercises,
        points = points,
        deleteWorkout = viewModel::deleteWorkout,
        updateChartMode = viewModel::updateChartMode,
        detachWorkoutFromRoutine = viewModel::detachWorkoutFromRoutine,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharedTransitionScope.InfoWorkoutScreenContent(
    workoutId: Long,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navController: NavHostController,
    workout: UiWorkout,
    routine: UiWorkout,
    isRoutine: Boolean,
    workoutDate: String,
    volumeExercises: String,
    workoutChart: WorkoutChart,
    exercises: List<UiExerciseWithSets>,
    points: List<Point>,
    deleteWorkout: () -> Unit,
    detachWorkoutFromRoutine: () -> Unit,
    updateChartMode: (WorkoutChart) -> Unit
) {
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(
                if (isRoutine) R.string.delete_routine_question
                else R.string.delete_workout_question
            ),
            text = stringResource(
                if (isRoutine) R.string.delete_routine_text
                else R.string.delete_workout_text
            ),
            confirmText = stringResource(R.string.delete),
            onConfirm = {
                deleteWorkout()
                showConfirmDialog = false
                navController.navigateUp()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }


    var showUnlikeRoutineDialog by rememberSaveable { mutableStateOf(false) }

    if (showUnlikeRoutineDialog) {
        ConfirmDialog(
            title = stringResource(R.string.unlink_routine_question),
            text = stringResource(R.string.unlink_routine_text),
            confirmText = stringResource(R.string.unlink_dialog),
            onConfirm = {
                detachWorkoutFromRoutine()
                showUnlikeRoutineDialog = false
            },
            onDismiss = { showUnlikeRoutineDialog = false }
        )
    }


    LibreFitScaffold(
        title = AnnotatedString(stringResource(if (isRoutine) R.string.routine else R.string.workout)),
        navigateBack = navController::navigateUp,
        actions = listOf(
            {
                navController.navigate(Route.EditWorkoutScreen(workoutId = workout.id)) {
                    launchSingleTop = true
                }
            },
            {
                showConfirmDialog = true
            }
        ),
        actionsIcons = listOf(
            painterResource(R.drawable.ic_edit),
            painterResource(R.drawable.ic_delete)
        ),
        actionsElevated = listOf(false, false)
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding) {
            item {
                ElevatedCard(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(workoutId),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = workout.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = workout.id.toString() + workout.title
                                ),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )

                        HorizontalDivider()

                        if (!isRoutine) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.duration),
                                    formatTime(workout.timeElapsed)
                                )
                            )
                        }

                        Text(
                            formatDetails(
                                if (isRoutine) stringResource(R.string.creation_date)
                                else stringResource(R.string.label_when),
                                workoutDate
                            )
                        )

                        HorizontalDivider()

                        Text(
                            formatDetails(
                                stringResource(R.string.exercises),
                                exercises.size.toString()
                            )
                        )
                        Text(
                            formatDetails(
                                stringResource(R.string.total_sets),
                                exercises.sumOf { it.sets.size }.toString()
                            )
                        )
                        if (!isRoutine) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.completed_sets),
                                    exercises.sumOf { exe ->
                                        exe.sets.filter { it.completed }.size
                                    }.toString()
                                )
                            )
                        }
                        Text(
                            formatDetails(
                                stringResource(R.string.volume),
                                volumeExercises + " " + stringResource(R.string.kg)
                            )
                        )
                    }
                }
            }

            if (workout.notes.isNotBlank()) {
                item {
                    ElevatedCard(
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(painterResource(R.drawable.ic_info), contentDescription = null)
                                Text(
                                    text = stringResource(R.string.notes),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(workout.notes)
                        }
                    }


                }
            }

            if (isRoutine) {
                item { HeadlineText(stringResource(R.string.past_workouts)) }

                item {
                    LibreFitCartesianChart(
                        decimalCount = when (workoutChart) {
                            WorkoutChart.DURATION -> 0
                            WorkoutChart.VOLUME -> 2
                            WorkoutChart.REPS -> 2
                        },
                        suffix = when (workoutChart) {
                            WorkoutChart.DURATION -> stringResource(R.string.min)
                            WorkoutChart.VOLUME -> stringResource(R.string.kg)
                            WorkoutChart.REPS -> null
                        },
                        points = points,
                        chartModes = WorkoutChart.entries,
                        chartMode = workoutChart,
                        updateChartMode = updateChartMode,
                        onEntrySelection = {
                            navController.navigate(Route.InfoWorkoutScreen(it))
                        }
                    )
                }
            }


            if (routine.id != 0L && !isRoutine) {
                item {
                    HeadlineText(stringResource(R.string.linked_routine))
                }

                item {
                    ElevatedCard(
                        onClick = {
                            navController.navigate(Route.InfoWorkoutScreen(routine.id))
                        },
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(routine.id),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(15.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = routine.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(
                                                routine.id.toString() + routine.title
                                            ),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        )
                                    )
                                    Text(
                                        stringResource(R.string.creation_date) + ": " +
                                                routine.created.format(
                                                    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                                                        .withLocale(
                                                            LocalLocale.current.platformLocale
                                                        )
                                                )
                                    )
                                }
                                IconButton(
                                    onClick = { showUnlikeRoutineDialog = true }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_unlink),
                                        contentDescription = stringResource(R.string.delete)
                                    )
                                }
                            }

                            LibreFitButton(
                                elevated = false,
                                text = stringResource(R.string.open_this_routine),
                                icon = painterResource(R.drawable.ic_open_new)
                            ) {
                                navController.navigate(Route.InfoWorkoutScreen(routine.id))
                            }
                        }
                    }
                }
            }


            item { HeadlineText(stringResource(R.string.exercises)) }
            items(exercises) { e ->
                ExerciseCardSmall(
                    exerciseWithSets = e,
                    isRoutine = isRoutine,
                    animatedVisibilityScope = animatedVisibilityScope
                ) {
                    navController.navigate(
                        Route.InfoExerciseScreen(
                            e.exercise.id,
                            e.exerciseDC.id
                        )
                    ) { launchSingleTop = true }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun InfoRoutineScreenPreview() {
    var routine by remember { mutableStateOf(UiWorkout(title = "Title routine")) }

    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                InfoWorkoutScreenContent(
                    navController = rememberNavController(),
                    deleteWorkout = {},
                    workout = UiWorkout(title = "My long workout title", notes = "This is a note!"),
                    routine = routine,
                    isRoutine = false,
                    workoutDate = Formatter.getFullDateFromLocalDate(LocalDateTime.now()),
                    volumeExercises = "100",
                    workoutChart = WorkoutChart.REPS,
                    exercises = listOf(
                        UiExerciseWithSets(
                            exerciseDC = UiExerciseDC(
                                name = "Name exercise",
                                images = persistentListOf("3_4_Sit-Up/0.jpg")
                            ),
                            sets = persistentListOf(UiSet(), UiSet())
                        )
                    ),
                    points = (0..10).map { Point(listOf(Random.nextDouble())) },
                    detachWorkoutFromRoutine = {
                        routine = UiWorkout()
                    },
                    updateChartMode = {},
                    workoutId = 0,
                    animatedVisibilityScope = this
                )
            }
        }
    }
}