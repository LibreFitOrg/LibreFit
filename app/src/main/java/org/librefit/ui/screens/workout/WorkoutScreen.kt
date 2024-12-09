/*
 * Copyright (c) 2024 LibreFit
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import org.librefit.R
import org.librefit.data.DataStoreManager
import org.librefit.data.SharedViewModel
import org.librefit.db.Workout
import org.librefit.nav.Destination
import org.librefit.ui.components.ConfirmDialog
import org.librefit.ui.components.ExerciseCard
import org.librefit.ui.components.ExerciseDetailModalBottomSheet
import org.librefit.util.ExerciseDC
import org.librefit.util.ExerciseWithSets
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Stable
@Composable
fun WorkoutScreen(
    userPreferences: DataStoreManager,
    workoutId: Int = 0,
    workoutTitle: String,
    navController: NavHostController,
    list: List<ExerciseDC>,
    sharedViewModel: SharedViewModel
) {
    /*
    This will pass "workoutId" and "list" to the view model so it can load
    exercises from db just one time (in initialization)
     */
    val viewModel: WorkoutScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass == WorkoutScreenViewModel::class.java) {
                    "Unknown ViewModel class"
                }
                @Suppress("UNCHECKED_CAST")
                return WorkoutScreenViewModel(workoutId, list) as T
            }
        }
    )


    //It adds the selected exercises from AddExerciseScreen
    LaunchedEffect(Unit) {
        sharedViewModel.getSelectedExercisesList().forEach { exerciseDC ->
            viewModel.addExerciseWithSets(
                ExerciseWithSets(
                    exerciseDC = exerciseDC
                )
            )
        }
    }


    val keepWorkoutScreenOn = userPreferences.workoutScreenOn.collectAsState(initial = true).value

    val context = LocalContext.current

    //It keeps the screen turned on
    if (keepWorkoutScreenOn) {
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
            title = stringResource(R.string.label_exit_dialog),
            text = stringResource(id = R.string.label_exit_workout),
            onConfirm = {
                navController.popBackStack()
                showExitDialog = false
            },
            onDismiss = { showExitDialog = false }
        )
    }


    BackHandler(enabled = !showExitDialog && !viewModel.isListEmpty()) {
        showExitDialog = true
    }

    var isModalSheetOpen by remember { mutableStateOf(false) }

    /**
     * It holds [ExerciseDC] for [ExerciseDetailModalBottomSheet]
     */
    var selectedExercise by remember { mutableStateOf<ExerciseDC?>(null) }

    val animatedProgress = animateFloatAsState(
        targetValue = viewModel.getProgress(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = ""
    ).value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (workoutTitle.isEmpty()) stringResource(R.string.label_new_workout)
                        else stringResource(R.string.label_workout) + ": $workoutTitle",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (viewModel.isListEmpty()) {
                                navController.popBackStack()
                            } else {
                                showExitDialog = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.label_navigate_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveExercisesWithWorkout(
                                workout = Workout(
                                    title = workoutTitle.ifEmpty {
                                        context.getString(R.string.label_workout)
                                    },
                                    timeElapsed = viewModel.timeElapsed
                                ),
                                exercises = viewModel.getExercises()
                            )
                            navController.popBackStack()
                        },
                        enabled = !viewModel.isListEmpty(),
                        colors = IconButtonDefaults.filledIconButtonColors()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = Icons.Default.Done.name
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(120.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Stopwatch(viewModel)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Destination.AddExerciseScreen)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.label_add_exercise)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(start = 15.dp, end = 15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.isListEmpty()) {
                item {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_monochrome),
                        contentDescription = ""
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.label_add_to_empty_workout),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(viewModel.exercises, key = { it.id }) { exerciseWithSets ->
                    ExerciseCard(
                        exerciseWithSets = exerciseWithSets,
                        addSet = {
                            viewModel.addSetToExercise(exerciseWithSets)
                        },
                        onDetail = {
                            selectedExercise = exerciseWithSets.exerciseDC
                            isModalSheetOpen = true
                        },
                        onDelete = {
                            viewModel.deleteExercise(exerciseWithSets)
                        },
                        updateSet = { set, value, mode ->
                            viewModel.updateSet(
                                exercise = exerciseWithSets,
                                set = set,
                                value = value,
                                mode = mode
                            )
                        },
                        updateExercise = { value, mode ->
                            viewModel.updateExercise(
                                exercise = exerciseWithSets,
                                value = value,
                                mode = mode
                            )
                        },
                        workout = true
                    )
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    if (isModalSheetOpen) {
        ExerciseDetailModalBottomSheet(exercise = selectedExercise!!) { isModalSheetOpen = false }
    }
}

@Composable
private fun Stopwatch(viewModel: WorkoutScreenViewModel) {

    val animatedExpansion = animateIntAsState(
        targetValue = if (viewModel.isTimerRunning) 80 else 55,
        label = ""
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(0.4f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.label_elapsed_time),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = formatTime(viewModel.timeElapsed),
                color = if (viewModel.pulsingTimer()) Color.Transparent else Color.Unspecified
            )
        }
        Box(
            modifier = Modifier
                .height(70.dp)
                .weight(0.2f),
            contentAlignment = Alignment.Center
        ) {
            FilledIconButton(
                onClick = {
                    if (viewModel.isTimerRunning) viewModel.stopTimer() else viewModel.startTimer()
                },
                modifier = Modifier.size(animatedExpansion.value.dp)
            ) {
                Icon(
                    imageVector = if (viewModel.isTimerRunning) ImageVector.vectorResource(id = R.drawable.ic_pause)
                    else Icons.Default.PlayArrow,
                    contentDescription = stringResource(if (viewModel.isTimerRunning) R.string.label_pause else R.string.label_play),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(0.4f))
    }
}

private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
}