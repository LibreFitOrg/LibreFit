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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import org.librefit.R
import org.librefit.data.ExerciseDC
import org.librefit.db.entity.Exercise
import org.librefit.db.relations.ExerciseWithSets
import org.librefit.enums.InfoMode
import org.librefit.nav.Route
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.ExerciseCard
import org.librefit.ui.components.animations.DumbbellLottie
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.components.modalBottomSheets.ExerciseDetailModalBottomSheet
import org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.util.Formatter.formatTime
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    val viewModel: WorkoutScreenViewModel = hiltViewModel()


    LaunchedEffect(Unit) {
        //It retrieves data from DB
        viewModel.initializeExercises(sharedViewModel.getPassedExercises())

        //It adds the selected exercises from AddExerciseScreen
        sharedViewModel.getSelectedExercisesList().forEach { exerciseDC ->
            viewModel.addExerciseWithSets(
                ExerciseWithSets(Exercise(exerciseId = exerciseDC.id), exerciseDC = exerciseDC)
            )
        }
    }

    val timeElapsed by viewModel.timeElapsed.collectAsState()


    val keepWorkoutScreenOn by viewModel.keepScreenOn.collectAsState(initial = true)

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


    var showExitDialog by remember { mutableStateOf(false) }

    if (showExitDialog) {
        ConfirmDialog(
            title = stringResource(R.string.exit),
            text = stringResource(id = R.string.exit_workout),
            onConfirm = {
                navController.popBackStack()
                showExitDialog = false
            },
            onDismiss = { showExitDialog = false }
        )
    }


    var infoMode by remember { mutableStateOf(InfoMode.DISMISS) }

    InfoModalBottomSheet(infoMode) { infoMode = InfoMode.DISMISS }


    BackHandler(enabled = !showExitDialog && !viewModel.isListEmpty()) {
        showExitDialog = true
    }


    var isExerciseDetailsOpen by remember { mutableStateOf(false) }

    /**
     * It holds [ExerciseDC] for [ExerciseDetailModalBottomSheet]
     */
    var selectedExercise by remember { mutableStateOf<ExerciseDC>(ExerciseDC()) }

    if (isExerciseDetailsOpen) {
        ExerciseDetailModalBottomSheet(exercise = selectedExercise) {
            isExerciseDetailsOpen = false
        }
    }

    CustomScaffold(
        title = AnnotatedString(stringResource(R.string.workout)),
        navigateBack = {
            if (viewModel.isListEmpty()) {
                navController.popBackStack()
            } else {
                showExitDialog = true
            }
        },
        actions = listOf {
            sharedViewModel.setPassedData(
                workout = sharedViewModel.getPassedWorkout().copy(
                    id = 0,
                    timeElapsed = timeElapsed,
                    completed = LocalDateTime.now(),
                    routine = false
                ),
                exercises = viewModel.getExercises(),
            )
            navController.navigate(Route.BeforeSavingScreen)
        },
        actionsEnabled = listOf(!viewModel.isListEmpty()),
        actionsDescription = listOf(stringResource(R.string.done)),
        fabIcon = ImageVector.vectorResource(R.drawable.ic_add),
        fabAction = {
            navController.navigate(Route.ExercisesScreen(addExercises = true))
        },
        fabDescription = stringResource(R.string.add_exercise),
        bottomBar = {
            BottomAppBar {
                BottomAppBarContent(viewModel)
            }
        }
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewModel.isListEmpty()) {
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
                        items = viewModel.exercisesWithSets,
                        key = { i, exercise -> exercise.exercise.id }
                    ) { i, exerciseWithSets ->
                        ExerciseCard(
                            modifier = Modifier.animateItem(),
                            exerciseWithSets = exerciseWithSets,
                            addSet = {
                                viewModel.addSetToExercise(i)
                            },
                            onDetail = {
                                selectedExercise = exerciseWithSets.exerciseDC
                                isExerciseDetailsOpen = true
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
                            setChronometerIsRunning = viewModel.setChronometerIsRunning,
                            setWithRunningChronometer = viewModel.setWithRunningChronometer,
                            workout = true
                        )
                    }
                }

                bottomMargin()
            }
        }
    }


    val lifecycleOwner = LocalLifecycleOwner.current

    val lifecycleObserver = remember {
        object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                viewModel.updateFocus(isFocused = true)
            }

            override fun onPause(owner: LifecycleOwner) {
                viewModel.updateFocus(isFocused = false)
            }
        }
    }

    // Attach the observer to the lifecycle
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun BottomAppBarContent(viewModel: WorkoutScreenViewModel) {
    val timeElapsed by viewModel.timeElapsed.collectAsState()

    val isChronometerPaused by viewModel.isChronometerPaused.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        val animatedProgress = animateFloatAsState(
            targetValue = viewModel.getProgress(),
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
                        if (isChronometerPaused) viewModel.startChronometer()
                        else viewModel.pauseChronometer()
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
                targetValue = viewModel.getRestTimeProgress(),
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
                    onClick = { viewModel.modifyRestTime(false) },
                    enabled = viewModel.restTime != 0
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
                    Text("${viewModel.restTime}")
                }
                IconButton(
                    onClick = { viewModel.modifyRestTime(true) },
                    enabled = viewModel.restTime != 0
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