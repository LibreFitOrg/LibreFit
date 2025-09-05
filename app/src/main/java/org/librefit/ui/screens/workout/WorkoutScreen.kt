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

package org.librefit.ui.screens.workout

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedToggleButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.db.relations.WorkoutWithExercisesAndSets
import org.librefit.enums.InfoMode
import org.librefit.enums.SetMode
import org.librefit.nav.Route
import org.librefit.ui.components.ExerciseCard
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.DumbbellLottie
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import java.time.LocalDateTime

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.WorkoutScreen(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel: WorkoutScreenViewModel = hiltViewModel()


    LaunchedEffect(Unit) {
        //It adds the selected exercises from AddExerciseScreen
        sharedViewModel.getSelectedExercisesList().forEach(viewModel::addExerciseWithSets)
    }

    val timeElapsed by viewModel.timeElapsed.collectAsState()

    val isStopwatchPaused by viewModel.isStopwatchPaused.collectAsState()

    val exercisesWithSets by viewModel.exercises.collectAsState()

    val keepWorkoutScreenOn by viewModel.keepScreenOn.collectAsState()

    val workout by viewModel.workout.collectAsState()

    val restTime by viewModel.restTime.collectAsState()

    val idSetWithRunningStopwatch by viewModel.idSetWithRunningStopwatch.collectAsState()

    val workoutProgress by viewModel.workoutProgress.collectAsState()

    val previousPerformances by viewModel.previousPerformances.collectAsState()

    val restTimerProgress by viewModel.restTimerProgress.collectAsState()
    

    //It keeps the screen turned on
    if (keepWorkoutScreenOn) {
        val context = LocalContext.current

        DisposableEffect(key1 = Unit) {
            val window = (context as Activity).window

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            onDispose {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }



    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(R.string.quit_workout_question),
            text = stringResource(R.string.quit_workout_text),
            confirmText = stringResource(R.string.quit_dialog),
            onConfirm = {
                viewModel.stopWorkoutService()
                navController.navigateUp()
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }


    var infoMode by remember { mutableStateOf(InfoMode.DISMISS) }

    InfoModalBottomSheet(infoMode) { infoMode = InfoMode.DISMISS }


    BackHandler {
        if (!showConfirmDialog && exercisesWithSets.isNotEmpty()) {
            showConfirmDialog = true
        } else {
            viewModel.stopWorkoutService()
            navController.navigateUp()
        }
    }

    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.workout)),
        navigateBack = {
            if (exercisesWithSets.isEmpty()) {
                viewModel.stopWorkoutService()
                navController.navigateUp()
            } else {
                showConfirmDialog = true
            }
        },
        actions = listOf {
            navController.navigate(
                Route.BeforeSavingScreen(
                    WorkoutWithExercisesAndSets(
                        workout = workout.copy(
                            timeElapsed = timeElapsed,
                            completed = LocalDateTime.now()
                        ).toEntity(),
                        exercisesWithSets = exercisesWithSets.map { it.toEntity() },
                    )
                )
            ) { launchSingleTop = true }
        },
        actionsEnabled = listOf(!exercisesWithSets.isEmpty()),
        actionsDescription = listOf(stringResource(R.string.done)),
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            WorkoutScreenContent(
                animatedVisibilityScope = animatedVisibilityScope,
                exercisesWithSets = exercisesWithSets,
                previousPerformances = previousPerformances,
                idSetWithRunningStopwatch = idSetWithRunningStopwatch,
                timeElapsed = timeElapsed,
                isStopwatchPaused = isStopwatchPaused,
                workoutProgress = workoutProgress,
                toggleStopwatch = viewModel::toggleStopwatch,
                updateIdSetWithRunningStopwatch = viewModel::updateIdSetWithRunningStopwatch,
                onSelectedExerciseIdChange = { id, exercise ->
                    navController.navigate(
                        Route.InfoExerciseScreen(
                            id,
                            exercise.toEntity()
                        )
                    ) { launchSingleTop = true }
                },
                updateSetTime = viewModel::updateSetTime,
                updateSetReps = viewModel::updateSetReps,
                updateSetLoad = viewModel::updateSetLoad,
                updateSetCompleted = viewModel::updateSetCompleted,
                addSetToExercise = viewModel::addSetToExercise,
                deleteSet = viewModel::deleteSet,
                updateExerciseNotes = viewModel::updateExerciseNotes,
                updateExerciseRestTime = viewModel::updateExerciseRestTime,
                updateExerciseSetMode = viewModel::updateExerciseSetMode,
                deleteExercise = viewModel::deleteExercise,
                showInfo = { infoMode = it },
                applyPreviousSetPerformance = viewModel::applyPreviousSetPerformance
            )
            FloatingWorkoutActionBar(
                restTimerProgress = restTimerProgress,
                restTime = restTime,
                modifyRestTime = viewModel::modifyRestTime,
                fabAction = {
                    navController.navigate(Route.ExercisesScreen(addExercises = true)) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }




    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.updateFocus(isFocused = true)
                else -> viewModel.updateFocus(isFocused = false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharedTransitionScope.WorkoutScreenContent(
    animatedVisibilityScope: AnimatedVisibilityScope,
    exercisesWithSets: List<UiExerciseWithSets>,
    previousPerformances: List<List<String>?>,
    timeElapsed: Int,
    isStopwatchPaused: Boolean,
    workoutProgress: Pair<Int, Int>,
    idSetWithRunningStopwatch: Long?,
    toggleStopwatch: () -> Unit,
    updateIdSetWithRunningStopwatch: (Long?) -> Unit,
    addSetToExercise: (Long) -> Unit,
    updateSetTime: (Int, Long) -> Unit,
    updateSetReps: (Int, Long) -> Unit,
    updateSetLoad: (Double, Long) -> Unit,
    updateSetCompleted: (Boolean, Long) -> Unit,
    deleteSet: (Long) -> Unit,
    updateExerciseNotes: (String, Long) -> Unit,
    updateExerciseRestTime: (Int, Long) -> Unit,
    updateExerciseSetMode: (SetMode, Long) -> Unit,
    deleteExercise: (Long) -> Unit,
    onSelectedExerciseIdChange: (Long, UiExerciseDC) -> Unit,
    showInfo: (InfoMode) -> Unit,
    applyPreviousSetPerformance: (Long) -> Unit
) {
    LibreFitLazyColumn {
        stickyHeader {
            ElevatedCard(shape = MaterialTheme.shapes.extraLargeIncreased) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val animatedProgress = animateFloatAsState(
                        targetValue = workoutProgress.let { p ->
                            p.first.toFloat() / (p.second.takeUnless { it == 0 }
                                ?: 1) // Avoid 0 division
                        },
                        animationSpec = WavyProgressIndicatorDefaults.ProgressAnimationSpec,
                        label = "AnimatedProgressForWavyIndicator"
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.completed_sets) + ": ${workoutProgress.first}/${workoutProgress.second}",
                        )
                        LinearWavyProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            progress = { animatedProgress.value },
                        )
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        //Play/Pause button
                        ElevatedToggleButton(
                            checked = !isStopwatchPaused,
                            onCheckedChange = { toggleStopwatch() },
                            shapes = ToggleButtonDefaults.shapes()
                        ) {
                            Icon(
                                painter = painterResource(if (isStopwatchPaused) R.drawable.ic_play_arrow else R.drawable.ic_pause),
                                contentDescription = stringResource(if (isStopwatchPaused) R.string.pause else R.string.resume),
                            )
                        }

                        Text(
                            text = stringResource(R.string.elapsed_time) + ": " + Formatter.formatTime(
                                timeElapsed
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

            }
        }
        if (exercisesWithSets.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DumbbellLottie()
                    Text(
                        text = stringResource(id = R.string.add_to_empty_workout),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            itemsIndexed(
                items = exercisesWithSets,
                key = { i, exercise -> exercise.exercise.id }
            ) { i, exerciseWithSets ->
                ExerciseCard(
                    modifier = Modifier.animateItem(),
                    animatedVisibilityScope = animatedVisibilityScope,
                    exerciseWithSets = exerciseWithSets,
                    previousPerformances = previousPerformances.getOrNull(i),
                    idSetWithRunningStopwatch = idSetWithRunningStopwatch,
                    workout = true,
                    addSet = addSetToExercise,
                    onDetail = onSelectedExerciseIdChange,
                    onDelete = deleteExercise,
                    deleteSet = deleteSet,
                    showInfo = showInfo,
                    updateIdSetWithRunningStopwatch = updateIdSetWithRunningStopwatch,
                    updateExerciseNotes = updateExerciseNotes,
                    updateExerciseRestTime = updateExerciseRestTime,
                    updateExerciseSetMode = updateExerciseSetMode,
                    updateSetTime = updateSetTime,
                    updateSetReps = updateSetReps,
                    updateSetLoad = updateSetLoad,
                    updateSetCompleted = updateSetCompleted,
                    applyPreviousSetPerformance = applyPreviousSetPerformance
                )
            }
        }
    }

}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BoxScope.FloatingWorkoutActionBar(
    restTimerProgress: Float,
    restTime: Int,
    modifyRestTime: (Boolean) -> Unit,
    fabAction: () -> Unit
) {
    HorizontalFloatingToolbar(
        expanded = restTime != 0,
        floatingActionButton = {
            FloatingToolbarDefaults.StandardFloatingActionButton(
                onClick = fabAction
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_exercise)
                )
            }
        },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(y = -ScreenOffset, x = -ScreenOffset),
    ) {
        val animatedTimerProgress = animateFloatAsState(
            targetValue = restTimerProgress,
            animationSpec = WavyProgressIndicatorDefaults.ProgressAnimationSpec,
            label = "progressTimerAnimation"
        )
        //Rest timer
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconSize = remember { 48.dp }
            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = stringResource(R.string.rest)
            )
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(iconSize),
                    progress = { animatedTimerProgress.value },
                )
                Text("$restTime")
            }

            val interactionSources = remember { List(2) { MutableInteractionSource() } }
            ButtonGroup(
                overflowIndicator = {}
            ) {
                customItem(
                    buttonGroupContent = {
                        IconButton(
                            modifier = Modifier.animateWidth(interactionSources[0]),
                            onClick = { modifyRestTime(false) },
                            enabled = restTime > 0,
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = interactionSources[0]
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_replay_10),
                                contentDescription = stringResource(R.string.add_ten_seconds),
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    },
                    menuContent = {}
                )
                customItem(
                    buttonGroupContent = {
                        IconButton(
                            modifier = Modifier.animateWidth(interactionSources[1]),
                            onClick = { modifyRestTime(true) },
                            enabled = restTime > 0,
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = interactionSources[1]
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_forward_10),
                                contentDescription = stringResource(R.string.reduce_ten_seconds),
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    },
                    menuContent = {}
                )
            }


        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun WorkoutScreenPreview() {
    val e = listOf(
        UiExerciseWithSets(
            exerciseDC = UiExerciseDC(
                name = "3/4 Sit-Up",
                images = persistentListOf("3_4_Sit-Up/0.jpg")
            )
        )
    )
    val workoutProgress = e.sumOf { it.sets.count { s -> s.completed } } to e.sumOf { it.sets.size }
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                LibreFitScaffold(
                    title = AnnotatedString(stringResource(R.string.workout)),
                    navigateBack = {},
                    actions = listOf {},
                    actionsEnabled = listOf(!e.isEmpty()),
                    actionsDescription = listOf(stringResource(R.string.done)),
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        WorkoutScreenContent(
                            animatedVisibilityScope = this@AnimatedVisibility,
                            exercisesWithSets = e,
                            previousPerformances = emptyList(),
                            idSetWithRunningStopwatch = null,
                            updateIdSetWithRunningStopwatch = {},
                            timeElapsed = 0,
                            isStopwatchPaused = false,
                            workoutProgress = workoutProgress,
                            toggleStopwatch = {},
                            addSetToExercise = {},
                            updateSetTime = { _, _ -> },
                            updateSetReps = { _, _ -> },
                            updateSetLoad = { _, _ -> },
                            updateSetCompleted = { _, _ -> },
                            deleteSet = {},
                            updateExerciseNotes = { _, _ -> },
                            updateExerciseRestTime = { _, _ -> },
                            updateExerciseSetMode = { _, _ -> },
                            deleteExercise = {},
                            onSelectedExerciseIdChange = { _, _ -> },
                            showInfo = {},
                            applyPreviousSetPerformance = {}
                        )
                        FloatingWorkoutActionBar(
                            restTimerProgress = 0f,
                            restTime = 300,
                            modifyRestTime = {},
                            fabAction = {}
                        )
                    }
                }
            }
        }
    }

}