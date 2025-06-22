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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.data.ChartData
import org.librefit.data.ExerciseDC
import org.librefit.db.entity.Set
import org.librefit.db.entity.Workout
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.enums.chart.WorkoutChart
import org.librefit.nav.Route
import org.librefit.ui.components.ExerciseCardSmall
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.charts.LibreFitCartesianChart
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.components.modalBottomSheets.ExerciseDetailModalBottomSheet
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter.formatDetails
import org.librefit.util.Formatter.formatTime
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.random.Random

@Composable
fun InfoWorkoutScreen(
    sharedViewModel: SharedViewModel,
    navController: NavHostController
) {
    val viewModel: InfoWorkoutScreenViewModel = hiltViewModel()

    val listChartData = viewModel.listChartData.collectAsState()

    val workout = viewModel.workout.collectAsState()

    val routine = viewModel.routine.collectAsState()

    val exercises = viewModel.exercises.collectAsState()

    LaunchedEffect(Unit, viewModel.getChartMode(), exercises.value) {
        viewModel.fetchListChartData()
    }

    LaunchedEffect(Unit) {
        viewModel.initialize(
            sharedViewModel.getPassedWorkout(),
            sharedViewModel.getPassedRoutine(),
            sharedViewModel.getPassedExercises()
        )
    }

    InfoWorkoutScreenContent(
        navController = navController,
        workout = workout.value,
        routine = routine.value,
        workoutDate = viewModel.getDate(),
        volumeExercises = viewModel.getVolumeExercises(),
        workoutChart = viewModel.getChartMode(),
        exercises = exercises.value,
        listChartData = listChartData.value,
        deleteWorkout = viewModel::deleteWorkout,
        updateChartMode = viewModel::updateChartMode,
        detachWorkoutFromRoutine = viewModel::detachWorkoutFromRoutine,
    )
}

@Composable
private fun InfoWorkoutScreenContent(
    navController: NavHostController,
    workout: Workout,
    routine: Workout,
    workoutDate: String,
    volumeExercises: String,
    workoutChart: WorkoutChart,
    exercises: List<ExerciseWithSets>,
    listChartData: List<ChartData>,
    deleteWorkout: () -> Unit,
    detachWorkoutFromRoutine: () -> Unit,
    updateChartMode: (WorkoutChart) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(
                if (workout.routine) R.string.delete_routine_question
                else R.string.delete_workout_question
            ),
            text = stringResource(
                if (workout.routine) R.string.delete_routine_text
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
    var selectedExercise by remember { mutableStateOf<ExerciseDC?>(null) }

    var isModalSheetOpen by remember { mutableStateOf(false) }

    LibreFitScaffold(
        title = AnnotatedString(workout.title),
        navigateBack = { navController.popBackStack() },
        actions = listOf(
            {
                navController.navigate(Route.EditWorkoutScreen)
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
                OutlinedCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (workout.notes.isNotBlank()) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.notes),
                                    workout.notes
                                )
                            )

                            HorizontalDivider()
                        }

                        if (!workout.routine) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.duration),
                                    formatTime(workout.timeElapsed)
                                )
                            )
                        }

                        Text(
                            formatDetails(
                                if (workout.routine) stringResource(R.string.creation_date)
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
                        if (!workout.routine) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.completed_sets),
                                    exercises.sumOf {
                                        it.sets.filter { it.completed == true }.size
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

            if (workout.routine) {
                item { HeadlineText(stringResource(R.string.past_workouts)) }

                item {
                    LibreFitCartesianChart(
                        format = when (workoutChart) {
                            WorkoutChart.DURATION -> DecimalFormat("# " + stringResource(R.string.min))
                            WorkoutChart.VOLUME -> DecimalFormat("#.## " + stringResource(R.string.kg))
                            WorkoutChart.REPS -> DecimalFormat()
                        },
                        listChartData = listChartData,
                        chartMode = workoutChart,
                        updateChartMode = { updateChartMode(it as WorkoutChart) }
                    )
                }
            }


            if (routine.title != "" && !workout.routine) {
                item {
                    HeadlineText(stringResource(R.string.routine))
                }

                item {
                    ElevatedCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.title) + " : " + routine.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    stringResource(R.string.creation_date) + " : " +
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
                    }
                }
            }


            item { HeadlineText(stringResource(R.string.exercises)) }
            items(exercises) { exercise ->
                ExerciseCardSmall(exercise, workout.routine) {
                    selectedExercise = exercise.exerciseDC
                    isModalSheetOpen = true
                }
            }
            bottomMargin()
        }
    }
    // Opened by info icon next to exercise name, it shows the details of an exercise
    if (isModalSheetOpen) {
        ExerciseDetailModalBottomSheet(exercise = selectedExercise!!) {
            isModalSheetOpen = false
        }
    }
}

@Preview
@Composable
private fun InfoRoutineScreenPreview() {
    var routine by remember { mutableStateOf(Workout(title = "Title routine")) }

    LibreFitTheme(false, true) {
        InfoWorkoutScreenContent(
            navController = rememberNavController(),
            deleteWorkout = {},
            workout = Workout(title = "Title workout"),
            routine = routine,
            workoutDate = "DD/MM/YY",
            volumeExercises = "100",
            workoutChart = WorkoutChart.REPS,
            exercises = listOf(
                ExerciseWithSets(
                    exerciseDC = ExerciseDC(name = "Name exercise"),
                    sets = listOf(Set(), Set())
                )
            ),
            listChartData = (0..10).map { ChartData(Random.nextFloat()) },
            detachWorkoutFromRoutine = {
                routine = Workout()
            },
            updateChartMode = {},
        )
    }
}