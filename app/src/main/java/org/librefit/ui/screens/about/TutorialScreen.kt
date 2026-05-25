/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.enums.pages.TutorialContent
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.nav.Route
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.rememberDrawableAspectRatio
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
        initialFirstVisibleItemIndex = checkNotNull(tutorialContent.lazyColumnIndex.takeIf { it in headlinesIndexes }) {
            "Invalid initial index: ${tutorialContent.lazyColumnIndex}. Possible indexes: $headlinesIndexes"
        }
    )

    val navigateBack: (() -> Unit)? = if (fromWelcomeScreen) null else navController::navigateUp


    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.tutorial)),
        navigateBack = navigateBack,
        actions = if (fromWelcomeScreen) persistentListOf({
            navController.navigate(Route.MainScreen) {
                launchSingleTop = true
                popUpTo(Route.TutorialScreen()) { inclusive = true }
            }
        }) else persistentListOf(),
        actionsDescription = persistentListOf(stringResource(R.string.done))
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
                            AsyncImage(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.large)
                                    .fillMaxWidth(0.8f)
                                    .aspectRatio(
                                        rememberDrawableAspectRatio(
                                            when (page) {
                                                0 -> R.drawable.tut_create_routine_1
                                                1 -> R.drawable.tut_create_routine_2
                                                2 -> R.drawable.tut_create_routine_3
                                                3 -> R.drawable.tut_create_routine_4
                                                4 -> R.drawable.tut_create_routine_5
                                                else -> error("Invalid page index: $page. Expected: ${0..pages}")
                                            }
                                        )
                                    ),
                                model = when (page) {
                                    0 -> R.drawable.tut_create_routine_1
                                    1 -> R.drawable.tut_create_routine_2
                                    2 -> R.drawable.tut_create_routine_3
                                    3 -> R.drawable.tut_create_routine_4
                                    4 -> R.drawable.tut_create_routine_5
                                    else -> error("Invalid page index: $page. Expected: ${0..pages}")
                                },
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
                                        else -> error("Invalid page index: $page. Expected: ${0..<pages}")
                                    }
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                var sliderPosition by rememberSaveable { mutableIntStateOf(pagerState.currentPage) }

                // Update slider when user swipe pages (instead of dragging the slider) and perform haptic feedback
                LaunchedEffect(pagerState.targetPage) {
                    // The if statement avoids execution during first composition and when slider already is in the correct position
                    if (sliderPosition != pagerState.targetPage) {
                        sliderPosition = pagerState.targetPage
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
                            value = sliderPosition.toFloat(),
                            onValueChange = {
                                sliderPosition = it.roundToInt()
                                if (it.roundToInt() != pagerState.targetPage) {
                                    coroutine.launch {
                                        pagerState.animateScrollToPage(
                                            page = it.roundToInt(),
                                        )
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                }
                            },
                            valueRange = 0f..(pagerState.pageCount.toFloat() - 1),
                            steps = pagerState.pageCount - 2,
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
                    beyondViewportPageCount = 6
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
                            AsyncImage(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.large)
                                    .fillMaxWidth(0.8f)
                                    .aspectRatio(
                                        rememberDrawableAspectRatio(
                                            when (page) {
                                                0 -> R.drawable.tut_complete_workout_1
                                                1 -> R.drawable.tut_complete_workout_2
                                                2 -> R.drawable.tut_complete_workout_3
                                                3 -> R.drawable.tut_complete_workout_4
                                                4 -> R.drawable.tut_complete_workout_5
                                                5 -> R.drawable.tut_complete_workout_6
                                                else -> error("Invalid page index: $page. Expected: ${0..<pages}")
                                            }
                                        )
                                    ),
                                model = when (page) {
                                    0 -> R.drawable.tut_complete_workout_1
                                    1 -> R.drawable.tut_complete_workout_2
                                    2 -> R.drawable.tut_complete_workout_3
                                    3 -> R.drawable.tut_complete_workout_4
                                    4 -> R.drawable.tut_complete_workout_5
                                    5 -> R.drawable.tut_complete_workout_6
                                    else -> error("Invalid page index: $page. Expected: ${0..pages - 1}")
                                },
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

                var sliderPosition by rememberSaveable { mutableIntStateOf(pagerState.currentPage) }

                // Update slider when user swipe pages (instead of dragging the slider) and perform haptic feedback
                LaunchedEffect(pagerState.targetPage) {
                    // The if statement avoids execution during first composition and when slider already is in the correct position
                    if (sliderPosition != pagerState.targetPage) {
                        sliderPosition = pagerState.targetPage
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
                            value = sliderPosition.toFloat(),
                            onValueChange = {
                                sliderPosition = it.roundToInt()
                                if (it.roundToInt() != pagerState.targetPage) {
                                    coroutine.launch {
                                        pagerState.animateScrollToPage(
                                            page = it.roundToInt(),
                                        )
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                }
                            },
                            valueRange = 0f..(pagerState.pageCount.toFloat() - 1),
                            steps = pagerState.pageCount - 2
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
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        TutorialScreen(navController = rememberNavController())
    }
}