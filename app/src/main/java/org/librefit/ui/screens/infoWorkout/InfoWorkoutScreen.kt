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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.enums.SetMode
import org.librefit.ui.components.ConfirmDialog
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.modalBottomSheets.ExerciseDetailModalBottomSheet
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.util.ExerciseDC
import org.librefit.util.formatDetails
import org.librefit.util.formatTime

@Composable
fun InfoWorkoutScreen(
    sharedViewModel: SharedViewModel,
    navController: NavHostController
) {
    /*
    This will pass "workoutId" to the view model so it can load and link
    exercises from db just one time (in initialization)
     */
    val viewModel: InfoWorkoutScreenViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.initializeWorkout(sharedViewModel.getPassedWorkout())
        viewModel.initializeExercises(sharedViewModel.getPassedExercises())
        viewModel.initializeRoutine(sharedViewModel.getPassedRoutine())
    }


    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(R.string.delete),
            text = stringResource(id = R.string.confirm_delete),
            onConfirm = {
                viewModel.deleteWorkout()
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
                viewModel.detachWorkoutFromRoutine()
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
        title = viewModel.getWorkoutTitle(),
        navigateBack = { navController.popBackStack() },
        actions = listOf(
            {
                //TODO: open create routine screen
            },
            {
                showConfirmDialog = true
            }
        ),
        actionsIcons = listOf(Icons.Default.Edit, Icons.Default.Delete),
        actionsElevated = listOf(false, false)
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(start = 15.dp, end = 15.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { HeadlineText(stringResource(R.string.overview)) }
            item {
                OutlinedCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (viewModel.getNotes().isNotBlank()) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.notes),
                                    viewModel.getNotes()
                                )
                            )
                        }

                        if (!viewModel.isRoutine()) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.duration),
                                    viewModel.getElapsedTime()
                                )
                            )
                        }

                        Text(
                            formatDetails(
                                if (viewModel.isRoutine()) stringResource(R.string.creation_date)
                                else stringResource(R.string.label_when),
                                viewModel.getDate()
                            )
                        )
                        Text(
                            formatDetails(
                                stringResource(R.string.exercises),
                                viewModel.getTotalExercises()
                            )
                        )
                        Text(
                            formatDetails(
                                stringResource(R.string.total_sets),
                                viewModel.getTotalSets()
                            )
                        )
                        if (!viewModel.isRoutine()) {
                            Text(
                                formatDetails(
                                    stringResource(R.string.completed_sets),
                                    viewModel.getCompletedSets()
                                )
                            )
                        }
                        Text(
                            formatDetails(
                                stringResource(R.string.volume),
                                viewModel.getVolumeExercises() + " " + stringResource(R.string.kg)
                            )
                        )
                    }
                }
            }


            if (viewModel.getRoutineTitle() != "") {
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
                                    text = stringResource(R.string.title) + " : " + viewModel.getRoutineTitle(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(stringResource(R.string.creation_date) + " : " + viewModel.getRoutineDate())
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
            items(viewModel.exercises) { exercise ->
                ElevatedCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = exercise.exerciseDC.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            IconButton(
                                onClick = {
                                    selectedExercise = exercise.exerciseDC
                                    isModalSheetOpen = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(R.string.info)
                                )
                            }
                        }


                        if (exercise.note.isNotBlank()) {
                            HorizontalDivider()

                            Text(formatDetails(stringResource(R.string.notes), exercise.note))
                        }

                        if (exercise.restTime != 0) {
                            HorizontalDivider()
                            Text(
                                formatDetails(
                                    stringResource(R.string.rest_time), exercise.restTime.toString()
                                            + " " + stringResource(R.string.seconds).replaceFirstChar { it.lowercase() })
                            )
                        }

                        if (exercise.sets.isNotEmpty()) {
                            HorizontalDivider()

                            val setMode = exercise.setMode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text(stringResource(R.string.set))
                                if (setMode == SetMode.TIME) {
                                    Text(stringResource(R.string.time))
                                } else {
                                    Text(stringResource(R.string.reps))
                                    if (setMode == SetMode.WEIGHT) {
                                        Text(
                                            stringResource(R.string.weight) + "(" + stringResource(
                                                R.string.kg
                                            ) + ")"
                                        )
                                    }
                                }
                                if (!viewModel.isRoutine()) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = stringResource(R.string.done)
                                    )
                                }
                            }

                            exercise.sets.forEachIndexed { index, set ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .background(
                                            if (!viewModel.isRoutine() && set.completed) MaterialTheme.colorScheme.secondaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainerLow
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Text("${index + 1}")
                                    if (setMode == SetMode.TIME) {
                                        Text(formatTime(set.elapsedTime).substring(3))
                                    } else {
                                        Text("${set.reps}")
                                        if (setMode == SetMode.WEIGHT) {
                                            Text("${set.weight}")
                                        }
                                    }
                                    if (!viewModel.isRoutine()) {
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
            bottomMargin()
        }

        // Opened by info icon next to exercise name, it shows the details of an exercise
        if (isModalSheetOpen) {
            ExerciseDetailModalBottomSheet(exercise = selectedExercise!!) {
                isModalSheetOpen = false
            }
        }
    }
}

@Preview
@Composable
private fun InfoRoutineScreenPreview() {
    InfoWorkoutScreen(
        navController = rememberNavController(),
        sharedViewModel = viewModel()
    )
}