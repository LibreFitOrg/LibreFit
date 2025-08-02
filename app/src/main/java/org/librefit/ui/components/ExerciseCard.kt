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

package org.librefit.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.enums.SetMode
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import org.librefit.util.Formatter.formatTime
import kotlin.math.roundToInt

private val NoOpUpdate: (Long?) -> Unit = {}

/**
 * A custom [ElevatedCard] designed to display an [UiExerciseWithSets] with a uniform appearance across
 * the app.
 *
 * @param modifier A [Modifier] that should be passed as `Modifier.animateItem` to enable
 * animation for the card within the list.
 * @param exerciseWithSets An instance of [UiExerciseWithSets] containing all the relevant information
 * required for the card display.
 * @param addSet A lambda function invoked when the "Add set" button is clicked.
 * @param onDetail A lambda function triggered when the *Info* icon is clicked, which should open
 * the [org.librefit.ui.components.modalBottomSheets.ExerciseDetailModalBottomSheet].
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
 * @param updateSetReps A function to update reps based on [UiSet.id].. For more details, refer to
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
 * @param idSetWithRunningChronometer The ID of the set whose timer is currently active. This ensures
 * only one timer runs at a time. The composable will display a running chronometer for the
 * set matching this ID. Pass null if no timer is active. This parameter is only used when [workout] is `true`.
 * @param updateIdSetWithRunningChronometer A callback invoked when the user interacts with a set with a
 * running chronometer. It provides the ID of the set that should become active, or null to stop the current timer.
 * This parameter is only used when [workout] is `true`.
 * @param workout A Boolean flag indicating whether a checkbox should be displayed next to each set.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCard(
    modifier: Modifier = Modifier,
    exerciseWithSets: UiExerciseWithSets,
    workout: Boolean = false,
    idSetWithRunningChronometer: Long? = null,
    addSet: (Long) -> Unit,
    onDetail: (String) -> Unit,
    onDelete: (Long) -> Unit,
    deleteSet: (Long) -> Unit,
    updateExerciseNotes: (String, Long) -> Unit,
    updateExerciseRestTime: (Int, Long) -> Unit,
    updateExerciseSetMode: (SetMode, Long) -> Unit,
    updateSetTime: (Int, Long) -> Unit,
    updateSetReps: (Int, Long) -> Unit,
    updateSetLoad: (Float, Long) -> Unit,
    updateSetCompleted: (Boolean, Long) -> Unit,
    showInfo: (InfoMode) -> Unit,
    updateIdSetWithRunningChronometer: (Long?) -> Unit = NoOpUpdate
) {
    ElevatedCard(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exerciseWithSets.exerciseDC.name,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onDetail(exerciseWithSets.exerciseDC.id) }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_info),
                        contentDescription = stringResource(R.string.info)
                    )
                }
                IconButton(onClick = { onDelete(exerciseWithSets.exercise.id) }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.notes)) },
                value = exerciseWithSets.exercise.notes,
                onValueChange = { updateExerciseNotes(it, exerciseWithSets.exercise.id) }
            )

            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))

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
                        imageVector = ImageVector.vectorResource(R.drawable.ic_info),
                        contentDescription = stringResource(R.string.info)
                    )
                }
                Text(stringResource(R.string.rest_time) + ": " + restTime
                        + " " + stringResource(R.string.seconds).replaceFirstChar { it.lowercase() })
            }

            val view = LocalView.current
            var oldValue by remember { mutableIntStateOf(0) }
            Slider(
                value = restTime.toFloat(),
                onValueChange = {
                    restTime = it.roundToInt()
                    if (restTime != oldValue) {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
                        oldValue = restTime
                    }
                },
                onValueChangeFinished = {
                    updateExerciseRestTime(
                        restTime,
                        exerciseWithSets.exercise.id
                    )
                },
                valueRange = 0f..300f,
                steps = 19
            )

            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))

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
                            imageVector = ImageVector.vectorResource(R.drawable.ic_info),
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
                        readOnly = true,
                        value = stringResource(setModeToStringId(exerciseWithSets.exercise.setMode)),
                        onValueChange = {},
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        SetMode.entries.forEachIndexed { index, mode ->
                            DropdownMenuItem(
                                onClick = {
                                    updateExerciseSetMode(mode, exerciseWithSets.exercise.id)
                                    expanded = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(setModeToStringId(mode))
                                    )
                                },
                                trailingIcon = if (exerciseWithSets.exercise.setMode == mode) {
                                    {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_check),
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

            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))

            //Headline set
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = stringResource(id = R.string.set),
                    color = MaterialTheme.colorScheme.secondary
                )
                if (exerciseWithSets.exercise.setMode == SetMode.DURATION) {
                    Text(
                        text = stringResource(R.string.time),
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.reps),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (exerciseWithSets.exercise.setMode == SetMode.LOAD ||
                        exerciseWithSets.exercise.setMode == SetMode.BODYWEIGHT_WITH_LOAD
                    ) {
                        Text(
                            text = stringResource(R.string.load) + " (" + stringResource(R.string.kg) + ")",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                if (workout) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_check),
                        contentDescription = stringResource(R.string.done)
                    )
                }
            }

            //Sets
            Sets(
                exerciseWithSets = exerciseWithSets,
                deleteSet = deleteSet,
                workout = workout,
                idSetWithRunningChronometer = idSetWithRunningChronometer,
                updateIdSetWithRunningChronometer = updateIdSetWithRunningChronometer,
                updateSetTime = updateSetTime,
                updateSetReps = updateSetReps,
                updateSetLoad = updateSetLoad,
                updateSetCompleted = updateSetCompleted
            )

            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))

            //Add set button
            LibreFitButton(
                text = stringResource(id = R.string.add_set),
                icon = ImageVector.vectorResource(R.drawable.ic_add_circle),
                onClick = { addSet(exerciseWithSets.exercise.id) },
                elevated = false
            )
        }
    }
}

@Composable
private fun Sets(
    exerciseWithSets: UiExerciseWithSets,
    deleteSet: (Long) -> Unit,
    updateSetTime: (Int, Long) -> Unit,
    updateSetReps: (Int, Long) -> Unit,
    updateSetLoad: (Float, Long) -> Unit,
    updateSetCompleted: (Boolean, Long) -> Unit,
    workout: Boolean,
    idSetWithRunningChronometer: Long?,
    updateIdSetWithRunningChronometer: (Long?) -> Unit
) {

    val setHeight = 60
    val animatedSetsColumnHeight = animateDpAsState(
        targetValue = (exerciseWithSets.sets.size * setHeight).dp,
        animationSpec = tween(600),
        label = "animatedSetsColumnHeight",
    )
    LazyColumn(
        modifier = Modifier.height(animatedSetsColumnHeight.value)
    ) {
        itemsIndexed(
            items = exerciseWithSets.sets,
            key = { i, set -> set.id }
        ) { i, set ->
            val timeValue by rememberUpdatedState(set.elapsedTime)
            val repValue by rememberUpdatedState(set.reps.toString())
            val weightValue by rememberUpdatedState(set.load.toString())

            val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    when (it) {
                        SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
                        else -> deleteSet(set.id)
                    }
                    return@rememberSwipeToDismissBoxState true
                },
                positionalThreshold = { it * 0.3f }
            )

            var zoom by remember { mutableStateOf(false) }
            val view = LocalView.current
            LaunchedEffect(swipeToDismissBoxState.progress < 0.3f) {
                if (swipeToDismissBoxState.progress < 0.3f || swipeToDismissBoxState.progress == 1f) {
                    zoom = false
                } else {
                    view.performHapticFeedback(HapticFeedbackConstantsCompat.DRAG_START)
                    zoom = true
                }
            }

            val size = animateDpAsState(
                targetValue = if (zoom) {
                    (ImageVector.vectorResource(R.drawable.ic_delete).defaultHeight.value * 1.2f).dp
                } else {
                    ImageVector.vectorResource(R.drawable.ic_delete).defaultHeight
                }
            )

            SwipeToDismissBox(
                modifier = Modifier.animateItem(),
                state = swipeToDismissBoxState,
                backgroundContent = {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(
                                RoundedCornerShape(
                                    topStart = CornerSize(if (i == 0) 15 else 0),
                                    topEnd = CornerSize(if (i == 0) 15 else 0),
                                    bottomEnd = CornerSize(
                                        if (i == exerciseWithSets.sets.lastIndex) 15 else 0
                                    ),
                                    bottomStart = CornerSize(
                                        if (i == exerciseWithSets.sets.lastIndex) 15 else 0
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            modifier = Modifier.size(size.value),
                            imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete),
                        )
                        Icon(
                            modifier = Modifier.size(size.value),
                            imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete),
                        )
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = CornerSize(if (i == 0) 15 else 0),
                                topEnd = CornerSize(if (i == 0) 15 else 0),
                                bottomEnd = CornerSize(
                                    if (i == exerciseWithSets.sets.lastIndex) 15 else 0
                                ),
                                bottomStart = CornerSize(
                                    if (i == exerciseWithSets.sets.lastIndex) 15 else 0
                                ),
                            )
                        )
                        .background(
                            if (set.completed) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow
                        )
                        .height(setHeight.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = "${i + 1}",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 20.dp, end = 10.dp)
                    )

                    if (exerciseWithSets.exercise.setMode == SetMode.DURATION) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (workout) {

                                IconButton(
                                    enabled = (idSetWithRunningChronometer == null
                                            || idSetWithRunningChronometer == set.id)
                                            && !set.completed,
                                    onClick = {
                                        val newId =
                                            if (idSetWithRunningChronometer == set.id) null else set.id
                                        updateIdSetWithRunningChronometer(newId)
                                    }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(
                                            if (idSetWithRunningChronometer == set.id)
                                                R.drawable.ic_pause else R.drawable.ic_play_arrow
                                        ),
                                        contentDescription = if (idSetWithRunningChronometer == set.id)
                                            stringResource(R.string.resume) else
                                            stringResource(R.string.pause)
                                    )
                                }
                            }
                            //Time
                            OutlinedTextField(
                                modifier = Modifier.width(80.dp),
                                value = formatTime(timeValue).substring(3),
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
                        //Reps
                        OutlinedTextField(
                            modifier = Modifier.width(80.dp),
                            value = repValue,
                            onValueChange = { string ->
                                val newRepValue = Formatter.parseIntegerValueInput(string)

                                newRepValue?.let { updateSetReps(it, set.id) }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent
                            )
                        )
                        if (exerciseWithSets.exercise.setMode == SetMode.LOAD ||
                            exerciseWithSets.exercise.setMode == SetMode.BODYWEIGHT_WITH_LOAD
                        ) {
                            //Weight
                            OutlinedTextField(
                                modifier = Modifier.width(80.dp),
                                value = weightValue,
                                onValueChange = { newString ->
                                    val newWeightValue = Formatter.parseFloatValueInput(newString)

                                    newWeightValue?.let { updateSetLoad(it, set.id) }
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
                    }

                    if (workout) {
                        Checkbox(
                            checked = set.completed,
                            onCheckedChange = { checked ->
                                if (idSetWithRunningChronometer == set.id) {
                                    updateIdSetWithRunningChronometer(null)
                                }
                                updateSetCompleted(checked, set.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun setModeToStringId(setMode: SetMode): Int {
    return when (setMode) {
        SetMode.LOAD -> R.string.load
        SetMode.BODYWEIGHT_WITH_LOAD -> R.string.bodyweight_with_load
        SetMode.BODYWEIGHT -> R.string.bodyweight
        SetMode.DURATION -> R.string.duration
    }
}

@Preview
@Composable
private fun ExerciseCardPreview() {
    var currentIdSetWithRunningSet by remember { mutableStateOf<Long?>(null) }

    var e by remember {
        mutableStateOf(
            UiExerciseWithSets(
                exercise = UiExercise(
                    notes = "This is a note!",
                    restTime = 90,
                    setMode = SetMode.DURATION
                ),
                sets = persistentListOf(UiSet(completed = true), UiSet(elapsedTime = 100)),
                exerciseDC = UiExerciseDC(name = "Exercise name")
            )
        )
    }

    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        ExerciseCard(
            exerciseWithSets = e,
            addSet = {
                val newSets = e.sets.toMutableList() + UiSet()
                e = e.copy(sets = newSets.toImmutableList())
            },
            onDetail = {},
            onDelete = {},
            deleteSet = { id ->
                e = e.copy(sets = e.sets.filter { it.id != id }.toImmutableList())
            },
            showInfo = {},
            idSetWithRunningChronometer = currentIdSetWithRunningSet,
            updateIdSetWithRunningChronometer = { currentIdSetWithRunningSet = it },
            workout = true,
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