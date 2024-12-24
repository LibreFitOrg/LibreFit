/*
 * Copyright (c) 2024. LibreFit
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

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.librefit.R
import org.librefit.db.Set
import org.librefit.enums.Category
import org.librefit.enums.Level
import org.librefit.enums.SetMode
import org.librefit.util.ExerciseDC
import org.librefit.util.ExerciseWithSets
import java.util.Locale
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
 * the [ExerciseDetailModalBottomSheet].
 * @param onDelete A lambda function executed when the [Delete] icon is clicked, it should result in
 * the removal of the card.
 * @param updateSet A function to update a specific set. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateSet] and
 * [org.librefit.ui.screens.createRoutine.CreateRoutineScreenViewModel.updateSet].
 * @param deleteSet A function called when the user swipes the card to remove it.
 * @param updateExercise A function to update the exercise details. For further information,
 * see [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateExercise] and
 * [org.librefit.ui.screens.createRoutine.CreateRoutineScreenViewModel.updateExercise].
 * @param workout A Boolean flag indicating whether a checkbox should be displayed next to each set.
 * This should be set to `true` when the card is used in [org.librefit.ui.screens.workout.WorkoutScreen];
 * otherwise, it should be `false`.
 */
@Composable
fun ExerciseCard(
    modifier: Modifier,
    exerciseWithSets: ExerciseWithSets,
    addSet: () -> Unit,
    onDetail: () -> Unit,
    onDelete: () -> Unit,
    updateSet: (Set, Int, Int) -> Unit,
    deleteSet: (Set) -> Unit,
    updateExercise: (String, Int) -> Unit,
    workout: Boolean = false
) {
    Log.d("ExerciseCard", "Recomposition of card id: " + exerciseWithSets.id)

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
            Text(
                stringResource(R.string.rest_time) + ": " + restTime
                    + " " + stringResource(R.string.seconds).replaceFirstChar { it.lowercase() })
            Slider(
                value = restTime.toFloat(),
                onValueChange = { restTime = it.roundToInt() },
                onValueChangeFinished = {
                    updateExercise(
                        restTime.toString(),
                        2
                    )
                },
                valueRange = 0f..300f,
                steps = 19
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Set mode selection
            //TODO: add info icon to explain in detail
            Text(stringResource(R.string.set_mode))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleChoiceSegmentedButtonRow {
                    SetMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = exerciseWithSets.setMode == mode,
                            onClick = {
                                updateExercise(mode.name, 1)
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = SetMode.entries.size
                            )
                        ) {
                            Text(
                                text = stringResource(setModeToStringId(mode)),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
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
                itemsIndexed(exerciseWithSets.sets) { i, set ->
                    key(set.id) {
                        var timeValue by remember {
                            mutableStateOf(
                                set.elapsedTime.toString().padStart(4, '0')
                            )
                        }

                        var repValue by remember { mutableStateOf(set.reps.toString()) }
                        var weightValue by remember { mutableStateOf(set.weight.toString()) }
                        var timeError by remember { mutableStateOf(false) }
                        var repError by remember { mutableStateOf(false) }
                        var weightError by remember { mutableStateOf(false) }

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
                                    //TODO: a play button to measure time in the text field below
                                    //Time
                                    OutlinedTextField(
                                        modifier = Modifier.width(80.dp),
                                        value = String.format(
                                            Locale.getDefault(),
                                            "%s:%s",
                                            timeValue.substring(0, 2),
                                            timeValue.substring(2, 4)
                                        ),
                                        onValueChange = { string ->
                                            //It removes the double dots and the leading zeros
                                            val value = string.filter { it != ':' }
                                                .replace("^0+".toRegex(), "")

                                            if (value.all { it.isDigit() }) {
                                                if (value.length > 4) {
                                                    timeError = true
                                                } else {
                                                    timeError = false
                                                    timeValue = value.padStart(4, '0')
                                                    updateSet(
                                                        set,
                                                        timeValue.toInt(),
                                                        2
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        isError = timeError,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    )
                                } else {
                                    //Reps
                                    OutlinedTextField(
                                        modifier = Modifier.width(80.dp),
                                        value = repValue,
                                        onValueChange = { string ->
                                            if (string.all { it.isDigit() }) {
                                                if (string.length > 4) {
                                                    repError = true
                                                } else {
                                                    repError = false
                                                    repValue = string
                                                    updateSet(
                                                        set,
                                                        repValue.ifEmpty { "0" }.toInt(),
                                                        1
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        isError = repError,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    )
                                    if (exerciseWithSets.setMode == SetMode.WEIGHT) {
                                        //Weight
                                        OutlinedTextField(
                                            modifier = Modifier.width(80.dp),
                                            value = weightValue,
                                            onValueChange = { string ->
                                                if (string.all { it.isDigit() }) {
                                                    if (string.length > 4) {
                                                        weightError = true
                                                    } else {
                                                        weightValue = string
                                                        weightError = false
                                                        updateSet(
                                                            set,
                                                            weightValue.ifEmpty { "0" }.toInt(),
                                                            0
                                                        )
                                                    }
                                                }
                                            },
                                            singleLine = true,
                                            isError = weightError,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                    }
                                }

                                if (workout) {
                                    Checkbox(
                                        checked = set.completed,
                                        onCheckedChange = { checked ->
                                            updateSet(
                                                set,
                                                if (checked) 1 else 0,
                                                3
                                            )
                                        }
                                    )
                                }
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
            sets = listOf(Set())
        ),
        {},
        {},
        {},
        { i, j, k -> },
        {},
        { i, j -> },
        false
    )
}