/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.editWorkout

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.enums.SetMode
import org.librefit.enums.SuccessMessage
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.nav.Route
import org.librefit.ui.components.ExerciseCard
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.DumbbellLottie
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.theme.LibreFitTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.EditWorkoutScreen(
    sharedViewModel: SharedViewModel,
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel: EditWorkoutScreenViewModel = hiltViewModel()

    val workout by viewModel.workout.collectAsStateWithLifecycle()

    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        sharedViewModel.getSelectedExercisesList().forEach(viewModel::addExerciseWithSets)
    }

    val idExerciseToDelete = rememberSaveable { mutableStateOf<Long?>(null) }

    idExerciseToDelete.value?.let {
        ConfirmDialog(
            title = stringResource(R.string.delete_exercise_question),
            text = stringResource(if (viewModel.getTypeOfEdit() == true) R.string.warning_delete_exercise_from_routine else R.string.confirm_delete),
            confirmText = stringResource(R.string.delete),
            onConfirm = {
                viewModel.deleteExercise(it)
                idExerciseToDelete.value = null
            },
            onDismiss = {
                idExerciseToDelete.value = null
            }
        )
    }

    EditWorkoutScreenContent(
        navController = navController,
        animatedVisibilityScope = animatedVisibilityScope,
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
        deleteExercise = { id ->
            idExerciseToDelete.value = id
        },
        updateExerciseNotes = viewModel::updateExerciseNotes,
        updateExerciseRestTime = viewModel::updateExerciseRestTime,
        updateExerciseSetMode = viewModel::updateExerciseSetMode,
        moveExercise = viewModel::moveExercise,
        saveWorkoutWithExercisesInDB = viewModel::saveWorkoutWithExercisesInDB,
    )

}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.EditWorkoutScreenContent(
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    typeOfEdit: Boolean?,
    exercisesWithSets: List<UiExerciseWithSets>,
    workout: UiWorkout,
    isTitleTooLong: Boolean,
    isTitleEmpty: Boolean,
    updateTitle: (String) -> Unit,
    updateNotes: (String) -> Unit,
    deleteSet: (Long) -> Unit,
    updateSetTime: (Int, Long) -> Unit,
    updateSetReps: (Int, Long) -> Unit,
    updateSetLoad: (Double, Long) -> Unit,
    updateSetCompleted: (Boolean, Long) -> Unit,
    addSetToExercise: (Long) -> Unit,
    deleteExercise: (Long) -> Unit,
    updateExerciseNotes: (String, Long) -> Unit,
    updateExerciseRestTime: (Int, Long) -> Unit,
    updateExerciseSetMode: (SetMode, Long) -> Unit,
    moveExercise: (Int, Int) -> Unit,
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
                navController.navigateUp()
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }


    var infoMode by remember { mutableStateOf(InfoMode.DISMISS) }
    val onInfoModeChange = remember {
        { newValue: InfoMode ->
            infoMode = newValue
        }
    }

    InfoModalBottomSheet(infoMode) { infoMode = InfoMode.DISMISS }

    val lazyListState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current
    // We track the exercise id so a release event from one card can't clear the collapse state while another card is still being pressed or dragged.
    var pressedExerciseId by remember { mutableStateOf<Long?>(null) }
    var draggingExerciseId by remember { mutableStateOf<Long?>(null) }
    val isAnyExerciseCollapsed = pressedExerciseId != null || draggingExerciseId != null
    val exerciseSectionStartIndex = 3
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromExerciseIndex = from.index - exerciseSectionStartIndex
        val toExerciseIndex = (to.index - exerciseSectionStartIndex)
            .coerceIn(0, exercisesWithSets.lastIndex)

        if (fromExerciseIndex in exercisesWithSets.indices && toExerciseIndex in exercisesWithSets.indices) {
            moveExercise(fromExerciseIndex, toExerciseIndex)
        }
    }

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
                navController.navigateUp()
            } else {
                showConfirmDialog = true
            }
        },
        actions = listOf {
            if (typeOfEdit == false) {
                navController.navigate(
                    Route.BeforeSavingScreen(
                        workout.id
                    )
                ) { launchSingleTop = true }
            } else {
                saveWorkoutWithExercisesInDB()
                navController.navigate(Route.SuccessScreen(SuccessMessage.ROUTINE_SAVED)) {
                    launchSingleTop = true
                    popUpTo(Route.MainScreen) { inclusive = false }
                }
            }
        },
        actionsDescription = listOf(
            if (typeOfEdit == false) stringResource(R.string.done)
            else stringResource(R.string.save)
        ),
        actionsEnabled = listOf(!isTitleEmpty && !isTitleTooLong && exercisesWithSets.isNotEmpty()),
        fabIcon = painterResource(R.drawable.ic_add),
        fabAction = {
            navController.navigate(Route.ExercisesScreen(addExercises = true)) {
                launchSingleTop = true
            }
        },
        fabDescription = stringResource(R.string.add_exercise),
        fabText = stringResource(R.string.add_exercise),
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding, lazyListState = lazyListState) {
            item {
                OutlinedTextField(
                    shape = MaterialTheme.shapes.large,
                    value = workout.title,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    onValueChange = { newTitle ->
                        updateTitle(newTitle)
                    },
                    trailingIcon = {
                        if (isTitleTooLong || isTitleEmpty) {
                            Icon(
                                painter = painterResource(R.drawable.ic_warning),
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
                    shape = MaterialTheme.shapes.large,
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
                    key = { _, e -> e.exercise.id }
                ) { _, exerciseWithSets ->
                    ReorderableItem(reorderableLazyListState, key = exerciseWithSets.exercise.id) { isDragging ->
                        val exerciseId = exerciseWithSets.exercise.id
                        ExerciseCard(
                            modifier = Modifier.animateItem(),
                            animatedVisibilityScope = animatedVisibilityScope,
                            exerciseWithSets = exerciseWithSets,
                            workout = typeOfEdit == false,
                            addSet = addSetToExercise,
                            onDetail = { id, idExerciseDC ->
                                navController.navigate(
                                    Route.InfoExerciseScreen(
                                        id,
                                        idExerciseDC
                                )
                            ) { launchSingleTop = true }
                            },
                            onDelete = deleteExercise,
                            isCollapsed = isAnyExerciseCollapsed || isDragging,
                            showDragHandle = true,
                            onDragHandlePressedChange = { isPressed ->
                                if (isPressed) {
                                    pressedExerciseId = exerciseId
                                } else if (pressedExerciseId == exerciseId) {
                                    pressedExerciseId = null
                                }
                            },
                            dragHandleModifier = Modifier.draggableHandle(
                                onDragStarted = {
                                    draggingExerciseId = exerciseId
                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.GestureThresholdActivate
                                    )
                                },
                                onDragStopped = {
                                    if (draggingExerciseId == exerciseId) {
                                        draggingExerciseId = null
                                    }
                                    if (pressedExerciseId == exerciseId) {
                                        pressedExerciseId = null
                                    }
                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.GestureEnd
                                    )
                                }
                            ),
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
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(device = "id:medium_phone")
@Composable
private fun EditWorkoutScreenPreview() {
    /**
     * Returns `null` when a new routine is created, `true` when a routine is edited and `false` when
     * a past workout is edited
     */
    val typeOfEdit = null

    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                EditWorkoutScreenContent(
                    navController = rememberNavController(),
                    animatedVisibilityScope = this,
                    typeOfEdit = typeOfEdit,
                    exercisesWithSets = persistentListOf(
                        UiExerciseWithSets(
                            exercise = UiExercise(setMode = SetMode.DURATION, restTime = 0),
                            exerciseDC = UiExerciseDC(
                                name = "Running, Treadmill",
                                images = persistentListOf("Running_Treadmill/0.webp"),
                                equipment = Equipment.OTHER,
                                category = Category.CARDIO
                            ),
                            sets = persistentListOf(UiSet(elapsedTime = 600))
                        ),
                        UiExerciseWithSets(
                            exercise = UiExercise(setMode = SetMode.LOAD, restTime = 120),
                            exerciseDC = UiExerciseDC(
                                name = "Barbell Bench Press - Medium Grip",
                                images = persistentListOf("Barbell_Bench_Press_-_Medium_Grip/0.webp"),
                                equipment = Equipment.MACHINE,
                                category = Category.STRENGTH
                            ),
                            sets = persistentListOf(
                                UiSet(load = 80.0, reps = 8),
                                UiSet(load = 80.0, reps = 8),
                                UiSet(load = 80.0, reps = 9)
                            )
                        )
                    ),
                    workout = UiWorkout(title = "\uD83C\uDFCB Upper body"),
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
                    moveExercise = { _, _ -> },
                    updateSetTime = { _, _ -> },
                    updateSetReps = { _, _ -> },
                    updateSetLoad = { _, _ -> },
                    updateSetCompleted = { _, _ -> }
                )
            }
        }
    }
}
