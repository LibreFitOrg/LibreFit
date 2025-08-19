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

import android.annotation.SuppressLint
import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
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
import org.librefit.util.Formatter.formatTime
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

    val isChronometerPaused by viewModel.isChronometerPaused.collectAsState()

    val exercisesWithSets by viewModel.exercises.collectAsState()

    val keepWorkoutScreenOn by viewModel.keepScreenOn.collectAsState()

    val workout by viewModel.workout.collectAsState()

    val restTime by viewModel.restTime.collectAsState()

    val idSetWithRunningChronometer by viewModel.idSetWithRunningChronometer.collectAsState()

    val progress by viewModel.progress.collectAsState()
    

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




    WorkoutScreenContent(
        animatedVisibilityScope = animatedVisibilityScope,
        timeElapsed = timeElapsed,
        isChronometerPaused = isChronometerPaused,
        isListEmpty = exercisesWithSets.isEmpty(),
        exercisesWithSets = exercisesWithSets,
        progress = progress,
        timerProgress = viewModel.getRestTimeProgress(),
        idSetWithRunningChronometer = idSetWithRunningChronometer,
        restTime = restTime,
        updateIdSetWithRunningChronometer = viewModel::updateIdSetWithRunningChronometer,
        navigateBack = {
            if (exercisesWithSets.isEmpty()) {
                viewModel.stopWorkoutService()
                navController.navigateUp()
            } else {
                showConfirmDialog = true
            }
        },
        fabAction = {
            navController.navigate(Route.ExercisesScreen(addExercises = true)) {
                launchSingleTop = true
            }
        },
        action = {
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
        onSelectedExerciseIdChange = { id, exercise ->
            navController.navigate(
                Route.InfoExerciseScreen(
                    id,
                    exercise.toEntity()
                )
            ) { launchSingleTop = true }
        },
        startChronometer = viewModel::startChronometer,
        pauseChronometer = viewModel::pauseChronometer,
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
        modifyRestTime = viewModel::modifyRestTime
    )


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


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.WorkoutScreenContent(
    animatedVisibilityScope: AnimatedVisibilityScope,
    timeElapsed: Int,
    isChronometerPaused: Boolean,
    exercisesWithSets: List<UiExerciseWithSets>,
    progress: Float,
    isListEmpty: Boolean,
    timerProgress: Float,
    idSetWithRunningChronometer: Long?,
    restTime: Int,
    updateIdSetWithRunningChronometer: (Long?) -> Unit,
    navigateBack: () -> Unit,
    fabAction: () -> Unit,
    action: () -> Unit,
    startChronometer: () -> Unit,
    pauseChronometer: () -> Unit,
    addSetToExercise: (Long) -> Unit,
    updateSetTime: (Int, Long) -> Unit,
    updateSetReps: (Int, Long) -> Unit,
    updateSetLoad: (Float, Long) -> Unit,
    updateSetCompleted: (Boolean, Long) -> Unit,
    deleteSet: (Long) -> Unit,
    updateExerciseNotes: (String, Long) -> Unit,
    updateExerciseRestTime: (Int, Long) -> Unit,
    updateExerciseSetMode: (SetMode, Long) -> Unit,
    deleteExercise: (Long) -> Unit,
    onSelectedExerciseIdChange: (Long, UiExerciseDC) -> Unit,
    showInfo: (InfoMode) -> Unit,
    modifyRestTime: (Boolean) -> Unit
) {
    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.workout)),
        navigateBack = navigateBack,
        actions = listOf { action() },
        actionsEnabled = listOf(!isListEmpty),
        actionsDescription = listOf(stringResource(R.string.done)),
        fabIcon = ImageVector.vectorResource(R.drawable.ic_add),
        fabAction = fabAction,
        fabDescription = stringResource(R.string.add_exercise),
        bottomBar = {
            BottomAppBar {
                BottomAppBarContent(
                    timeElapsed = timeElapsed,
                    isChronometerPaused = isChronometerPaused,
                    progress = progress,
                    timerProgress = timerProgress,
                    restTime = restTime,
                    startChronometer = startChronometer,
                    pauseChronometer = pauseChronometer,
                    modifyRestTime = modifyRestTime
                )
            }
        }
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding) {
            if (isListEmpty) {
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
                        idSetWithRunningChronometer = idSetWithRunningChronometer,
                        workout = true,
                        addSet = addSetToExercise,
                        onDetail = onSelectedExerciseIdChange,
                        onDelete = deleteExercise,
                        deleteSet = deleteSet,
                        showInfo = showInfo,
                        updateIdSetWithRunningChronometer = updateIdSetWithRunningChronometer,
                        updateExerciseNotes = updateExerciseNotes,
                        updateExerciseRestTime = updateExerciseRestTime,
                        updateExerciseSetMode = updateExerciseSetMode,
                        updateSetTime = updateSetTime,
                        updateSetReps = updateSetReps,
                        updateSetLoad = updateSetLoad,
                        updateSetCompleted = updateSetCompleted,
                    )
                }
            }
        }
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun BottomAppBarContent(
    timeElapsed: Int,
    isChronometerPaused: Boolean,
    progress: Float,
    timerProgress: Float,
    restTime: Int,
    startChronometer: () -> Unit,
    pauseChronometer: () -> Unit,
    modifyRestTime: (Boolean) -> Unit
) {

    Column(modifier = Modifier.fillMaxSize()) {
        val animatedProgress = animateFloatAsState(
            targetValue = progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            label = ""
        )
        LinearProgressIndicator(
            progress = { animatedProgress.value },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f),
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .weight(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            BoxWithConstraints(
                modifier = Modifier
                    .weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                val maxHeight = maxHeight.value
                val maxWidth = maxWidth.value
                val animatedWidth = animateFloatAsState(
                    targetValue = if (isChronometerPaused) maxHeight else maxWidth
                )
                //Play button
                FilledIconButton(
                    onClick = {
                        if (isChronometerPaused) startChronometer()
                        else pauseChronometer()
                    },
                    modifier = Modifier
                        .height(maxHeight.dp)
                        .width(animatedWidth.value.dp)
                ) {
                    Icon(
                        imageVector = if (isChronometerPaused) ImageVector.vectorResource(R.drawable.ic_play_arrow) else
                            ImageVector.vectorResource(id = R.drawable.ic_pause),
                        contentDescription = stringResource(if (isChronometerPaused) R.string.pause else R.string.resume),
                        modifier = Modifier.fillMaxSize(0.7f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.33f)
                    .padding(start = 15.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.elapsed_time),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = formatTime(timeElapsed)
                )
            }


            val timerProgress = animateFloatAsState(
                targetValue = timerProgress,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                label = "timerAnimation"
            )
            //Rest timer
            Row(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { modifyRestTime(false) },
                    enabled = restTime != 0
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_replay_10),
                        contentDescription = stringResource(R.string.add_ten_seconds),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp),
                        progress = { timerProgress.value },
                        strokeWidth = 5.dp
                    )
                    Text("$restTime")
                }
                IconButton(
                    onClick = { modifyRestTime(true) },
                    enabled = restTime != 0
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_forward_10),
                        contentDescription = stringResource(R.string.reduce_ten_seconds),
                        modifier = Modifier.size(40.dp)
                    )
                }

            }

        }
    }
}