/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
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
import androidx.compose.foundation.lazy.LazyItemScope
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.enums.PreviousPerformanceSet
import org.librefit.enums.SetMode
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.nav.Route
import org.librefit.ui.components.ExerciseCard
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.DumbbellLottie
import org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter

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

    val timeElapsed by viewModel.timeElapsed.collectAsStateWithLifecycle()

    val isStopwatchPaused by viewModel.isStopwatchPaused.collectAsStateWithLifecycle()

    val exercisesWithSets by viewModel.exercises.collectAsStateWithLifecycle()

    val keepWorkoutScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()

    val restTime by viewModel.restTime.collectAsStateWithLifecycle()

    val idSetWithRunningStopwatch by viewModel.idSetWithRunningStopwatch.collectAsStateWithLifecycle()

    val workoutProgress by viewModel.workoutProgress.collectAsStateWithLifecycle()

    val previousPerformances by viewModel.previousPerformances.collectAsStateWithLifecycle()

    val restTimerProgress by viewModel.restTimerProgress.collectAsStateWithLifecycle()

    val runningWorkoutId by viewModel.runningWorkoutId.collectAsStateWithLifecycle()

    val isHeaderSticky by viewModel.isHeaderSticky.collectAsStateWithLifecycle()


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


    val infoMode = remember { mutableStateOf(InfoMode.DISMISS) }

    InfoModalBottomSheet(infoMode.value) { infoMode.value = InfoMode.DISMISS }


    BackHandler {
        viewModel.stopWorkoutService()
        navController.navigateUp()
    }

    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.workout)),
        navigateBack = {
            viewModel.stopWorkoutService()
            navController.navigateUp()
        },
        actions = listOf {
            navController.navigate(
                Route.BeforeSavingScreen(
                    runningWorkoutId = runningWorkoutId
                ),
            ) { launchSingleTop = true }
        },
        actionsEnabled = listOf(!exercisesWithSets.isEmpty()),
        actionsDescription = listOf(stringResource(R.string.done)),
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
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
            WorkoutScreenContent(
                animatedVisibilityScope = animatedVisibilityScope,
                exercisesWithSets = exercisesWithSets,
                previousPerformances = previousPerformances,
                idSetWithRunningStopwatch = idSetWithRunningStopwatch,
                timeElapsed = timeElapsed,
                isStopwatchPaused = isStopwatchPaused,
                workoutProgress = workoutProgress,
                isHeaderSticky = isHeaderSticky,
                toggleStopwatch = viewModel::toggleStopwatch,
                updateIdSetWithRunningStopwatch = viewModel::updateIdSetWithRunningStopwatch,
                onSelectedExerciseIdChange = { id, idExerciseDC ->
                    navController.navigate(
                        Route.InfoExerciseScreen(
                            id,
                            idExerciseDC
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
                showInfo = { infoMode.value = it },
                applyPreviousSetPerformance = viewModel::applyPreviousSetPerformance
            )
        }
    }




    val lifecycleOwner = LocalLifecycleOwner.current

    // Save state of running workout when this screen is the primary focus
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.runningWorkoutState.collect {
                viewModel.saveRunningWorkout(it)
            }
        }
    }


    // Keep track of focus to play alter sound or not
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
    previousPerformances: List<List<PreviousPerformanceSet>?>,
    timeElapsed: Int,
    isStopwatchPaused: Boolean,
    workoutProgress: Pair<Int, Int>,
    idSetWithRunningStopwatch: Long?,
    isHeaderSticky: Boolean,
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
    onSelectedExerciseIdChange: (Long, String) -> Unit,
    showInfo: (InfoMode) -> Unit,
    applyPreviousSetPerformance: (Long) -> Unit
) {
    LibreFitLazyColumn {
        val headerContent: @Composable LazyItemScope.() -> Unit = {
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

        if (isHeaderSticky) {
            stickyHeader { headerContent() }
        } else {
            item { headerContent() }
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
                key = { _, exercise -> exercise.exercise.id }
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
            .offset(y = -ScreenOffset, x = -ScreenOffset)
            .zIndex(1f),
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
            exercise = UiExercise(
                setMode = SetMode.LOAD,
                restTime = 120,
                notes = "Feeling well today"
            ),
            exerciseDC = UiExerciseDC(
                name = "Barbell Bench Press - Medium Grip",
                images = persistentListOf("Barbell_Bench_Press_-_Medium_Grip/0.jpg"),
                equipment = Equipment.MACHINE,
                category = Category.STRENGTH
            ),
            sets = persistentListOf(
                UiSet(load = 80.0, reps = 9, completed = true),
                UiSet(load = 80.0, reps = 8),
                UiSet(load = 80.0, reps = 9)
            )
        )
    )
    val workoutProgress = e.sumOf { it.sets.count { s -> s.completed } } to e.sumOf { it.sets.size }
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
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
                            timeElapsed = 186,
                            isStopwatchPaused = false,
                            workoutProgress = workoutProgress,
                            isHeaderSticky = true,
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
                            restTimerProgress = 97f / 120,
                            restTime = 97,
                            modifyRestTime = {},
                            fabAction = {}
                        )
                    }
                }
            }
        }
    }

}