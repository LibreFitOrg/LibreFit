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

package org.librefit.ui.screens.infoExercise

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.db.entity.ExerciseDC
import org.librefit.enums.InfoExerciseMode
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.mappers.toUi
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.InfoExerciseScreen(
    id: Long,
    exerciseDC: ExerciseDC,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navController: NavHostController
) {
    InfoExerciseContent(
        id = id,
        exercise = exerciseDC.toUi(),
        animatedVisibilityScope = animatedVisibilityScope,
        navigateBack = navController::popBackStack
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.InfoExerciseContent(
    id: Long,
    exercise: UiExerciseDC,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navigateBack: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { InfoExerciseMode.entries.size })
    val coroutineScope = rememberCoroutineScope()

    val stringId = if (id == 0L) "" else id.toString()

    LibreFitScaffold(
        title = AnnotatedString(exercise.name),
        navigateBack = navigateBack
    ) { innerPadding ->
        LibreFitLazyColumn(
            innerPadding = innerPadding,
            startEndPadding = 0.dp
        ) {
            item {
                AlternatingImages(stringId, exercise, animatedVisibilityScope)
            }
            item {
                PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                    InfoExerciseMode.entries.forEachIndexed { i, enum ->
                        Tab(
                            selected = pagerState.currentPage == i,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(i)
                                }
                            },
                            text = {
                                Text(
                                    text = stringResource(
                                        id = when (enum) {
                                            InfoExerciseMode.DETAILS -> R.string.details
                                            InfoExerciseMode.INSTRUCTIONS -> R.string.instructions
                                            InfoExerciseMode.HISTORY -> R.string.history
                                        }
                                    )
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(
                                        id = when (enum) {
                                            InfoExerciseMode.DETAILS -> R.drawable.ic_badge
                                            InfoExerciseMode.INSTRUCTIONS -> R.drawable.ic_reference
                                            InfoExerciseMode.HISTORY -> R.drawable.ic_history
                                        }
                                    ),
                                    contentDescription = stringResource(
                                        id = when (enum) {
                                            InfoExerciseMode.DETAILS -> R.string.details
                                            InfoExerciseMode.INSTRUCTIONS -> R.string.instructions
                                            InfoExerciseMode.HISTORY -> R.string.history
                                        }
                                    )
                                )
                            }
                        )
                    }
                }
            }
            item {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(start = 15.dp, end = 15.dp),
                    pageSpacing = 20.dp,
                ) { pageIndex ->
                    val enum = InfoExerciseMode.entries[pageIndex]

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (enum) {
                            InfoExerciseMode.DETAILS -> DetailsPage(exercise)
                            InfoExerciseMode.HISTORY -> HistoryPage()
                            InfoExerciseMode.INSTRUCTIONS -> {
                                Text(
                                    text = buildString {
                                        exercise.instructions.forEachIndexed { index, instruction ->
                                            // For all items except the first, add the separator BEFORE the item.
                                            if (index > 0) {
                                                append("\n\n")
                                            }
                                            append("${index + 1}. $instruction")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsPage(exercise: UiExerciseDC) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (exercise.force != null) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = stringResource(Formatter.exerciseEnumToStringId(exercise.force)),
                label = { Text(stringResource(R.string.force)) },
                onValueChange = {},
                readOnly = true,
                singleLine = true,
            )
        }
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = stringResource(Formatter.exerciseEnumToStringId(exercise.level)),
            label = { Text(stringResource(R.string.level)) },
            onValueChange = {},
            readOnly = true,
            singleLine = true,
        )
        if (exercise.mechanic != null) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = stringResource(Formatter.exerciseEnumToStringId(exercise.mechanic)),
                label = { Text(stringResource(R.string.mechanic)) },
                onValueChange = {},
                readOnly = true,
                singleLine = true,
            )
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (exercise.equipment != null) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = stringResource(Formatter.exerciseEnumToStringId(exercise.equipment)),
                label = { Text(stringResource(R.string.equipment)) },
                onValueChange = {},
                readOnly = true,
                singleLine = true,
            )
        }
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = stringResource(Formatter.exerciseEnumToStringId(exercise.category)),
            label = { Text(stringResource(R.string.category)) },
            onValueChange = {},
            readOnly = true,
            singleLine = true,
        )
    }

    if (exercise.primaryMuscles.isNotEmpty()) {
        HeadlineText(text = stringResource(id = R.string.primary_muscles))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(exercise.primaryMuscles) { muscle ->
                ElevatedCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            imageVector = ImageVector.vectorResource(
                                id = Formatter.muscleToVectorId(
                                    muscle
                                )
                            ),
                            contentDescription = stringResource(
                                Formatter.exerciseEnumToStringId(
                                    muscle
                                )
                            ),
                            modifier = Modifier.size(150.dp)
                        )
                        Spacer(Modifier.height(15.dp))

                        Text(
                            text = stringResource(Formatter.exerciseEnumToStringId(muscle)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (exercise.secondaryMuscles.isNotEmpty()) {
        HeadlineText(text = stringResource(id = R.string.secondary_muscles))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(exercise.secondaryMuscles) { muscle ->
                ElevatedCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            imageVector = ImageVector.vectorResource(
                                id = Formatter.muscleToVectorId(
                                    muscle
                                )
                            ),
                            contentDescription = stringResource(
                                Formatter.exerciseEnumToStringId(
                                    muscle
                                )
                            ),
                            modifier = Modifier.size(150.dp)
                        )
                        Spacer(Modifier.height(15.dp))

                        Text(
                            text = stringResource(Formatter.exerciseEnumToStringId(muscle)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun HistoryPage() {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("TODO")
        }
    }

}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.AlternatingImages(
    stringId: String,
    exercise: UiExerciseDC,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current

    val firstBitmap = remember {
        BitmapFactory.decodeStream(context.assets.open(exercise.images[0]))
    }
    val secondBitmap = remember {
        BitmapFactory.decodeStream(context.assets.open(exercise.images[1]))
    }

    var currentBitmap by remember { mutableStateOf(firstBitmap) }

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

    Box(
        modifier = Modifier
            .padding(15.dp)
            .sharedElement(
                sharedContentState = rememberSharedContentState(stringId + exercise.id),
                animatedVisibilityScope = animatedVisibilityScope
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        Image(
            bitmap = currentBitmap.asImageBitmap(),
            contentDescription = exercise.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun InfoExercisePreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                InfoExerciseContent(
                    id = 0L,
                    exercise = UiExerciseDC(
                        name = "3/4 Sit-Up",
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
                    ),
                    animatedVisibilityScope = this,
                    navigateBack = {}
                )
            }
        }
    }
}