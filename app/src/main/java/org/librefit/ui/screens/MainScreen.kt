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

package org.librefit.ui.screens


import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.enums.pages.MainScreenPages
import org.librefit.nav.Route
import org.librefit.ui.components.LibreFitAppName.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.screens.home.HomeScreen
import org.librefit.ui.screens.library.LibraryScreen
import org.librefit.ui.screens.profile.ProfileScreen


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.MainScreen(
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val fabAction: () -> Unit = remember {
        {
            navController.navigate(Route.EditWorkoutScreen(0L)) {
                launchSingleTop = true
            }
        }
    }

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
        actions = listOf(
            { navController.navigate(Route.AboutScreen) { launchSingleTop = true } },
            { navController.navigate(Route.SettingsScreen) { launchSingleTop = true } }
        ),
        actionsIcons = listOf(
            painterResource(R.drawable.ic_info),
            painterResource(R.drawable.ic_settings)
        ),
        actionsElevated = listOf(false, false),
        fabAction = if (pagerState.currentPage == MainScreenPages.HOME.ordinal) fabAction else null,
        fabIcon = painterResource(R.drawable.ic_add),
        fabDescription = stringResource(R.string.create_routine),
        bottomBar = {
            NavigationBar {
                MainScreenPages.entries.forEach { page ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == page.ordinal,
                        onClick = { goToPage(page.ordinal) },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = when (page) {
                                        MainScreenPages.LIBRARY -> R.drawable.ic_library
                                        MainScreenPages.HOME -> R.drawable.ic_home
                                        MainScreenPages.PROFILE -> R.drawable.ic_person
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
            beyondViewportPageCount = 1
        ) { pageIndex ->
            when (pageIndex) {
                0 -> LibraryScreen(innerPadding)
                1 -> HomeScreen(innerPadding, navController, animatedVisibilityScope)
                2 -> ProfileScreen(innerPadding, navController, animatedVisibilityScope)
                else -> error("Invalid page index in main screen: $pageIndex. Number of pages: ${pagerState.pageCount}")
            }
        }
    }
}