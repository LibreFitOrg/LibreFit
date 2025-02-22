/*
 * Copyright (c) 2025. LibreFit
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.librefit.R
import org.librefit.enums.SetMode
import org.librefit.util.ExerciseWithSets
import org.librefit.util.Formatter.formatDetails
import org.librefit.util.Formatter.formatTime

/**
 * This is a smaller version of [ExerciseCard]. It is suitable to only show data of [ExerciseWithSets]
 * but not to modify it.
 *
 * @param exercise A [ExerciseWithSets] that holds the data
 * @param isRoutine When `false`, the card shows checkboxes of set completion
 * @param onDetail A lambda function triggered when the [Info] icon is clicked, which should open
 * the [org.librefit.ui.components.modalBottomSheets.ExerciseDetailModalBottomSheet].
 */
@Composable
fun ExerciseCardSmall(
    exercise: ExerciseWithSets,
    isRoutine: Boolean = false,
    onDetail: () -> Unit
) {
    ElevatedCard(Modifier.padding(5.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = exercise.exerciseDC.name,
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(
                    onClick = onDetail
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.info)
                    )
                }
            }


            if (exercise.note.isNotBlank()) {
                HorizontalDivider()

                Text(formatDetails(stringResource(R.string.notes), exercise.note))
            }

            if (exercise.restTime != 0) {
                HorizontalDivider()
                Text(
                    formatDetails(
                        stringResource(R.string.rest_time), exercise.restTime.toString()
                                + " " + stringResource(R.string.seconds).replaceFirstChar { it.lowercase() })
                )
            }

            if (exercise.sets.isNotEmpty()) {
                HorizontalDivider()

                val setMode = exercise.setMode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(stringResource(R.string.set))
                    if (setMode == SetMode.TIME) {
                        Text(stringResource(R.string.time))
                    } else {
                        Text(stringResource(R.string.reps))
                        if (setMode == SetMode.WEIGHT) {
                            Text(
                                stringResource(R.string.weight) + "(" + stringResource(
                                    R.string.kg
                                ) + ")"
                            )
                        }
                    }
                    if (!isRoutine) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.done)
                        )
                    }
                }

                Column {
                    exercise.sets.forEachIndexed { index, set ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(
                                    RoundedCornerShape(
                                        topStart = CornerSize(if (index == 0) 25 else 0),
                                        topEnd = CornerSize(if (index == 0) 25 else 0),
                                        bottomEnd = CornerSize(
                                            if (index == exercise.sets.lastIndex) 25 else 0
                                        ),
                                        bottomStart = CornerSize(
                                            if (index == exercise.sets.lastIndex) 25 else 0
                                        ),
                                    )
                                )
                                .background(
                                    if (!isRoutine && set.completed) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow
                                )
                                .padding(5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text("${index + 1}")
                            if (setMode == SetMode.TIME) {
                                Text(formatTime(set.elapsedTime).substring(3))
                            } else {
                                Text("${set.reps}")
                                if (setMode == SetMode.WEIGHT) {
                                    Text("${set.weight}")
                                }
                            }
                            if (!isRoutine) {
                                Checkbox(
                                    checked = set.completed,
                                    onCheckedChange = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}