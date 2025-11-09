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
 *
 * LibreFit is subject to additional terms covering author attribution and
 * trademark usage, as found in the accompanying ADDITIONAL_TERMS.md file.
 */

package org.librefit.ui.screens.home

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.librefit.R
import org.librefit.enums.pages.MainScreenPages
import org.librefit.nav.Route
import org.librefit.ui.components.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.theme.LibreFitTheme

@OptIn(ExperimentalPermissionsApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel: HomeScreenViewModel = hiltViewModel()


    val requestPermissionNextTime by viewModel.requestPermissionNextTime.collectAsStateWithLifecycle()

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        //Permission granted by default below Tiramisu
        null
    }

    val routines by viewModel.routines.collectAsStateWithLifecycle()

    HomeScreenContent(
        innerPadding = innerPadding,
        navController = navController,
        routines = routines,
        animatedVisibilityScope = animatedVisibilityScope,
        navigateToRoutine = { workoutId ->
            val hasNotificationPermission = notificationPermissionState?.status?.isGranted != false

            val requestPermission = !hasNotificationPermission && requestPermissionNextTime

            if (requestPermission) {
                navController.navigate(Route.RequestPermissionScreen(workoutId = workoutId)) {
                    launchSingleTop = true
                }
            } else {
                navController.navigate(Route.WorkoutScreen(workoutId = workoutId)) {
                    launchSingleTop = true
                    popUpTo(Route.RequestPermissionScreen(workoutId = workoutId)) {
                        inclusive = true
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharedTransitionScope.HomeScreenContent(
    innerPadding: PaddingValues,
    navController: NavHostController,
    routines: List<UiWorkout>,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navigateToRoutine: (Long) -> Unit
) {

    LibreFitLazyColumn(innerPadding) {
        // TODO: implement workout resume
        item {
            //"Start empty workout" button
            LibreFitButton(
                text = stringResource(id = R.string.start_empty_workout),
                icon = painterResource(R.drawable.ic_play_arrow),
                onClick = {
                    navigateToRoutine(0)
                },
            )
        }


        item {
            HeadlineText(stringResource(id = R.string.your_routines))
        }

        if (routines.isEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.start_creating_routine),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = {
                            navController.navigate(Route.TutorialScreen()) {
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_help),
                            contentDescription = stringResource(R.string.help)
                        )
                    }
                }
            }
        }

        items(routines, key = { it.id }) { routine ->
            ElevatedCard(
                onClick = {
                    navController.navigate(Route.InfoWorkoutScreen(workoutId = routine.id)) {
                        launchSingleTop = true
                    }
                },
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(routine.id),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = routine.title,
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    routine.id.toString() + routine.title
                                ),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                        IconButton(
                            onClick = {
                                navController.navigate(Route.InfoWorkoutScreen(workoutId = routine.id)) {
                                    launchSingleTop = true
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.info)
                            )
                        }
                    }
                    LibreFitButton(
                        text = stringResource(R.string.start_routine),
                        icon = painterResource(R.drawable.ic_play_arrow),
                        elevated = false
                    ) {
                        navigateToRoutine(routine.id)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun HomeScreenPreview() {
    val pagerState = rememberPagerState(
        initialPage = MainScreenPages.HOME.ordinal,
        pageCount = { MainScreenPages.entries.size }
    )

    LibreFitTheme(dynamicColor = false, darkTheme = true) {

        LibreFitScaffold(
            title = buildAnnotatedString {
                GetAppNameInAnnotatedBuilder(MaterialTheme.typography.titleLargeEmphasized)
            },
            actions = listOf({ }, { }, { }),
            actionsIcons = listOf(
                painterResource(R.drawable.ic_favorite),
                painterResource(R.drawable.ic_info),
                painterResource(R.drawable.ic_settings)
            ),
            actionsElevated = listOf(false, false, false),
            fabAction = {},
            fabIcon = painterResource(R.drawable.ic_add),
            bottomBar = {
                NavigationBar {
                    MainScreenPages.entries.forEach { page ->
                        NavigationBarItem(
                            selected = pagerState.currentPage == page.ordinal,
                            onClick = { },
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
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    HomeScreenContent(
                        innerPadding = innerPadding,
                        navController = rememberNavController(),
                        routines = (0..0).map { i ->
                            UiWorkout(
                                id = i.toLong(),
                                title = "Chest day"
                            )
                        },
                        navigateToRoutine = {},
                        animatedVisibilityScope = this
                    )
                }
            }
        }
    }
}