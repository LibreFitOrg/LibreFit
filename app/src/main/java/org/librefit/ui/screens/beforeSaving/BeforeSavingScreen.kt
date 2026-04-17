/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.beforeSaving

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.components.modalBottomSheets.InputModalBottomSheet
import org.librefit.ui.models.InputModalBottomSheetState
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import org.librefit.util.Formatter.formatTime
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.BeforeSavingScreen(
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel: BeforeSavingScreenViewModel = hiltViewModel()

    val volume by viewModel.volume.collectAsStateWithLifecycle()

    val workout by viewModel.workout.collectAsStateWithLifecycle()

    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    val routine by viewModel.routine.collectAsStateWithLifecycle()

    val useNumberPicker by viewModel.useNumberPicker.collectAsStateWithLifecycle()


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

    var inputModalBottomSheetState by remember { mutableStateOf<InputModalBottomSheetState?>(null) }

    inputModalBottomSheetState?.let {
        InputModalBottomSheet(
            state = it,
            onValueChange = { newState ->
                if (newState is InputModalBottomSheetState.HoursMinutesSeconds) {
                    viewModel.setTimeElapsed(newState.totalSeconds)
                }
            },
            onDismiss = {
                inputModalBottomSheetState = null
            }
        )
    }


    BeforeSavingScreenContent(
        navController = navController,
        showUnlikeRoutineDialog = { showUnlikeRoutineDialog.value = true },
        showDatePickerDialog = { showDatePickerDialog.value = true },
        exercises = exercises,
        workout = workout,
        routine = routine,
        volumeExercises = volume,
        animatedVisibilityScope = animatedVisibilityScope,
        useNumberPicker = useNumberPicker,
        isTitleTooLong = viewModel.isTitleTooLong(),
        isTitleEmpty = viewModel.isTitleEmpty(),
        updateWorkoutTitle = viewModel::updateWorkoutTitle,
        updateWorkoutNotes = viewModel::updateWorkoutNotes,
        saveExercisesWithWorkout = viewModel::saveExercisesWithWorkout,
        setTimeElapsed = viewModel::setTimeElapsed,
        onInputModalBottomSheetRequest = {
            inputModalBottomSheetState = it
        }
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.BeforeSavingScreenContent(
    navController: NavHostController,
    showUnlikeRoutineDialog: () -> Unit,
    showDatePickerDialog: () -> Unit,
    exercises: List<UiExerciseWithSets>,
    workout: UiWorkout,
    routine: UiWorkout,
    volumeExercises: String,
    isTitleTooLong: Boolean,
    isTitleEmpty: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
    useNumberPicker: Boolean,
    updateWorkoutTitle: (String) -> Unit,
    updateWorkoutNotes: (String) -> Unit,
    saveExercisesWithWorkout: () -> Unit,
    setTimeElapsed: (Int) -> Unit,
    onInputModalBottomSheetRequest: (InputModalBottomSheetState) -> Unit,
) {
    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.overview)),
        navigateBack = navController::navigateUp,
        actions = listOf {
            saveExercisesWithWorkout()
            navController.navigate(Route.SuccessScreen(SuccessMessage.WORKOUT_SAVED)) {
                launchSingleTop = true
                popUpTo(Route.MainScreen) { inclusive = false }
            }
        },
        actionsDescription = listOf(stringResource(R.string.save)),
        actionsEnabled = listOf(!isTitleEmpty && !isTitleTooLong)
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding) {
            item {
                OutlinedTextField(
                    shape = MaterialTheme.shapes.large,
                    value = workout.title,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    onValueChange = updateWorkoutTitle,
                    trailingIcon = {
                        if (isTitleEmpty || isTitleTooLong) {
                            Icon(
                                painter = painterResource(R.drawable.ic_warning),
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
                    shape = MaterialTheme.shapes.large,
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
                    Box(
                        modifier = Modifier.weight(0.5f),
                    ) {
                        OutlinedTextField(
                            readOnly = useNumberPicker,
                            shape = MaterialTheme.shapes.large,
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
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(top = 7.dp) // Thin offset to match inner shape
                                .clip(MaterialTheme.shapes.largeIncreased)
                                .clickable(enabled = useNumberPicker) {
                                    onInputModalBottomSheetRequest(
                                        workout.timeElapsed.seconds.toComponents { hours, minutes, seconds, _ ->
                                            InputModalBottomSheetState.HoursMinutesSeconds(
                                                hours = hours.toInt(),
                                                minutes = minutes,
                                                seconds = seconds
                                            )
                                        }
                                    )
                                }
                        ) { }
                    }
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.weight(0.5f),
                        value = Formatter.getShortDateFromLocalDate(workout.completed),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.label_when)) },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = showDatePickerDialog) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_date_range),
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
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.weight(0.5f),
                        value = volumeExercises,
                        label = { Text(stringResource(R.string.volume)) },
                        suffix = { Text(stringResource(R.string.kg)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.large,
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
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.weight(0.5f),
                        value = "${exercises.sumOf { it.sets.size }}",
                        label = { Text(stringResource(R.string.total_sets)) },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                    )
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.large,
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

            if (routine.title != "") {
                item {
                    HeadlineText(stringResource(R.string.linked_routine))
                }
                item {
                    ElevatedCard(
                        shape = MaterialTheme.shapes.extraLargeIncreased,
                        onClick = {
                            navController.navigate(Route.InfoWorkoutScreen(routine.id)) {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(routine.id),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
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
                                        modifier = Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(
                                                routine.id.toString() + routine.title
                                            ),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        )
                                    )
                                    Text(
                                        text = stringResource(R.string.creation_date) + ": " +
                                                Formatter.getLongDateFromLocalDate(routine.created),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                IconButton(
                                    onClick = showUnlikeRoutineDialog
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
                                navController.navigate(Route.InfoWorkoutScreen(routine.id)) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }
            }


            item {
                HeadlineText(stringResource(R.string.exercises))
            }

            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLargeIncreased) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
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
                                Text("${exercise.sets.count { s -> s.completed }} / ${exercise.sets.count()}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(device = "id:medium_phone")
@Composable
private fun BeforeSavingScreenPreview() {
    val e = listOf(
        UiExerciseWithSets(),
        UiExerciseWithSets(),
        UiExerciseWithSets(),
        UiExerciseWithSets(),
        UiExerciseWithSets(
            exercise = UiExercise(setMode = SetMode.DURATION, restTime = 0, notes="Easy pace just to warm up"),
            exerciseDC = UiExerciseDC(
                name = "Running, Treadmill",
                images = persistentListOf("Running_Treadmill/0.webp"),
                equipment = Equipment.OTHER,
                category = Category.CARDIO
            ),
            sets = persistentListOf(
                UiSet(elapsedTime = 605, completed = true))
        ),
        UiExerciseWithSets(
            exercise = UiExercise(
                setMode = SetMode.LOAD,
                restTime = 120,
            ),
            exerciseDC = UiExerciseDC(
                name = "Barbell Bench Press - Medium Grip",
                images = persistentListOf("Barbell_Bench_Press_-_Medium_Grip/0.webp"),
                equipment = Equipment.MACHINE,
                category = Category.STRENGTH
            ),
            sets = persistentListOf(
                UiSet(load = 80.0, reps = 8, completed = true),
                UiSet(load = 80.0, reps = 9, completed = true),
                UiSet(load = 80.0, reps = 9, completed = true),
                UiSet(load = 50.0, reps = 8, completed = true),
                UiSet(load = 50.0, reps = 9, completed = true),
                UiSet(load = 50.0, reps = 9, completed = true),
                UiSet(load = 50.0, reps = 8, completed = true),
                UiSet(load = 50.0, reps = 9, completed = true),
                UiSet(load = 50.0, reps = 9, completed = true),
                UiSet(load = 50.0, reps = 8, completed = true),
                UiSet(load = 50.0, reps = 9, completed = true),
                UiSet(load = 50.0, reps = 9, completed = true),
                UiSet(load = 50.0, reps = 8, completed = true),
                UiSet(load = 50.0, reps = 9, completed = true),
                UiSet(load = 50.0, reps = 9, completed = true),
            )
        ),
        UiExerciseWithSets(
            exercise = UiExercise(
                setMode = SetMode.BODYWEIGHT,
                restTime = 120,
            ),
            exerciseDC = UiExerciseDC(
                name = "Pushups",
                images = persistentListOf("Pushups/0.webp"),
                equipment = Equipment.BODY_ONLY,
                category = Category.STRENGTH
            ),
            sets = persistentListOf(
                UiSet(reps = 9, completed = true),
                UiSet(reps = 8, completed = true),
                UiSet(reps = 9, completed = true),
            )
        ),
        UiExerciseWithSets(
            exercise = UiExercise(
                setMode = SetMode.DURATION,
                restTime = 120,
            ),
            exerciseDC = UiExerciseDC(
                name = "Chest And Front Of Shoulder Stretch",
                images = persistentListOf("Chest_And_Front_Of_Shoulder_Stretch/0.webp"),
                equipment = Equipment.BODY_ONLY,
                category = Category.STRETCHING
            ),
            sets = persistentListOf(
                UiSet(elapsedTime = 127, completed = true),
            )
        )
    )

    val volume = e.sumOf { eWs -> eWs.sets.sumOf { it.load * it.reps } }

    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                BeforeSavingScreenContent(
                    navController = rememberNavController(),
                    showUnlikeRoutineDialog = {},
                    showDatePickerDialog = {},
                    exercises = e,
                    workout = UiWorkout(
                        title = "\uD83C\uDFCB Upper body",
                        notes = "Feeling well today",
                        timeElapsed = 3689
                    ),
                    routine = UiWorkout(title = "\uD83C\uDFCB Upper body"),
                    volumeExercises = "$volume",
                    isTitleTooLong = false,
                    isTitleEmpty = false,
                    useNumberPicker = true,
                    updateWorkoutTitle = {},
                    updateWorkoutNotes = {},
                    saveExercisesWithWorkout = {},
                    setTimeElapsed = {},
                    animatedVisibilityScope = this,
                    onInputModalBottomSheetRequest = {}
                )
            }
        }
    }
}