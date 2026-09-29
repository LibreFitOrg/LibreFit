/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens


import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.enums.pages.MainScreenPages
import org.librefit.ui.components.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.screens.home.HomeScreen
import org.librefit.ui.screens.library.LibraryScreen
import org.librefit.ui.screens.profile.ProfileScreen


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.MainScreen(
    onNavigateToSupportScreen: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToSettingsScreen: () -> Unit,
    onNavigateToEditWorkout: () -> Unit,
    // Home page navigation
    onNavigateToInfoWorkout: (Long) -> Unit,
    onNavigateToRequestPermissionScreen: (Long) -> Unit,
    onNavigateToWorkout: (Long) -> Unit,
    onNavigateToTutorialScreen: () -> Unit,
    // Profile page navigation
    onNavigateToCompleteWorkoutTutorial: () -> Unit,
    onNavigateToExercisesScreen: () -> Unit,
    onNavigateToStatisticsScreen: () -> Unit,
    onNavigateToMeasurementsScreen: () -> Unit,
    onNavigateToCalendarScreen: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {

    val pagerState = rememberPagerState(
        initialPage = MainScreenPages.HOME.ordinal,
        pageCount = { MainScreenPages.entries.size }
    )

    val coroutine = rememberCoroutineScope()

    val goToPage: (Int) -> Unit = remember {
        { pageIndex ->
            coroutine.launch {
                pagerState.animateScrollToPage(pageIndex)
            }
        }
    }


    LibreFitScaffold(
        title = buildAnnotatedString {
            GetAppNameInAnnotatedBuilder(MaterialTheme.typography.titleLargeEmphasized)
        },
        actions = persistentListOf(
            onNavigateToSupportScreen,
            onNavigateToAboutScreen,
            onNavigateToSettingsScreen
        ),
        actionsIcons = persistentListOf(
            painterResource(R.drawable.ic_favorite),
            painterResource(R.drawable.ic_info),
            painterResource(R.drawable.ic_settings)
        ),
        actionsElevated = persistentListOf(true, false, false),
        fabAction = if (pagerState.currentPage == MainScreenPages.HOME.ordinal) {
            onNavigateToEditWorkout
        } else null,
        fabIcon = painterResource(R.drawable.ic_add),
        fabDescription = stringResource(R.string.create_routine),
        fabText = stringResource(R.string.create_routine),
        bottomBar = {
            ShortNavigationBar {
                MainScreenPages.entries.forEach { page ->
                    val selected = pagerState.currentPage == page.ordinal
                    ShortNavigationBarItem(
                        selected = selected,
                        onClick = { goToPage(page.ordinal) },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = when (page) {
                                        MainScreenPages.LIBRARY -> if (selected) R.drawable.ic_library_filled else R.drawable.ic_library
                                        MainScreenPages.HOME -> if (selected) R.drawable.ic_home_filled else R.drawable.ic_home
                                        MainScreenPages.PROFILE -> if (selected) R.drawable.ic_person_filled else R.drawable.ic_person
                                    }
                                ),
                                contentDescription = stringResource(
                                    id = when (page) {
                                        MainScreenPages.LIBRARY -> R.string.library
                                        MainScreenPages.HOME -> R.string.home
                                        MainScreenPages.PROFILE -> R.string.profile
                                    }
                                )
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(
                                    id = when (page) {
                                        MainScreenPages.LIBRARY -> R.string.library
                                        MainScreenPages.HOME -> R.string.home
                                        MainScreenPages.PROFILE -> R.string.profile
                                    }
                                )
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            contentPadding = innerPadding
        ) { pageIndex ->
            when (pageIndex) {
                0 -> LibraryScreen()
                1 -> HomeScreen(
                    onNavigateToInfoWorkout = onNavigateToInfoWorkout,
                    onNavigateToRequestPermissionScreen = onNavigateToRequestPermissionScreen,
                    onNavigateToWorkout = onNavigateToWorkout,
                    onNavigateToTutorialScreen = onNavigateToTutorialScreen,
                    animatedVisibilityScope = animatedVisibilityScope
                )

                2 -> ProfileScreen(
                    onNavigateToExercisesScreen = onNavigateToExercisesScreen,
                    onNavigateToStatisticsScreen = onNavigateToStatisticsScreen,
                    onNavigateToMeasurementsScreen = onNavigateToMeasurementsScreen,
                    onNavigateToCalendarScreen = onNavigateToCalendarScreen,
                    onNavigateToInfoWorkout = onNavigateToInfoWorkout,
                    onNavigateToTutorialScreen = onNavigateToCompleteWorkoutTutorial,
                    animatedVisibilityScope = animatedVisibilityScope
                )
                else -> error("Invalid page index in main screen: $pageIndex. Number of pages: ${pagerState.pageCount}")
            }
        }
    }
}