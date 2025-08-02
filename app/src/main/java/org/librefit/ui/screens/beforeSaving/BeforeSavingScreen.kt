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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.db.entity.Workout
import org.librefit.enums.InfoMode
import org.librefit.enums.SuccessMessage
import org.librefit.nav.Route
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import org.librefit.util.Formatter.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeforeSavingScreen(
    navController: NavHostController
) {
    val viewModel: BeforeSavingScreenViewModel = hiltViewModel()

    val volume by viewModel.volume.collectAsState()

    val workout by viewModel.workout.collectAsState()

    val routine by viewModel.routine.collectAsState()


    val showUnlikeRoutineDialog = remember { mutableStateOf(false) }

    if (showUnlikeRoutineDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.unlink_routine_question),
            text = stringResource(R.string.unlink_routine_text),
            onConfirm = {
                viewModel.detachWorkoutFromRoutine()
                showUnlikeRoutineDialog.value = false
            },
            onDismiss = { showUnlikeRoutineDialog.value = false }
        )
    }


    val datePickerState = rememberDatePickerState()
    val showDatePickerDialog = remember { mutableStateOf(false) }

    if (showDatePickerDialog.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateCompletedDate(datePickerState.selectedDateMillis)
                        showDatePickerDialog.value = false
                    }
                ) {
                    Text(stringResource(R.string.ok_dialog))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog.value = false }) {
                    Text(stringResource(R.string.cancel_dialog))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    BeforeSavingScreenContent(
        navController = navController,
        showUnlikeRoutineDialog = showUnlikeRoutineDialog,
        showDatePickerDialog = showDatePickerDialog,
        exercises = viewModel.exercises,
        workout = workout,
        routine = routine,
        volumeExercises = volume,
        isTitleTooLong = viewModel.isTitleTooLong(),
        isTitleEmpty = viewModel.isTitleEmpty(),
        updateWorkoutTitle = viewModel::updateWorkoutTitle,
        updateWorkoutNotes = viewModel::updateWorkoutNotes,
        saveExercisesWithWorkout = viewModel::saveExercisesWithWorkout,
        setTimeElapsed = viewModel::setTimeElapsed
    )
}

@Composable
fun BeforeSavingScreenContent(
    navController: NavHostController,
    showUnlikeRoutineDialog: MutableState<Boolean>,
    showDatePickerDialog: MutableState<Boolean>,
    exercises: List<UiExerciseWithSets>,
    workout: Workout = Workout(),
    routine: Workout = Workout(),
    volumeExercises: String,
    isTitleTooLong: Boolean,
    isTitleEmpty: Boolean,
    updateWorkoutTitle: (String) -> Unit,
    updateWorkoutNotes: (String) -> Unit,
    saveExercisesWithWorkout: () -> Unit,
    setTimeElapsed: (Int) -> Unit
) {
    val routineTitle = routine.title


    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.overview)),
        navigateBack = { navController.popBackStack() },
        actions = listOf {
            saveExercisesWithWorkout()
            navController.navigate(Route.SuccessScreen(SuccessMessage.WORKOUT_SAVED)) {
                popUpTo(Route.MainScreen) { inclusive = false }
            }
        },
        actionsDescription = listOf(stringResource(R.string.save)),
        actionsEnabled = listOf(!isTitleEmpty && !isTitleTooLong)
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding) {
            item {
                OutlinedTextField(
                    value = workout.title,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    onValueChange = updateWorkoutTitle,
                    trailingIcon = {
                        if (isTitleEmpty || isTitleTooLong) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_warning),
                                contentDescription = stringResource(R.string.warning)
                            )
                        }
                    },
                    isError = isTitleEmpty || isTitleTooLong,
                    label = { Text(text = stringResource(id = R.string.title)) },
                    supportingText = {
                        when {
                            isTitleTooLong -> {
                                Text(stringResource(R.string.title_length_exceeded_30))
                            }

                            isTitleEmpty -> {
                                Text(stringResource(R.string.title_cannot_be_empty))
                            }
                        }
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = workout.notes,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = updateWorkoutNotes,
                    label = { Text(text = stringResource(id = R.string.notes)) },
                )
            }
            item {
                HeadlineText(stringResource(R.string.statistics), InfoMode.BEFORE_SAVING_STATS)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    OutlinedTextField(
                        modifier = Modifier.weight(0.5f),
                        value = formatTime(workout.timeElapsed),
                        label = { Text(stringResource(R.string.elapsed_time)) },
                        onValueChange = { string ->
                            val stringValue = string.filter { it.isDigit() }.takeLast(6)

                            val seconds = stringValue.toInt() % 100
                            val minutes = (stringValue.toInt() % 10000 - seconds) / 100
                            val hours =
                                (stringValue.toInt() - stringValue.toInt() % 10000) / 10000

                            setTimeElapsed(hours * 3600 + minutes * 60 + seconds)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(0.5f),
                        value = Formatter.getShortDateFromLocalDate(workout.completed),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.label_when)) },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                showDatePickerDialog.value = !showDatePickerDialog.value
                            }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_date_range),
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
                        value = volumeExercises,
                        label = { Text(stringResource(R.string.volume)) },
                        suffix = { Text(stringResource(R.string.kg)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(0.5f),
                        value = "${exercises.size}",
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
                        value = "${exercises.sumOf { it.sets.size }}",
                        label = { Text(stringResource(R.string.total_sets)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(0.5f),
                        value = "${
                            exercises.sumOf { exercise ->
                                exercise.sets.filter { it.completed }.size
                            }
                        }",
                        label = { Text(stringResource(R.string.completed_sets)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
                }
            }

            if (routineTitle != "") {
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
                                    text = stringResource(R.string.title) + " : " + routineTitle,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    stringResource(R.string.creation_date) + " : "
                                            + Formatter.getShortDateFromLocalDate(routine.created)
                                )
                            }
                            IconButton(
                                onClick = { showUnlikeRoutineDialog.value = true }
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
                        exercises.forEachIndexed { index, exercise ->
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

@Preview
@Composable
private fun BeforeSavingScreenPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        BeforeSavingScreenContent(
            navController = rememberNavController(),
            showUnlikeRoutineDialog = remember { mutableStateOf(false) },
            showDatePickerDialog = remember { mutableStateOf(false) },
            exercises = listOf(
                UiExerciseWithSets(
                    exerciseDC = UiExerciseDC(name = "Pullup")
                )
            ),
            volumeExercises = "100 kg",
            isTitleTooLong = false,
            isTitleEmpty = false,
            updateWorkoutTitle = {},
            updateWorkoutNotes = {},
            saveExercisesWithWorkout = {},
            setTimeElapsed = {}
        )
    }
}