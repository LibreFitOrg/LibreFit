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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import org.librefit.enums.ChartMode
import org.librefit.nav.Route
import org.librefit.ui.components.ConfirmDialog
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.ExerciseCardSmall
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.charts.CustomCartesianChart
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

    LaunchedEffect(Unit) {
        viewModel.initialize(
            sharedViewModel.getPassedWorkout(),
            sharedViewModel.getPassedRoutine(),
            sharedViewModel.getPassedExercises()
        )
    }

    InfoWorkoutScreenContent(
        navController = navController,
        workout = viewModel.workout.value,
        routine = viewModel.routine.value,
        workoutDate = viewModel.getDate(),
        volumeExercises = viewModel.getVolumeExercises(),
        chartMode = viewModel.getChartMode(),
        exercises = viewModel.exercises,
        listChartData = viewModel.getListChartData(),
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
    chartMode: ChartMode,
    exercises: List<ExerciseWithSets>,
    listChartData: List<ChartData>,
    deleteWorkout: () -> Unit,
    detachWorkoutFromRoutine: () -> Unit,
    updateChartMode: (ChartMode) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(R.string.delete),
            text = stringResource(id = R.string.confirm_delete),
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
            title = stringResource(R.string.unlink_routine),
            text = stringResource(R.string.unlink_routine_desc),
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

    CustomScaffold(
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
        actionsIcons = listOf(Icons.Default.Edit, Icons.Default.Delete),
        actionsElevated = listOf(false, false)
    ) { innerPadding ->
        // Centers the LazyColumn on the screen and restricts its maximum width to 600.dp.
        // This prevents the content from stretching too wide on larger (landscape) screens
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .padding(start = 15.dp, end = 15.dp)
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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

                item { HeadlineText(stringResource(R.string.past_workouts)) }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(ChartMode.entries) {
                            FilterChip(
                                selected = chartMode == it,
                                onClick = { updateChartMode(it) },
                                label = {
                                    Text(
                                        stringResource(
                                            id = when (it) {
                                                ChartMode.DURATION -> R.string.duration
                                                ChartMode.VOLUME -> R.string.volume
                                                ChartMode.REPS -> R.string.reps
                                            }
                                        )
                                    )
                                },
                                leadingIcon = {
                                    if (chartMode == it) {
                                        Icon(
                                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null
                                        )
                                    }
                                },
                                enabled = listChartData.isNotEmpty()
                            )
                        }
                    }
                }

                item {
                    CustomCartesianChart(
                        format = when (chartMode) {
                            ChartMode.DURATION -> DecimalFormat("# " + stringResource(R.string.min))
                            ChartMode.VOLUME -> DecimalFormat("#.## " + stringResource(R.string.kg))
                            ChartMode.REPS -> DecimalFormat()
                        },
                        listChartData = listChartData
                    )
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
                                    .padding(10.dp),
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
                                        imageVector = Icons.Default.Delete,
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
    LibreFitTheme(false, true) {
        InfoWorkoutScreenContent(
            navController = rememberNavController(),
            deleteWorkout = {},
            workout = Workout(title = "Title workout"),
            routine = Workout(title = "Title routine"),
            workoutDate = "DD/MM/YY",
            volumeExercises = "100",
            chartMode = ChartMode.REPS,
            exercises = listOf(
                ExerciseWithSets(
                    exerciseDC = ExerciseDC(name = "Name exercise"),
                    sets = listOf(Set(), Set())
                )
            ),
            listChartData = (0..10).map { ChartData(Random.nextFloat()) },
            detachWorkoutFromRoutine = {},
            updateChartMode = {},
        )
    }
}