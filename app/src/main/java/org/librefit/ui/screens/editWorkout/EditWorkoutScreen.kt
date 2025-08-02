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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.db.entity.Workout
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.enums.InfoMode
import org.librefit.enums.SetMode
import org.librefit.enums.SuccessMessage
import org.librefit.nav.Route
import org.librefit.ui.components.ExerciseCard
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.DumbbellLottie
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.components.modalBottomSheets.ExerciseDetailModalBottomSheet
import org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.theme.LibreFitTheme

@Composable
fun EditWorkoutScreen(
    sharedViewModel: SharedViewModel,
    navController: NavHostController
) {
    val viewModel: EditWorkoutScreenViewModel = hiltViewModel()

    val workout by viewModel.workout.collectAsState()

    val exercises by viewModel.exercises.collectAsState()

    LaunchedEffect(Unit) {
        sharedViewModel.getSelectedExercisesList().forEach(viewModel::addExerciseWithSets)
    }

    EditWorkoutScreenContent(
        navController = navController,
        typeOfEdit = viewModel.getTypeOfEdit(),
        exercisesWithSets = exercises,
        workout = workout,
        isTitleTooLong = viewModel.isTitleTooLong(),
        isTitleEmpty = viewModel.isTitleEmpty(),
        updateTitle = viewModel::updateTitle,
        updateNotes = viewModel::updateNotes,
        updateSetTime = viewModel::updateSetTime,
        updateSetReps = viewModel::updateSetReps,
        updateSetLoad = viewModel::updateSetLoad,
        updateSetCompleted = viewModel::updateSetCompleted,
        deleteSet = viewModel::deleteSet,
        addSetToExercise = viewModel::addSetToExercise,
        deleteExercise = viewModel::deleteExercise,
        updateExerciseNotes = viewModel::updateExerciseNotes,
        updateExerciseRestTime = viewModel::updateExerciseRestTime,
        updateExerciseSetMode = viewModel::updateExerciseSetMode,
        saveWorkoutWithExercisesInDB = viewModel::saveWorkoutWithExercisesInDB,
    )

}

@Composable
private fun EditWorkoutScreenContent(
    navController: NavHostController,
    typeOfEdit: Boolean?,
    exercisesWithSets: List<UiExerciseWithSets>,
    workout: Workout,
    isTitleTooLong: Boolean,
    isTitleEmpty: Boolean,
    updateTitle: (String) -> Unit,
    updateNotes: (String) -> Unit,
    deleteSet: (Long) -> Unit,
    updateSetTime: (Int, Long) -> Unit,
    updateSetReps: (Int, Long) -> Unit,
    updateSetLoad: (Float, Long) -> Unit,
    updateSetCompleted: (Boolean, Long) -> Unit,
    addSetToExercise: (Long) -> Unit,
    deleteExercise: (Long) -> Unit,
    updateExerciseNotes: (String, Long) -> Unit,
    updateExerciseRestTime: (Int, Long) -> Unit,
    updateExerciseSetMode: (SetMode, Long) -> Unit,
    saveWorkoutWithExercisesInDB: () -> Unit
) {

    var showConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !showConfirmDialog && exercisesWithSets.isNotEmpty()) {
        showConfirmDialog = true
    }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(
                if (typeOfEdit == null) R.string.quit_routine_creation_question
                else R.string.discard_changes_question
            ),
            text = stringResource(
                if (typeOfEdit == null) R.string.exit_create_routine else R.string.discard_changes_text
            ),
            confirmText = stringResource(
                if (typeOfEdit == null) R.string.quit_dialog else R.string.discard_dialog
            ),
            onConfirm = {
                navController.popBackStack()
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    /**
     * Used to display information about the selected exercise in [ExerciseDetailModalBottomSheet]
     */
    var selectedExerciseId by remember { mutableStateOf<String?>(null) }
    val onSelectedExerciseIdChange = remember {
        { newId: String ->
            selectedExerciseId = newId
        }
    }

    if (selectedExerciseId != null) {
        ExerciseDetailModalBottomSheet(
            exercise = exercisesWithSets.map { it.exerciseDC }
                .find { it.id == selectedExerciseId }!!
        ) { selectedExerciseId = null }
    }


    var infoMode by remember { mutableStateOf(InfoMode.DISMISS) }
    val onInfoModeChange = remember {
        { newValue: InfoMode ->
            infoMode = newValue
        }
    }

    InfoModalBottomSheet(infoMode) { infoMode = InfoMode.DISMISS }

    LibreFitScaffold(
        title = AnnotatedString(
            when (typeOfEdit) {
                null -> stringResource(R.string.create_routine)
                true -> stringResource(R.string.edit_routine)
                false -> stringResource(R.string.edit_workout)
            }
        ),
        navigateBack = {
            if (exercisesWithSets.isEmpty()) {
                navController.popBackStack()
            } else {
                showConfirmDialog = true
            }
        },
        actions = listOf {
            if (typeOfEdit == false) {
                navController.navigate(
                    Route.BeforeSavingScreen(
                        WorkoutWithExercisesAndSets(
                            workout = workout,
                            exercisesWithSets = exercisesWithSets.map { it.toEntity() }
                        )
                    )
                )
            } else {
                saveWorkoutWithExercisesInDB()
                navController.navigate(Route.SuccessScreen(SuccessMessage.ROUTINE_SAVED)) {
                    popUpTo(Route.MainScreen) { inclusive = false }
                }
            }
        },
        actionsDescription = listOf(
            if (typeOfEdit == false) stringResource(R.string.done)
            else stringResource(R.string.save)
        ),
        actionsEnabled = listOf(!isTitleEmpty && !isTitleTooLong && exercisesWithSets.isNotEmpty()),
        fabIcon = ImageVector.vectorResource(R.drawable.ic_add),
        fabAction = {
            navController.navigate(Route.ExercisesScreen(addExercises = true))
        },
        fabDescription = stringResource(R.string.add_exercise)
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding) {
            item {
                OutlinedTextField(
                    value = workout.title,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    onValueChange = { newTitle ->
                        updateTitle(newTitle)
                    },
                    trailingIcon = {
                        if (isTitleTooLong || isTitleEmpty) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_warning),
                                contentDescription = stringResource(R.string.warning)
                            )
                        }
                    },
                    isError = isTitleTooLong || isTitleEmpty,
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
                    onValueChange = { newNotes ->
                        updateNotes(newNotes)
                    },
                    label = { Text(text = stringResource(id = R.string.notes)) },
                )
            }
            item {
                HorizontalDivider()
            }
            if (exercisesWithSets.isEmpty()) {
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
                    items = exercisesWithSets,
                    key = { i, e -> e.exercise.id }
                ) { i, exerciseWithSets ->
                    ExerciseCard(
                        modifier = Modifier.animateItem(),
                        exerciseWithSets = exerciseWithSets,
                        workout = typeOfEdit == false,
                        addSet = addSetToExercise,
                        onDetail = onSelectedExerciseIdChange,
                        onDelete = deleteExercise,
                        deleteSet = deleteSet,
                        updateExerciseNotes = updateExerciseNotes,
                        updateExerciseRestTime = updateExerciseRestTime,
                        updateExerciseSetMode = updateExerciseSetMode,
                        showInfo = onInfoModeChange,
                        updateSetTime = updateSetTime,
                        updateSetReps = updateSetReps,
                        updateSetLoad = updateSetLoad,
                        updateSetCompleted = updateSetCompleted
                    )
                }
            }
            bottomMargin()
        }
    }
}

@Preview
@Composable
private fun EditWorkoutScreenPreview() {
    /**
     * Returns `null` when a new routine is created, `true` when a routine is edited and `false` when
     * a past workout is edited
     */
    val typeOfEdit = false

    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        EditWorkoutScreenContent(
            navController = rememberNavController(),
            typeOfEdit = typeOfEdit,
            exercisesWithSets = persistentListOf(
                UiExerciseWithSets(
                    exercise = UiExercise(restTime = 90, setMode = SetMode.BODYWEIGHT),
                    exerciseDC = UiExerciseDC(name = "Name exercise"),
                    sets = persistentListOf(UiSet(), UiSet(completed = true))
                )
            ),
            workout = Workout(title = "Title workout", notes = "This is a note"),
            isTitleTooLong = false,
            isTitleEmpty = false,
            updateTitle = { _ -> },
            updateNotes = { _ -> },
            addSetToExercise = { _ -> },
            deleteExercise = { _ -> },
            deleteSet = { _ -> },
            saveWorkoutWithExercisesInDB = { },
            updateExerciseNotes = { _, _ -> },
            updateExerciseRestTime = { _, _ -> },
            updateExerciseSetMode = { _, _ -> },
            updateSetTime = { _, _ -> },
            updateSetReps = { _, _ -> },
            updateSetLoad = { _, _ -> },
            updateSetCompleted = { _, _ -> }
        )
    }
}