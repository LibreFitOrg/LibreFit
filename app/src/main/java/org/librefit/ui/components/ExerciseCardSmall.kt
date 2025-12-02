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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.enums.SetMode
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.UiExerciseWithSets
import org.librefit.ui.models.UiSet
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import org.librefit.util.Formatter.formatDetails
import org.librefit.util.Formatter.formatTime

/**
 * This is a smaller version of [ExerciseCard] when actually is a button. It is suitable to only show data of [UiExerciseWithSets]
 * but not to modify it.
 *
 * @param exerciseWithSets A [UiExerciseWithSets] that holds the data
 * @param animatedVisibilityScope Used for image's animation transition
 * @param isRoutine When `false`, the card shows checkboxes of set completion
 * @param onDetail A lambda function triggered when the `Info` icon is clicked or when [ExerciseCardSmall] is clicked.
 * It should open the [org.librefit.ui.screens.infoExercise.InfoExerciseScreen].
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.ExerciseCardSmall(
    exerciseWithSets: UiExerciseWithSets,
    isRoutine: Boolean = false,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onDetail: () -> Unit
) {
    Button(
        onClick = onDetail,
        modifier = Modifier
            .padding(5.dp),
        shapes = ButtonDefaults.shapes(
            shape = MaterialTheme.shapes.extraLarge
        ),
        contentPadding = ButtonDefaults.MediumContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
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
                    modifier = Modifier.weight(1f),
                    text = exerciseWithSets.exerciseDC.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
                IconButton(
                    onClick = onDetail
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info),
                        contentDescription = stringResource(R.string.info)
                    )
                }
            }
            HorizontalDivider()

            Text(
                text = formatDetails(
                    stringResource(R.string.type_of_set),
                    stringResource(
                        Formatter.setModeToStringId(exerciseWithSets.exercise.setMode)
                    )
                )
            )

            if (exerciseWithSets.exercise.restTime != 0) {
                Text(
                    formatDetails(
                        stringResource(R.string.rest_time),
                        exerciseWithSets.exercise.restTime.toString()
                                + " " + stringResource(R.string.seconds).replaceFirstChar { it.lowercase() })
                )
            }

            if (exerciseWithSets.exercise.notes.isNotBlank()) {
                HorizontalDivider()

                Text(formatDetails(stringResource(R.string.notes), exerciseWithSets.exercise.notes))
            }


            if (exerciseWithSets.sets.isNotEmpty()) {
                val setMode = exerciseWithSets.exercise.setMode
                ElevatedCard(
                    shape = MaterialTheme.shapes.largeIncreased,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(stringResource(R.string.set))
                        if (setMode == SetMode.DURATION) {
                            Text(stringResource(R.string.time))
                        } else {
                            if (setMode == SetMode.LOAD || setMode == SetMode.BODYWEIGHT_WITH_LOAD) {
                                Text(
                                    stringResource(R.string.load) + " (" + stringResource(
                                        R.string.kg
                                    ) + ")"
                                )
                            }
                            Text(stringResource(R.string.reps))
                        }
                        if (!isRoutine) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = stringResource(R.string.done)
                            )
                        }
                    }
                    Column {
                        exerciseWithSets.sets.forEachIndexed { index, set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = CornerSize(if (index == 0) 45 else 0),
                                            topEnd = CornerSize(if (index == 0) 45 else 0),
                                            bottomEnd = CornerSize(
                                                if (index == exerciseWithSets.sets.lastIndex) 45 else 0
                                            ),
                                            bottomStart = CornerSize(
                                                if (index == exerciseWithSets.sets.lastIndex) 45 else 0
                                            ),
                                        )
                                    )
                                    .background(
                                        if (!isRoutine && set.completed) MaterialTheme.colorScheme.tertiaryContainer
                                        else Color.Unspecified
                                    )
                                    .padding(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text("${index + 1}")
                                if (setMode == SetMode.DURATION) {
                                    Text(formatTime(set.elapsedTime).substring(3))
                                } else {
                                    if (setMode == SetMode.LOAD || setMode == SetMode.BODYWEIGHT_WITH_LOAD) {
                                        Text("${set.load}")
                                    }
                                    Text("${set.reps}")
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
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun ExerciseCardSmallPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                ExerciseCardSmall(
                    exerciseWithSets = UiExerciseWithSets(
                        exercise = UiExercise(
                            notes = "Notes",
                            restTime = 100,
                            setMode = SetMode.BODYWEIGHT
                        ),
                        exerciseDC = UiExerciseDC(
                            name = "Name exercise long long long long",
                            images = persistentListOf("3_4_Sit-Up/0.jpg")
                        ),
                        sets = persistentListOf(UiSet(completed = true), UiSet(reps = 10), UiSet())
                    ),
                    animatedVisibilityScope = this
                ) { }
            }
        }
    }
}