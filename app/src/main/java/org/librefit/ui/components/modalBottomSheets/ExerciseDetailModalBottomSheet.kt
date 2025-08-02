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

package org.librefit.ui.components.modalBottomSheets

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import org.librefit.R
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailModalBottomSheet(
    exercise: UiExerciseDC,
    onDismiss: () -> Unit
) {

    //TODO: move all logic in a separate screen in order to show past performance
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
            }
            item { HorizontalDivider() }

            item { AlternatingImages(exercise = exercise) }

            item { HorizontalDivider() }

            item { HeadlineText(text = stringResource(id = R.string.details)) }

            if (exercise.force != null) {
                item {
                    Text(
                        Formatter.formatDetails(
                            stringResource(R.string.force),
                            stringResource(Formatter.exerciseEnumToStringId(exercise.force))
                        )
                    )
                }
            }
            item {
                Text(
                    Formatter.formatDetails(
                        stringResource(R.string.level),
                        stringResource(Formatter.exerciseEnumToStringId(exercise.level))
                    )
                )
            }
            if (exercise.mechanic != null) {
                item {
                    Text(
                        Formatter.formatDetails(
                            stringResource(R.string.mechanic),
                            stringResource(Formatter.exerciseEnumToStringId(exercise.mechanic))
                        )
                    )
                }
            }
            if (exercise.equipment != null) {
                item {
                    Text(
                        Formatter.formatDetails(
                            stringResource(R.string.equipment),
                            stringResource(Formatter.exerciseEnumToStringId(exercise.equipment))
                        )
                    )
                }
            }
            item {
                Text(
                    Formatter.formatDetails(
                        stringResource(R.string.category),
                        stringResource(Formatter.exerciseEnumToStringId(exercise.category))
                    )
                )
            }
            if (exercise.primaryMuscles.isNotEmpty() || exercise.secondaryMuscles.isNotEmpty()) {
                item { HorizontalDivider() }
                item { HeadlineText(text = stringResource(id = R.string.muscles)) }
            }

            if (exercise.primaryMuscles.isNotEmpty()) {
                item {
                    MusclesSection(
                        musclesText = stringResource(id = R.string.primary_muscles),
                        musclesList = exercise.primaryMuscles
                    )
                }
            }


            if (exercise.secondaryMuscles.isNotEmpty()) {
                item {
                    MusclesSection(
                        musclesText = stringResource(id = R.string.secondary_muscles),
                        musclesList = exercise.secondaryMuscles
                    )
                }
            }

            item { HorizontalDivider() }

            item { HeadlineText(text = stringResource(id = R.string.instructions)) }

            item {
                Text(
                    text = exercise.instructions.mapIndexed { index, instruction ->
                        "${index + 1}. $instruction"
                    }.joinToString("\n\n")
                )
            }

            bottomMargin()
        }
    }
}

@Composable
private fun MusclesSection(musclesText: String, musclesList: List<Muscle>) {
    val context = LocalContext.current

    Text(
        text = Formatter.formatDetails(
            boldText = musclesText,
            text = musclesList.joinToString(separator = ", ") {
                context.getString(Formatter.exerciseEnumToStringId(it))
            }
        )
    )

    LazyRow {
        items(musclesList) { muscle ->
            Image(
                imageVector = ImageVector.vectorResource(id = Formatter.muscleToVectorId(muscle)),
                contentDescription = stringResource(Formatter.exerciseEnumToStringId(muscle)),
                modifier = Modifier.size(150.dp)
            )
        }
    }
}

@Composable
private fun AlternatingImages(exercise: UiExerciseDC) {
    val firstBitmap =
        BitmapFactory.decodeStream(LocalContext.current.assets.open(exercise.images[0]))
    val secondBitmap =
        BitmapFactory.decodeStream(LocalContext.current.assets.open(exercise.images[1]))

    var currentBitmap by rememberSaveable { mutableStateOf(firstBitmap) }

    var isPaused by rememberSaveable { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        var i = 0
        while (true) {
            delay(1000)
            if (!isPaused) {
                i++
                currentBitmap = if (i % 2 == 0) firstBitmap else secondBitmap
            }
        }
    }

    currentBitmap?.let {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .fillMaxWidth()
            )
            FilledIconButton(
                modifier = Modifier.padding(5.dp),
                onClick = { isPaused = !isPaused }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (isPaused) R.drawable.ic_play_arrow else R.drawable.ic_pause
                    ),
                    contentDescription = stringResource(if (isPaused) R.string.pause else R.string.resume),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ExerciseDetailModalBottomSheetPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        var openModalBottomSheet by remember { mutableStateOf(false) }

        if (openModalBottomSheet) {
            ExerciseDetailModalBottomSheet(
                exercise = UiExerciseDC(
                    name = "3_4_Sit-Up",
                    force = Force.PULL,
                    level = Level.BEGINNER,
                    mechanic = Mechanic.COMPOUND,
                    equipment = Equipment.BODY_ONLY,
                    primaryMuscles = persistentListOf(Muscle.ABDOMINALS),
                    instructions = persistentListOf(
                        "Lie down on the floor and secure your feet. Your legs should be bent at the knees.",
                        "Place your hands behind or to the side of your head. You will begin with your back on the ground. This will be your starting position.",
                        "Flex your hips and spine to raise your torso toward your knees.",
                        "At the top of the contraction your torso should be perpendicular to the ground. Reverse the motion, going only ¾ of the way down.",
                        "Repeat for the recommended amount of repetitions."
                    ),
                    category = Category.STRENGTH,
                    images = persistentListOf("3_4_Sit-Up/0.jpg", "3_4_Sit-Up/1.jpg")
                )
            ) { openModalBottomSheet = false }
        }
        LibreFitScaffold {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    LibreFitButton(
                        text = "Open modal bottom sheet",
                        icon = ImageVector.vectorResource(R.drawable.ic_add)
                    ) {
                        openModalBottomSheet = true
                    }
                }
            }
        }

    }
}
