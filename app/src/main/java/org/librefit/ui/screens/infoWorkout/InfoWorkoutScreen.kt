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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.enums.chart.WorkoutChart
import org.librefit.nav.Route
import org.librefit.ui.components.ExerciseCardSmall
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.charts.LibreFitCartesianChart
import org.librefit.ui.components.charts.Point
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.components.modalBottomSheets.ExerciseDetailModalBottomSheet
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter.formatDetails
import org.librefit.util.Formatter.formatTime
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.InfoWorkoutScreen(
    navController: NavHostController,
    workoutId: Long,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel: InfoWorkoutScreenViewModel = hiltViewModel()

    val points by viewModel.points.collectAsState()

    val workout by viewModel.workout.collectAsState()

    val routine by viewModel.routine.collectAsState()

    val exercises by viewModel.exercises.collectAsState()

    val workoutChartMode by viewModel.workoutChart.collectAsState()

    val volume by viewModel.volume.collectAsState()


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

@OptIn(ExperimentalSharedTransitionApi::class)
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
    var showConfirmDialog by remember { mutableStateOf(false) }

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
                navController.popBackStack()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }


    var showUnlikeRoutineDialog by remember { mutableStateOf(false) }

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


    /**
     * Holds the information to show in [ExerciseDetailModalBottomSheet]
     */
    var selectedExercise by remember { mutableStateOf<UiExerciseDC?>(null) }

    LibreFitScaffold(
        title = AnnotatedString(stringResource(if (isRoutine) R.string.routine else R.string.workout)),
        navigateBack = { navController.popBackStack() },
        actions = listOf(
            {
                navController.navigate(Route.EditWorkoutScreen(workoutId = workout.id))
            },
            {
                showConfirmDialog = true
            }
        ),
        actionsIcons = listOf(
            ImageVector.vectorResource(R.drawable.ic_edit),
            ImageVector.vectorResource(R.drawable.ic_delete)
        ),
        actionsElevated = listOf(false, false)
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding) {
            item {
                ElevatedCard(
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = workout.id.toString() + workout.title
                                ),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )

                        if (workout.notes.isNotBlank()) {
                            HorizontalDivider()

                            Text(
                                formatDetails(
                                    stringResource(R.string.notes),
                                    workout.notes
                                )
                            )
                        }

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

            if (isRoutine) {
                item { HeadlineText(stringResource(R.string.past_workouts)) }

                item {
                    LibreFitCartesianChart(
                        format = when (workoutChart) {
                            WorkoutChart.DURATION -> DecimalFormat("# " + stringResource(R.string.min))
                            WorkoutChart.VOLUME -> DecimalFormat("#.## " + stringResource(R.string.kg))
                            WorkoutChart.REPS -> DecimalFormat()
                        },
                        points = points,
                        chartMode = workoutChart,
                        updateChartMode = { updateChartMode(it as WorkoutChart) },
                        navController = navController
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
                                                            Locale.getDefault()
                                                        )
                                                )
                                    )
                                }
                                IconButton(
                                    onClick = { showUnlikeRoutineDialog = true }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_unlink),
                                        contentDescription = stringResource(R.string.delete)
                                    )
                                }
                            }

                            LibreFitButton(
                                elevated = false,
                                text = stringResource(R.string.open_this_routine),
                                icon = ImageVector.vectorResource(R.drawable.ic_open_new)
                            ) {
                                navController.navigate(Route.InfoWorkoutScreen(routine.id))
                            }
                        }
                    }
                }
            }


            item { HeadlineText(stringResource(R.string.exercises)) }
            items(exercises) { exercise ->
                ExerciseCardSmall(exercise, isRoutine) {
                    selectedExercise = exercise.exerciseDC
                }
            }
        }
    }
    // Opened by info icon next to exercise name, it shows the details of an exercise
    selectedExercise?.let {
        ExerciseDetailModalBottomSheet(exercise = selectedExercise!!) {
            selectedExercise = null
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun InfoRoutineScreenPreview() {
    var routine by remember { mutableStateOf(UiWorkout(title = "Title routine")) }

    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                InfoWorkoutScreenContent(
                    navController = rememberNavController(),
                    deleteWorkout = {},
                    workout = UiWorkout(title = "My workout", notes = "This is a note!"),
                    routine = routine,
                    isRoutine = false,
                    workoutDate = "DD/MM/YY",
                    volumeExercises = "100",
                    workoutChart = WorkoutChart.REPS,
                    exercises = listOf(
                        UiExerciseWithSets(
                            exerciseDC = UiExerciseDC(name = "Name exercise"),
                            sets = persistentListOf(UiSet(), UiSet())
                        )
                    ),
                    points = (0..10).map { Point(listOf(Random.nextFloat())) },
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