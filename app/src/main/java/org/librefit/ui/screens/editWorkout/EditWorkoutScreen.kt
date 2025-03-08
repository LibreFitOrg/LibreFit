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

package org.librefit.ui.screens.editWorkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.data.ExerciseDC
import org.librefit.data.ExerciseWithSets
import org.librefit.db.Exercise
import org.librefit.enums.InfoMode
import org.librefit.enums.SuccessMessage
import org.librefit.nav.Destination
import org.librefit.ui.components.ConfirmDialog
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.ExerciseCard
import org.librefit.ui.components.animations.DumbbellLottie
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.modalBottomSheets.ExerciseDetailModalBottomSheet
import org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet
import org.librefit.ui.screens.shared.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutScreen(
    sharedViewModel: SharedViewModel,
    navController: NavHostController
) {
    val viewModel: EditWorkoutScreenViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        sharedViewModel.getSelectedExercisesList().forEach { exerciseDC ->
            viewModel.addExerciseWithSets(
                ExerciseWithSets(Exercise(exerciseId = exerciseDC.id), exerciseDC = exerciseDC)
            )
        }
        viewModel.initialize(
            workout = sharedViewModel.getPassedWorkout(),
            newExercises = sharedViewModel.getPassedExercises(),
            routine = sharedViewModel.getPassedRoutine()
        )
    }

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !showExitDialog && !viewModel.isListEmpty()) {
        showExitDialog = true
    }

    if (showExitDialog) {
        ConfirmDialog(
            title = stringResource(R.string.exit),
            text = if (viewModel.getTypeOfEdit() == false) stringResource(id = R.string.exit_workout)
            else stringResource(id = R.string.exit_create_routine),
            onConfirm = {
                navController.popBackStack()
                showExitDialog = false
            },
            onDismiss = { showExitDialog = false }
        )
    }

    CustomScaffold(
        title = AnnotatedString(
            when (viewModel.getTypeOfEdit()) {
                null -> stringResource(R.string.create_routine)
                true -> stringResource(R.string.edit_routine)
                false -> stringResource(R.string.edit_workout)
            }
        ),
        navigateBack = {
            if (viewModel.isListEmpty()) {
                navController.popBackStack()
            } else {
                showExitDialog = true
            }
        },
        actions = listOf {
            if (viewModel.getTypeOfEdit() == false) {
                sharedViewModel.setPassedData(
                    workout = viewModel.getWorkout(),
                    exercises = viewModel.getExercises(),
                )
                navController.navigate(Destination.BeforeSavingScreen)
            } else {
                viewModel.saveWorkoutWithExercisesInDB()
                navController.navigate(Destination.SuccessScreen(SuccessMessage.ROUTINE_SAVED)) {
                    popUpTo(Destination.MainScreen) { inclusive = false }
                }
            }
        },
        actionsDescription = listOf(
            if (viewModel.getTypeOfEdit() == false) stringResource(R.string.done)
            else stringResource(R.string.save)
        ),
        actionsEnabled = listOf(viewModel.isTitleAllowed() && !viewModel.isListEmpty()),
        fabIcon = Icons.Default.Add,
        fabAction = {
            navController.navigate(Destination.ExercisesScreen(addExercises = true))
        },
        fabDescription = stringResource(R.string.add_exercise)
    ) { innerPadding ->
        EditWorkoutScreen(
            innerPadding = innerPadding,
            viewModel = viewModel
        )
    }
}

@Composable
private fun EditWorkoutScreen(
    innerPadding: PaddingValues,
    viewModel: EditWorkoutScreenViewModel
) {

    /**
     * Used to display information about the selected exercise in [ExerciseDetailModalBottomSheet]
     */
    var selectedExercise by remember { mutableStateOf<ExerciseDC>(ExerciseDC()) }
    var isModalSheetOpen by remember { mutableStateOf(false) }

    if (isModalSheetOpen) {
        ExerciseDetailModalBottomSheet(exercise = selectedExercise) { isModalSheetOpen = false }
    }


    var infoMode by remember { mutableStateOf(InfoMode.DISMISS) }

    if (infoMode != InfoMode.DISMISS) {
        InfoModalBottomSheet(infoMode) { infoMode = InfoMode.DISMISS }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = innerPadding)
            .padding(start = 15.dp, end = 15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            OutlinedTextField(
                value = viewModel.getTitle(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                onValueChange = { newTitle ->
                    viewModel.updateTitle(newTitle)
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
                value = viewModel.getNotes(),
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { newNotes ->
                    viewModel.updateNotes(newNotes)
                },
                label = { Text(text = stringResource(id = R.string.notes)) },
            )
        }
        item {
            HorizontalDivider()
        }
        if (viewModel.isListEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DumbbellLottie()
                    Text(
                        text = stringResource(id = R.string.start_adding_exercises),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            itemsIndexed(
                items = viewModel.exercisesWithSets,
                key = { i, e -> e.exercise.id }
            ) { i, exerciseWithSets ->
                ExerciseCard(
                    modifier = Modifier.animateItem(),
                    exerciseWithSets = exerciseWithSets,
                    addSet = {
                        viewModel.addSetToExercise(i)
                    },
                    onDetail = {
                        selectedExercise = exerciseWithSets.exerciseDC
                        isModalSheetOpen = true
                    },
                    onDelete = {
                        viewModel.deleteExercise(i)
                    },
                    updateSet = { set, value, mode ->
                        viewModel.updateSet(
                            index = i,
                            set = set,
                            value = value,
                            mode = mode
                        )
                    },
                    deleteSet = { set ->
                        viewModel.deleteSet(
                            index = i,
                            set = set
                        )
                    },
                    updateExercise = { value, mode ->
                        viewModel.updateExercise(
                            index = i,
                            value = value,
                            mode = mode
                        )
                    },
                    showInfo = { infoMode = it },
                    workout = viewModel.getTypeOfEdit() == false
                )
            }
        }
        bottomMargin()
    }
}

@Preview
@Composable
private fun CreateRoutineScreenPreview() {
    EditWorkoutScreen(viewModel(), rememberNavController())
}