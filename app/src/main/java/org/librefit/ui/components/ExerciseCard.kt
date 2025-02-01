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

@file:Suppress("KDocUnresolvedReference")

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.db.Set
import org.librefit.enums.Category
import org.librefit.enums.InfoMode
import org.librefit.enums.Level
import org.librefit.enums.SetMode
import org.librefit.util.ExerciseDC
import org.librefit.util.ExerciseWithSets
import org.librefit.util.formatTime
import kotlin.math.roundToInt
import kotlin.text.ifEmpty
import kotlin.text.toInt

/**
 * A custom [ElevatedCard] designed to display an [ExerciseWithSets] with a uniform appearance across
 * the app.
 *
 * @param modifier A [Modifier] that should be passed as [Modifier.animateItem] to enable
 * animation for the card within the list.
 * @param exerciseWithSets An instance of [ExerciseWithSets] containing all the relevant information
 * required for the card display.
 * @param addSet A lambda function invoked when the "Add set" button is clicked.
 * @param onDetail A lambda function triggered when the [Info] icon is clicked, which should open
 * the [org.librefit.ui.components.modalBottomSheets.ExerciseDetailModalBottomSheet].
 * @param onDelete A lambda function executed when the [Delete] icon is clicked, it should result in
 * the removal of the card.
 * @param updateSet A function to update a specific set. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateSet] and
 * [org.librefit.ui.screens.edit.EditScreenViewModel.updateSet].
 * @param deleteSet A function called when the user swipes the set to remove it.
 * @param updateExercise A function to update the exercise details. For further information,
 * see [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateExercise] and
 * [org.librefit.ui.screens.edit.EditScreenViewModel.updateExercise].
 * @param showInfo A lambda function executed when info icon next to "type of set" or "rest time" text
 * is clicked. The passed parameter is used by [org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet] to show the relevant information.
 * @param setChronometerIsRunning This should be passed only from the workout screen (so [workout]
 * must be `true`). It allows only one set timer to be running at once.
 * @param setWithRunningChronometer This should be passed only from the workout screen (so [workout]
 * must be `true`). It allows only one set timer to be running at once.
 * @param workout A Boolean flag indicating whether a checkbox should be displayed next to each set.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCard(
    modifier: Modifier = Modifier,
    exerciseWithSets: ExerciseWithSets,
    addSet: () -> Unit,
    onDetail: () -> Unit,
    onDelete: () -> Unit,
    updateSet: (Set, Float, Int) -> Unit,
    deleteSet: (Set) -> Unit,
    updateExercise: (String, Int) -> Unit,
    showInfo: (InfoMode) -> Unit,
    setChronometerIsRunning: MutableState<Boolean> = mutableStateOf(false),
    setWithRunningChronometer: MutableState<Set> = mutableStateOf(Set()),
    workout: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()

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
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDetail) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.info)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.notes)) },
                value = exerciseWithSets.note,
                onValueChange = {
                    updateExercise(it, 0)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))

            //Rest timer slider
            var restTime by remember { mutableIntStateOf(exerciseWithSets.restTime) }
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
                        imageVector = Icons.Default.Info,
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
                    updateExercise(
                        restTime.toString(),
                        2
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
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.info) + ":"
                        )
                    }
                    Text(stringResource(R.string.type_of_set))
                }


                var expanded by remember { mutableStateOf(false) }

                val focusRequester = remember { FocusRequester() }

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
                    TextField(
                        readOnly = true,
                        value = stringResource(setModeToStringId(exerciseWithSets.setMode)),
                        onValueChange = {},
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        SetMode.entries.forEachIndexed { index, mode ->
                            DropdownMenuItem(
                                onClick = {
                                    updateExercise(mode.name, 1)
                                    expanded = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(setModeToStringId(mode))
                                    )
                                },
                                trailingIcon = if (exerciseWithSets.setMode == mode) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = stringResource(R.string.checkbox)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.background(
                                    if (exerciseWithSets.setMode == mode) MaterialTheme.colorScheme.inversePrimary.copy(
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
                if (exerciseWithSets.setMode == SetMode.TIME) {
                    Text(
                        text = stringResource(R.string.time),
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.reps),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (exerciseWithSets.setMode == SetMode.WEIGHT) {
                        Text(
                            text = stringResource(R.string.weight) + " (" + stringResource(R.string.kg) + ")",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                if (workout) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(R.string.done)
                    )
                }
            }

            //Sets
            val setHeight = 80
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
                    var timeValue by remember { mutableIntStateOf(set.elapsedTime) }
                    var repValue by remember { mutableStateOf(set.reps.toString()) }
                    var weightValue by remember { mutableStateOf(set.weight.toString()) }

                    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            when (it) {
                                SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
                                else -> deleteSet(set)
                            }
                            return@rememberSwipeToDismissBoxState true
                        },
                        positionalThreshold = { it * 0.3f }
                    )

                    SwipeToDismissBox(
                        modifier = Modifier.animateItem(),
                        state = swipeToDismissBoxState,
                        backgroundContent = {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.small)
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
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                )
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                )
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
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

                            if (exerciseWithSets.setMode == SetMode.TIME) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (workout) {
                                        IconButton(
                                            enabled = (!setChronometerIsRunning.value ||
                                                    setWithRunningChronometer.value.id == set.id) &&
                                                    !set.completed,
                                            onClick = {
                                                if (setChronometerIsRunning.value) {
                                                    setChronometerIsRunning.value = false
                                                    setWithRunningChronometer.value = Set()
                                                } else {
                                                    setChronometerIsRunning.value = true
                                                    setWithRunningChronometer.value = set

                                                    coroutineScope.launch {
                                                        val startTime =
                                                            System.currentTimeMillis()

                                                        val pastTime = timeValue

                                                        while (setChronometerIsRunning.value) {
                                                            val currentTime =
                                                                System.currentTimeMillis()

                                                            timeValue =
                                                                (currentTime - startTime)
                                                                    .toInt() / 1000 + pastTime

                                                            updateSet(
                                                                set,
                                                                timeValue.toFloat(),
                                                                2
                                                            )

                                                            delay(1000)
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            val running = setChronometerIsRunning.value ||
                                                    setWithRunningChronometer.value.id == set.id
                                            Icon(
                                                imageVector = if (!running) Icons.Default.PlayArrow
                                                else ImageVector.vectorResource(R.drawable.ic_pause),
                                                contentDescription = if (running)
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
                                            val stringValue = string
                                                .filter { it.isDigit() }
                                                .takeLast(4)

                                            val seconds = stringValue.toInt() % 100
                                            val minutes = (stringValue.toInt() - seconds) / 100

                                            timeValue = minutes * 60 + seconds

                                            updateSet(
                                                set,
                                                timeValue.toFloat(),
                                                2
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    )
                                }
                            } else {
                                //Reps
                                OutlinedTextField(
                                    modifier = Modifier.width(80.dp),
                                    value = repValue,
                                    onValueChange = { string ->
                                        val stringValue = string.filter { it.isDigit() }.takeLast(4)

                                        repValue = stringValue.removePrefix("0")
                                        updateSet(
                                            set,
                                            repValue.ifEmpty { "0" }.toFloat(),
                                            1
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )
                                if (exerciseWithSets.setMode == SetMode.WEIGHT) {
                                    //Weight
                                    OutlinedTextField(
                                        modifier = Modifier.width(80.dp),
                                        value = weightValue,
                                        onValueChange = { string ->
                                            val stringValue = string
                                                .replace(",", ".")
                                                .filter { it.isDigit() || it == '.' }
                                                .takeLast(5)

                                            val firstDotIndex = stringValue.indexOf(".")

                                            if (firstDotIndex == -1) {
                                                weightValue = stringValue
                                            } else {
                                                val beforeFirstDot = stringValue.substring(
                                                    0, firstDotIndex + 1
                                                )

                                                val afterFirstDot = stringValue
                                                    .substring(firstDotIndex + 1)
                                                    .replace(".", "")

                                                weightValue = beforeFirstDot + afterFirstDot
                                            }

                                            if (weightValue == ".") weightValue = "0.0"

                                            updateSet(
                                                set,
                                                weightValue.ifEmpty { "0" }.toFloat(),
                                                0
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }

                            if (workout) {
                                Checkbox(
                                    checked = set.completed,
                                    onCheckedChange = { checked ->
                                        if (setChronometerIsRunning.value) {
                                            setChronometerIsRunning.value = false
                                            setWithRunningChronometer.value = Set()
                                        }
                                        updateSet(
                                            set,
                                            if (checked) 1f else 0f,
                                            3
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))

            //Add set button
            CustomTextButton(
                text = stringResource(id = R.string.add_set),
                icon = Icons.Default.AddCircle,
                onClick = addSet,
                elevated = false
            )
        }
    }
}

private fun setModeToStringId(setMode: SetMode): Int {
    return when (setMode) {
        SetMode.WEIGHT -> R.string.weight
        SetMode.REPS -> R.string.reps
        SetMode.TIME -> R.string.time
    }
}

@Preview
@Composable
private fun ExerciseCardPreview() {
    val timerRunning = rememberSaveable { mutableStateOf(false) }
    val set = rememberSaveable { mutableStateOf(Set()) }
    ExerciseCard(
        Modifier,
        ExerciseWithSets(
            exerciseDC = ExerciseDC(
                id = "",
                name = "Exercise",
                level = Level.INTERMEDIATE,
                primaryMuscles = emptyList(),
                secondaryMuscles = emptyList(),
                instructions = listOf(""),
                category = Category.POWERLIFTING,
                images = listOf("")
            ),
            setMode = SetMode.TIME,
            sets = listOf(Set(elapsedTime = 120)),
            restTime = 120
        ),
        {},
        {},
        {},
        { i, j, k -> },
        {},
        { i, j -> },
        {},
        timerRunning,
        set,
        workout = true
    )
}