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

package org.librefit.ui.screens.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.enums.pages.TutorialContent
import org.librefit.nav.Route
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.theme.LibreFitTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    tutorialContent: TutorialContent = TutorialContent.CREATE_ROUTINE,
    fromWelcomeScreen: Boolean = false,
    navController: NavHostController
) {
    val coroutine = rememberCoroutineScope()

    val haptic = LocalHapticFeedback.current

    /**
     * IMPORTANT: Keep in sync with the list below and [TutorialContent]
     */
    val headlinesIndexes = remember {
        setOf(0, 3)
    }

    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = tutorialContent.lazyColumnIndex.takeIf { it in headlinesIndexes }
            ?: error("Invalid initial index: ${tutorialContent.lazyColumnIndex}. Possible indexes: $headlinesIndexes")
    )

    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.tutorial)),
        navigateBack = navController::navigateUp,
        actions = if (fromWelcomeScreen) listOf {
            navController.navigate(Route.MainScreen) {
                launchSingleTop = true
                // popUpTo(Route.WelcomeScreen) { inclusive = true }
            }
        } else emptyList(),
        actionsDescription = listOf(stringResource(R.string.done))
    ) { innerPadding ->
        LibreFitLazyColumn(
            innerPadding = innerPadding,
            lazyListState = lazyListState
        ) {
            item {
                HeadlineText(stringResource(R.string.create_routine))
            }
            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLargeIncreased) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.tut_create_routine_headline))
                    }
                }
            }
            item {
                val pages = remember { 5 }
                val pagerState = rememberPagerState(
                    pageCount = { pages },
                    initialPage = tutorialContent.takeIf { it == TutorialContent.CREATE_ROUTINE }?.pageIndex
                        ?: 0
                )

                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(start = 30.dp, end = 30.dp),
                    pageSpacing = 20.dp,
                    beyondViewportPageCount = 5
                ) { page ->
                    Card(
                        shape = MaterialTheme.shapes.extraLargeIncreased
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.large)
                                    .fillMaxWidth(0.8f),
                                painter = painterResource(
                                    id = when (page) {
                                        0 -> R.drawable.tut_create_routine_1
                                        1 -> R.drawable.tut_create_routine_2
                                        2 -> R.drawable.tut_create_routine_3
                                        3 -> R.drawable.tut_create_routine_4
                                        4 -> R.drawable.tut_create_routine_5
                                        else -> error("Invalid page index: $page. Expected: ${0..pages}")
                                    }
                                ),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth
                            )
                            Text(
                                text = stringResource(
                                    id = when (page) {
                                        0 -> R.string.tut_create_routine_1
                                        1 -> R.string.tut_create_routine_2
                                        2 -> R.string.tut_create_routine_3
                                        3 -> R.string.tut_create_routine_4
                                        4 -> R.string.tut_create_routine_5
                                        else -> error("Invalid page index: $page. Expected: ${0..pages - 1}")
                                    }
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                var sliderPosition by remember { mutableFloatStateOf(pagerState.currentPage.toFloat()) }

                // Update slider when user swipe pages (instead of dragging the slider) and perform haptic feedback
                LaunchedEffect(pagerState.currentPage) {
                    // The if statement avoids execution during first composition and when slider already is in the correct position
                    if (sliderPosition != pagerState.currentPage.toFloat()) {
                        sliderPosition = pagerState.currentPage.toFloat()
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    }
                }

                ElevatedCard(
                    shape = MaterialTheme.shapes.extraLargeIncreased
                ) {
                    Row(
                        modifier = Modifier.padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(stringResource(R.string.step) + ": ${pagerState.currentPage + 1}/${pagerState.pageCount}")
                        Slider(
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it },
                            valueRange = 0f..(pagerState.pageCount.toFloat() - 1),
                            steps = pagerState.pageCount - 2,
                            onValueChangeFinished = {
                                coroutine.launch {
                                    pagerState.animateScrollToPage(
                                        page = sliderPosition.roundToInt(),
                                    )
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            }
                        )
                    }
                }
            }
            item {
                HeadlineText(stringResource(R.string.complete_workout))
            }
            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLargeIncreased) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.tut_complete_workout_headline))
                    }
                }
            }
            item {
                val pages = remember { 6 }
                val pagerState = rememberPagerState(
                    pageCount = { pages },
                    initialPage = tutorialContent.takeIf { it == TutorialContent.COMPLETE_WORKOUT }?.pageIndex
                        ?: 0
                )

                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(start = 30.dp, end = 30.dp),
                    pageSpacing = 20.dp,
                    beyondViewportPageCount = 5
                ) { page ->
                    Card(
                        shape = MaterialTheme.shapes.extraLargeIncreased
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.large)
                                    .fillMaxWidth(0.8f),
                                painter = painterResource(
                                    id = when (page) {
                                        0 -> R.drawable.tut_complete_workout_1
                                        1 -> R.drawable.tut_complete_workout_2
                                        2 -> R.drawable.tut_complete_workout_3
                                        3 -> R.drawable.tut_complete_workout_4
                                        4 -> R.drawable.tut_complete_workout_5
                                        5 -> R.drawable.tut_complete_workout_6
                                        else -> error("Invalid page index: $page. Expected: ${0..pages - 1}")
                                    }
                                ),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth
                            )
                            Text(
                                text = stringResource(
                                    id = when (page) {
                                        0 -> R.string.tut_complete_workout_1
                                        1 -> R.string.tut_complete_workout_2
                                        2 -> R.string.tut_complete_workout_3
                                        3 -> R.string.tut_complete_workout_4
                                        4 -> R.string.tut_complete_workout_5
                                        5 -> R.string.tut_complete_workout_6
                                        else -> error("Invalid page index: $page. Expected: ${0..pages}")
                                    }
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                var sliderPosition by remember { mutableFloatStateOf(pagerState.currentPage.toFloat()) }

                // Update slider when user swipe pages (instead of dragging the slider) and perform haptic feedback
                LaunchedEffect(pagerState.currentPage) {
                    // The if statement is used to avoid execution when slider already is in the correct position
                    if (sliderPosition != pagerState.currentPage.toFloat()) {
                        sliderPosition = pagerState.currentPage.toFloat()
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    }
                }

                ElevatedCard(
                    shape = MaterialTheme.shapes.extraLargeIncreased
                ) {
                    Row(
                        modifier = Modifier.padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(stringResource(R.string.step) + ": ${pagerState.currentPage + 1}/${pagerState.pageCount}")
                        Slider(
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it },
                            valueRange = 0f..(pagerState.pageCount.toFloat() - 1),
                            steps = pagerState.pageCount - 2,
                            onValueChangeFinished = {
                                coroutine.launch {
                                    pagerState.animateScrollToPage(
                                        page = sliderPosition.roundToInt(),
                                    )
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(locale = "it")
@Composable
private fun TutorialScreenPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        TutorialScreen(navController = rememberNavController())
    }
}