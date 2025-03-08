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

package org.librefit.ui.screens.beforeSaving

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.enums.SuccessMessage
import org.librefit.nav.Destination
import org.librefit.ui.components.ConfirmDialog
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.util.Formatter.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeforeSavingScreen(
    sharedViewModel: SharedViewModel,
    navController: NavHostController
) {
    val viewModel: BeforeSavingScreenViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.initializeWorkout(sharedViewModel.getPassedWorkout())
        viewModel.initializeExercises(sharedViewModel.getPassedExercises())
        viewModel.initializeRoutine(sharedViewModel.getPassedRoutine())
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


    var infoMode by remember { mutableStateOf(InfoMode.DISMISS) }

    if (infoMode != InfoMode.DISMISS) {
        InfoModalBottomSheet(infoMode) { infoMode = InfoMode.DISMISS }
    }


    val datePickerState = rememberDatePickerState()
    var showDatePickerDialog by remember { mutableStateOf(false) }

    if (showDatePickerDialog == true) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateCompletedDate(datePickerState.selectedDateMillis)
                        showDatePickerDialog = false
                    }
                ) {
                    Text(stringResource(R.string.ok_dialog))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(stringResource(R.string.cancel_dialog))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    CustomScaffold(
        title = AnnotatedString(stringResource(R.string.overview)),
        navigateBack = { navController.popBackStack() },
        actions = listOf {
            viewModel.saveExercisesWithWorkout()
            navController.navigate(Destination.SuccessScreen(SuccessMessage.WORKOUT_SAVED)) {
                popUpTo(Destination.MainScreen) { inclusive = false }
            }
        },
        actionsDescription = listOf(stringResource(R.string.save)),
        actionsEnabled = listOf(viewModel.isTitleAllowed())
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(
                    value = viewModel.getWorkoutTitle(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    onValueChange = { newTitle ->
                        viewModel.updateWorkoutTitle(newTitle)
                    },
                    trailingIcon = {
                        if (!viewModel.isTitleAllowed()) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(R.string.warning)
                            )
                        }
                    },
                    isError = !viewModel.isTitleAllowed(),
                    label = { Text(text = stringResource(id = R.string.title)) },
                    supportingText = {
                        when {
                            viewModel.isTitleTooLong() -> {
                                Text(stringResource(R.string.title_length_exceeded_30))
                            }

                            viewModel.isTitleEmpty() -> {
                                Text(stringResource(R.string.title_cannot_be_empty))
                            }
                        }
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = viewModel.getWorkoutNotes(),
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { newNotes ->
                        viewModel.updateWorkoutNotes(newNotes)
                    },
                    label = { Text(text = stringResource(id = R.string.notes)) },
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.statistics),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = {
                            infoMode = InfoMode.BEFORE_SAVING_STATS
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.info)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    OutlinedTextField(
                        modifier = Modifier.weight(0.5f),
                        value = formatTime(viewModel.getTimeElapsed()),
                        label = { Text(stringResource(R.string.elapsed_time)) },
                        onValueChange = { string ->
                            val stringValue = string.filter { it.isDigit() }.takeLast(6)

                            val seconds = stringValue.toInt() % 100
                            val minutes = (stringValue.toInt() % 10000 - seconds) / 100
                            val hours = (stringValue.toInt() - stringValue.toInt() % 10000) / 10000

                            viewModel.setTimeElapsed(hours * 3600 + minutes * 60 + seconds)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(0.5f),
                        value = viewModel.getCompletedDate(),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.label_when)) },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePickerDialog = !showDatePickerDialog }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = stringResource(R.string.select_date)
                                )
                            }
                        }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(0.5f),
                        value = viewModel.getVolumeExercises(),
                        label = { Text(stringResource(R.string.volume)) },
                        suffix = { Text(stringResource(R.string.kg)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(0.5f),
                        value = "${viewModel.getExercises().size}",
                        label = { Text(stringResource(R.string.exercises)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(0.5f),
                        value = "${viewModel.getTotalSets()}",
                        label = { Text(stringResource(R.string.total_sets)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(0.5f),
                        value = "${viewModel.getCompletedSets()}",
                        label = { Text(stringResource(R.string.completed_sets)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
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


            item {
                HeadlineText(stringResource(R.string.exercises))
            }

            item {
                ElevatedCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = stringResource(R.string.completed_sets),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        viewModel.getExercises().forEachIndexed { index, exercise ->
                            if (index != 0) {
                                HorizontalDivider()
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = exercise.exerciseDC.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("${exercise.sets.filter { it.completed }.size} / ${exercise.sets.size}")
                            }
                        }
                    }
                }
            }

            bottomMargin()
        }
    }
}