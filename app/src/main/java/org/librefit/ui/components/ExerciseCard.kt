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
 *
 * LibreFit is subject to additional terms covering author attribution and
 * trademark usage, as found in the accompanying ADDITIONAL_TERMS.md file.
 */

package org.librefit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.enums.PreviousPerformanceSet
import org.librefit.enums.SetMode
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import kotlin.math.roundToInt

/**
 * A custom [ElevatedCard] designed to display an [UiExerciseWithSets] with a uniform appearance across
 * the app.
 *
 * @param modifier A [Modifier] that should be passed as `Modifier.animateItem` to enable
 * animation for the card within the list.
 * @param animatedVisibilityScope Used for image's animation transition
 * @param exerciseWithSets An instance of [UiExerciseWithSets] containing all the relevant information
 * required for the card display.
 * @param previousPerformances When not null and not empty, it displays the performances of previous set next
 * to the associated set. The strings should be already formatted and ready to be displayed.
 * @param addSet A lambda function invoked when the "Add set" button is clicked.
 * @param onDetail A lambda function triggered when the exercise's name or image is clicked, which should open
 * the [org.librefit.ui.screens.infoExercise.InfoExerciseScreen].
 * @param onDelete A lambda function executed when the *Delete* icon is clicked, it should result in
 * the removal of the card.
 * @param updateExerciseNotes A function to update notes based on [UiExercise.id]. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateExerciseNotes] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateExerciseNotes].
 * @param updateExerciseRestTime A function to update rest time based on [UiExercise.id]. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateExerciseRestTime] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateExerciseRestTime].
 * @param updateExerciseSetMode A function to update the set mode based on.
 * For more details, refer to [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateExerciseSetMode]
 * and [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateExerciseSetMode].
 * @param updateSetLoad A function to update load based on [UiSet.id]. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateSetLoad] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateSetLoad].
 * @param updateSetReps A function to update reps based on [UiSet.id]. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateSetReps] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateSetReps].
 * @param updateSetTime A function to update time based on [UiSet.id].. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateSetTime] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateSetTime].
 * @param updateSetCompleted A function to update completed state based on [UiSet.id]. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateSetCompleted] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateSetCompleted].
 * @param deleteSet A function called when the user swipes the set to remove it.
 * @param showInfo A lambda function executed when info icon next to "type of set" or "rest time" text
 * is clicked. The passed parameter is used by [org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet] to show the relevant information.
 * @param idSetWithRunningStopwatch The ID of the set whose stopwatch is currently active. This ensures
 * only one timer runs at a time. The composable will display a running stopwatch for the
 * set matching this ID. Pass null if no timer is active. This parameter is only used when [workout] is `true`.
 * @param updateIdSetWithRunningStopwatch A callback invoked when the user interacts with a set with a
 * running stopwatch. It provides the ID of the set that should become active, or null to stop the current timer.
 * This parameter is only used when [workout] is `true`.
 * @param workout A Boolean flag indicating whether a checkbox should be displayed next to each set.
 * @param applyPreviousSetPerformance Triggered when the user clicks the previous set performance
 * (on the left to the set counter) * and should update the current set with the values of the previous set.
 */
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SharedTransitionScope.ExerciseCard(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    exerciseWithSets: UiExerciseWithSets,
    previousPerformances: List<PreviousPerformanceSet>? = null,
    workout: Boolean = false,
    idSetWithRunningStopwatch: Long? = null,
    addSet: (Long) -> Unit,
    onDetail: (Long, UiExerciseDC) -> Unit,
    onDelete: (Long) -> Unit,
    deleteSet: (Long) -> Unit,
    updateExerciseNotes: (String, Long) -> Unit,
    updateExerciseRestTime: (Int, Long) -> Unit,
    updateExerciseSetMode: (SetMode, Long) -> Unit,
    updateSetTime: (Int, Long) -> Unit,
    updateSetReps: (Int, Long) -> Unit,
    updateSetLoad: (Double, Long) -> Unit,
    updateSetCompleted: (Boolean, Long) -> Unit,
    showInfo: (InfoMode) -> Unit,
    updateIdSetWithRunningStopwatch: (Long?) -> Unit = {},
    applyPreviousSetPerformance: (Long) -> Unit = {}
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            onDetail(exerciseWithSets.exercise.id, exerciseWithSets.exerciseDC)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val model =
                        remember(exerciseWithSets.exerciseDC.images) { exerciseWithSets.exerciseDC.images.firstOrNull() }
                    AsyncImage(
                        model = model?.let { "file:///android_asset/${it}" },
                        fallback = painterResource(R.drawable.no_image),
                        contentDescription = exerciseWithSets.exerciseDC.name,
                        contentScale = ContentScale.Crop,
                        colorFilter = if (model == null) ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant) else null,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = exerciseWithSets.exercise.id.toString() + exerciseWithSets.exerciseDC.id
                                ),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .size(50.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                    Text(
                        text = exerciseWithSets.exerciseDC.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                IconButton(onClick = { onDelete(exerciseWithSets.exercise.id) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }

            OutlinedTextField(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.notes)) },
                value = exerciseWithSets.exercise.notes,
                onValueChange = { updateExerciseNotes(it, exerciseWithSets.exercise.id) }
            )

            //Rest timer slider
            var restTime by remember { mutableIntStateOf(exerciseWithSets.exercise.restTime) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    // Read more at InfoModalBottomSheet
                    onClick = { showInfo(InfoMode.REST_TIMER) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info),
                        contentDescription = stringResource(R.string.info)
                    )
                }
                Text(stringResource(R.string.rest_time) + ": " + restTime
                        + " " + stringResource(R.string.seconds).replaceFirstChar { it.lowercase() })
            }

            val haptic = LocalHapticFeedback.current
            Slider(
                value = restTime.toFloat(),
                onValueChange = {
                    // By dividing first and then multiplying by 5, it rounds to the closest number multiple of 5
                    restTime = (it / 5).roundToInt() * 5
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                },
                onValueChangeFinished = {
                    updateExerciseRestTime(
                        restTime,
                        exerciseWithSets.exercise.id
                    )
                },
                valueRange = 0f..300f,
                // 19 steps means values multiple of 5
                steps = 19
            )

            HorizontalDivider()

            // Set mode selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(0.5f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        // Refer to InfoModalBottomSheet to know the reason behind this value.
                        // Do NOT change it.
                        onClick = { showInfo(InfoMode.TYPE_OF_SET) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info),
                            contentDescription = stringResource(R.string.info) + ":"
                        )
                    }
                    Text(stringResource(R.string.type_of_set))
                }


                var expanded by remember { mutableStateOf(false) }

                val focusRequester = remember { FocusRequester() }

                // Type of set selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .weight(0.5f)
                        .clickable {
                            expanded = !expanded
                            focusRequester.requestFocus()
                        }
                        .focusRequester(focusRequester)
                        .focusable()
                ) {
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.large,
                        readOnly = true,
                        value = stringResource(Formatter.setModeToStringId(exerciseWithSets.exercise.setMode)),
                        onValueChange = {},
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        SetMode.entries.forEachIndexed { _, mode ->
                            DropdownMenuItem(
                                onClick = {
                                    updateExerciseSetMode(mode, exerciseWithSets.exercise.id)
                                    expanded = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(Formatter.setModeToStringId(mode))
                                    )
                                },
                                trailingIcon = if (exerciseWithSets.exercise.setMode == mode) {
                                    {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_check),
                                            contentDescription = stringResource(R.string.checkbox)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.background(
                                    if (exerciseWithSets.exercise.setMode == mode) MaterialTheme.colorScheme.inversePrimary.copy(
                                        0.3f
                                    ) else Color.Unspecified
                                )
                            )
                        }
                    }
                }
            }

            ElevatedCard(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                //Headline set
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (previousPerformances != null) Arrangement.SpaceBetween else Arrangement.SpaceAround
                ) {
                    Text(
                        text = stringResource(id = R.string.set),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    if (previousPerformances != null) {
                        Text(
                            text = "Previous",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (exerciseWithSets.exercise.setMode == SetMode.DURATION) {
                        Text(
                            text = stringResource(R.string.time),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        if (exerciseWithSets.exercise.setMode == SetMode.LOAD ||
                            exerciseWithSets.exercise.setMode == SetMode.BODYWEIGHT_WITH_LOAD
                        ) {
                            Text(
                                text = stringResource(R.string.load) + " (" + stringResource(R.string.kg) + ")",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.reps),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (workout) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = stringResource(R.string.done)
                        )
                    }
                }

                //Sets
                Column(modifier = Modifier.animateContentSize()) {
                    exerciseWithSets.sets.forEachIndexed { i, set ->
                        key(set.id) {
                            Set(
                                i = i,
                                set = set,
                                previousSet = previousPerformances?.getOrNull(i),
                                lastIndex = exerciseWithSets.sets.lastIndex,
                                setMode = exerciseWithSets.exercise.setMode,
                                isStopwatchRunning = idSetWithRunningStopwatch == null,
                                isThisSetStopwatchRunning = idSetWithRunningStopwatch == set.id,
                                workout = workout,
                                deleteSet = deleteSet,
                                updateIdSetWithRunningStopwatch = updateIdSetWithRunningStopwatch,
                                updateSetTime = updateSetTime,
                                updateSetReps = updateSetReps,
                                updateSetLoad = updateSetLoad,
                                updateSetCompleted = updateSetCompleted,
                                applyPreviousSet = applyPreviousSetPerformance
                            )
                        }
                    }
                }
            }

            //Add set button
            LibreFitButton(
                text = stringResource(id = R.string.add_set),
                icon = painterResource(R.drawable.ic_add_circle),
                onClick = { addSet(exerciseWithSets.exercise.id) },
                elevated = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Set(
    i: Int,
    set: UiSet,
    previousSet: PreviousPerformanceSet? = null,
    lastIndex: Int,
    setMode: SetMode,
    isStopwatchRunning: Boolean,
    isThisSetStopwatchRunning: Boolean,
    workout: Boolean,
    deleteSet: (Long) -> Unit,
    updateSetTime: (Int, Long) -> Unit,
    updateSetReps: (Int, Long) -> Unit,
    updateSetLoad: (Double, Long) -> Unit,
    updateSetCompleted: (Boolean, Long) -> Unit,
    updateIdSetWithRunningStopwatch: (Long?) -> Unit,
    applyPreviousSet: (Long) -> Unit
) {
    val timeValue by rememberUpdatedState(set.elapsedTime)
    var repValue by remember(set.reps) { mutableStateOf(set.reps.toString()) }
    var weightValue by remember(set.load) { mutableStateOf(set.load.toString()) }

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(swipeToDismissBoxState.currentValue) {
        if (swipeToDismissBoxState.currentValue != SwipeToDismissBoxValue.Settled) {
            haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        }
    }

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        onDismiss = { deleteSet(set.id) },
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(
                            topStart = CornerSize(if (i == 0) 45 else 0),
                            topEnd = CornerSize(if (i == 0) 45 else 0),
                            bottomEnd = CornerSize(
                                if (i == lastIndex) 45 else 0
                            ),
                            bottomStart = CornerSize(
                                if (i == lastIndex) 45 else 0
                            ),
                        )
                    )
                    .background(
                        when (swipeToDismissBoxState.dismissDirection) {
                            SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                        }
                    )
                    .padding(start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = when (swipeToDismissBoxState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                    SwipeToDismissBoxValue.Settled -> Arrangement.Start
                    SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.delete),
                )
            }
        }
    ) {
        Row(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = CornerSize(if (i == 0) 45 else 0),
                        topEnd = CornerSize(if (i == 0) 45 else 0),
                        bottomEnd = CornerSize(
                            if (i == lastIndex) 45 else 0
                        ),
                        bottomStart = CornerSize(
                            if (i == lastIndex) 45 else 0
                        ),
                    )
                )
                .background(
                    if (set.completed) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHighest
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (previousSet != null) Arrangement.SpaceBetween else Arrangement.SpaceAround
        ) {
            Text(
                text = "${i + 1}",
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 20.dp)
            )

            previousSet?.let { values ->
                TextButton(
                    onClick = { applyPreviousSet(set.id) },
                    modifier = Modifier.wrapContentWidth()
                ) {
                    val (previousReps, previousLoad, previousTime) = values
                    val text = when (setMode) {
                        SetMode.LOAD -> "$previousLoad" + stringResource(R.string.kg) + " * $previousReps"
                        SetMode.BODYWEIGHT -> "$previousReps"
                        SetMode.BODYWEIGHT_WITH_LOAD -> "$previousLoad" + stringResource(R.string.kg)
                        SetMode.DURATION -> Formatter.formateSecondsInMinutesAndSeconds(previousTime)
                    }
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (setMode == SetMode.DURATION) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (workout) {

                        IconButton(
                            enabled = (isStopwatchRunning || isThisSetStopwatchRunning)
                                    && !set.completed,
                            onClick = {
                                val newId = if (isThisSetStopwatchRunning) null else set.id
                                updateIdSetWithRunningStopwatch(newId)
                            }
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isThisSetStopwatchRunning)
                                        R.drawable.ic_pause else R.drawable.ic_play_arrow
                                ),
                                contentDescription = if (isThisSetStopwatchRunning)
                                    stringResource(R.string.resume) else
                                    stringResource(R.string.pause)
                            )
                        }
                    }
                    //Time
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.width(80.dp),
                        value = Formatter.formateSecondsInMinutesAndSeconds(timeValue),
                        onValueChange = { string ->
                            val newTimeValue =
                                Formatter.parseTimeInputToSeconds(string)

                            updateSetTime(newTimeValue, set.id)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        )
                    )
                }
            } else {
                if (setMode == SetMode.LOAD || setMode == SetMode.BODYWEIGHT_WITH_LOAD) {
                    //Weight
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.width(80.dp),
                        value = weightValue,
                        onValueChange = { string ->
                            weightValue = Formatter.normalizeNumericString(string)
                            updateSetLoad(Formatter.parseDoubleFromString(weightValue), set.id)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        )
                    )
                }
                //Reps
                OutlinedTextField(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.width(80.dp),
                    value = repValue,
                    onValueChange = { string ->
                        repValue = Formatter.normalizeNumericString(string)
                        updateSetReps(Formatter.parseIntegerFromString(repValue) ?: 0, set.id)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    )
                )
            }

            if (workout) {
                Checkbox(
                    checked = set.completed,
                    onCheckedChange = { checked ->
                        if (isThisSetStopwatchRunning) {
                            updateIdSetWithRunningStopwatch(null)
                        }
                        updateSetCompleted(checked, set.id)
                    }
                )
            }
        }
    }

}


@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun ExerciseCardPreview() {
    val currentIdSetWithRunningSet = remember { mutableStateOf<Long?>(null) }

    val e = remember {
        mutableStateOf(
            UiExerciseWithSets(
                exercise = UiExercise(
                    notes = "This is a note!",
                    restTime = 90,
                    setMode = SetMode.BODYWEIGHT_WITH_LOAD
                ),
                sets = persistentListOf(UiSet(completed = true), UiSet(elapsedTime = 100)),
                exerciseDC = UiExerciseDC(
                    name = "Exercise name",
                    images = persistentListOf("3_4_Sit-Up/0.jpg")
                )
            )
        )
    }

    val previousPerformances = e.value.sets.map { _ ->
        when (e.value.exercise.setMode) {
            SetMode.BODYWEIGHT -> PreviousPerformanceSet(reps = 10)
            SetMode.DURATION -> PreviousPerformanceSet(time = 124)
            SetMode.BODYWEIGHT_WITH_LOAD -> PreviousPerformanceSet(reps = 10, load = 12.0)
            SetMode.LOAD -> PreviousPerformanceSet(reps = 10, load = 12.0)
        }
    }

    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                ExerciseCard(
                    animatedVisibilityScope = this,
                    exerciseWithSets = e.value,
                    previousPerformances = previousPerformances,
                    addSet = {
                        val newSets = e.value.sets.toMutableList() + UiSet()
                        e.value = e.value.copy(sets = newSets.toImmutableList())
                    },
                    onDetail = { _, _ -> },
                    onDelete = {},
                    deleteSet = { id ->
                        e.value = e.value.copy(sets = e.value.sets.filter { it.id != id }
                            .toImmutableList())
                        if (id == currentIdSetWithRunningSet.value) currentIdSetWithRunningSet.value =
                            null
                    },
                    showInfo = {},
                    idSetWithRunningStopwatch = currentIdSetWithRunningSet.value,
                    updateIdSetWithRunningStopwatch = { currentIdSetWithRunningSet.value = it },
                    workout = true,
                    updateExerciseNotes = { notes, _ ->
                        e.value = e.value.copy(exercise = e.value.exercise.copy(notes = notes))
                    },
                    updateExerciseRestTime = { restTime, _ ->
                        e.value =
                            e.value.copy(exercise = e.value.exercise.copy(restTime = restTime))
                    },
                    updateExerciseSetMode = { setMode, _ ->
                        e.value = e.value.copy(exercise = e.value.exercise.copy(setMode = setMode))
                    },
                    updateSetTime = { time, id ->
                        e.value = e.value.copy(
                            sets = e.value.sets.map {
                                if (it.id == id) it.copy(elapsedTime = time) else it
                            }.toImmutableList()
                        )
                    },
                    updateSetReps = { reps, id ->
                        e.value = e.value.copy(
                            sets = e.value.sets.map {
                                if (it.id == id) it.copy(reps = reps) else it
                            }.toImmutableList()
                        )
                    },
                    updateSetLoad = { load, id ->
                        e.value = e.value.copy(
                            sets = e.value.sets.map {
                                if (it.id == id) it.copy(load = load) else it
                            }.toImmutableList()
                        )
                    },
                    updateSetCompleted = { completed, id ->
                        e.value = e.value.copy(
                            sets = e.value.sets.map {
                                if (it.id == id) it.copy(completed = completed) else it
                            }.toImmutableList()
                        )
                    },
                    applyPreviousSetPerformance = { id ->
                        val index = e.value.sets.indexOfFirst { it.id == id }
                        previousPerformances.getOrNull(index)?.let { p ->
                            e.value = e.value.copy(
                                sets = e.value.sets.map { set ->
                                    if (set.id == id) {
                                        when (e.value.exercise.setMode) {
                                            SetMode.BODYWEIGHT -> set.copy(reps = p.reps)
                                            SetMode.LOAD -> set.copy(load = p.load)
                                            SetMode.DURATION -> set.copy(elapsedTime = p.time)
                                            SetMode.BODYWEIGHT_WITH_LOAD -> set.copy(
                                                load = p.load,
                                                reps = p.reps
                                            )
                                        }
                                    } else set
                                }.toImmutableList()
                            )
                        }
                    }
                )
            }
        }
    }
}