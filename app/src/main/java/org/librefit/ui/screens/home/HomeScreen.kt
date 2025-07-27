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

package org.librefit.ui.screens.home

import android.Manifest
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.librefit.R
import org.librefit.db.entity.Workout
import org.librefit.nav.Route
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.theme.LibreFitTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
) {
    val viewModel: HomeScreenViewModel = hiltViewModel()


    val requestPermissionAgain by viewModel.requestPermissionAgain.collectAsState()

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        //Permission granted by default below Tiramisu
        null
    }

    val routines by viewModel.routines.collectAsState()

    HomeScreenContent(
        innerPadding = innerPadding,
        navController = navController,
        routines = routines,
        navigateToRoutine = { workoutId ->
            val hasNotificationPermission = notificationPermissionState?.status?.isGranted != false

            val requestPermission = !hasNotificationPermission && requestPermissionAgain

            if (requestPermission) {
                navController.navigate(Route.RequestPermissionScreen(workoutId = workoutId))
            } else {
                navController.navigate(Route.WorkoutScreen(workoutId = workoutId)) {
                    popUpTo(Route.RequestPermissionScreen(workoutId = workoutId)) {
                        inclusive = true
                    }
                }
            }
        }
    )
}

@Composable
private fun HomeScreenContent(
    innerPadding: PaddingValues,
    navController: NavHostController,
    routines: List<Workout>,
    navigateToRoutine: (Long) -> Unit
) {

    LibreFitLazyColumn(innerPadding) {
        item {
            //"Start empty workout" button
            LibreFitButton(
                text = stringResource(id = R.string.start_empty_workout),
                icon = ImageVector.vectorResource(R.drawable.ic_play_arrow),
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.start_creating_routine),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        //TODO: implement a default routine

        items(routines, key = { it.id }) { routine ->
            ElevatedCard(
                modifier = Modifier
                    .padding(5.dp)
                    .clip(CardDefaults.elevatedShape)
                    .clickable {
                        navController.navigate(Route.InfoWorkoutScreen(workoutId = routine.id))
                    }
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
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = {
                                navController.navigate(Route.InfoWorkoutScreen(workoutId = routine.id))
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.info)
                            )
                        }
                    }
                    LibreFitButton(
                        text = stringResource(R.string.start_routine),
                        icon = ImageVector.vectorResource(R.drawable.ic_play_arrow),
                        elevated = false
                    ) {
                        navigateToRoutine(routine.id)
                    }
                }
            }
        }


        bottomMargin()
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        LibreFitScaffold(
            title = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(stringResource(id = R.string.app_name).removeRange(5, 8))
                }
                append(stringResource(id = R.string.app_name).removeRange(0, 5))
            },
            actions = listOf {},
            actionsIcons = listOf(ImageVector.vectorResource(R.drawable.ic_settings)),
            actionsElevated = listOf(false),
            fabIcon = ImageVector.vectorResource(R.drawable.ic_add),
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_home),
                                stringResource(R.string.home)
                            )
                        },
                        label = { Text(stringResource(R.string.home)) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { },
                        icon = {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_person),
                                stringResource(R.string.profile)
                            )
                        },
                        label = { Text(stringResource(R.string.profile)) }
                    )
                }
            }
        ) {
            HomeScreenContent(
                innerPadding = it,
                navController = rememberNavController(),
                routines = (0..5).map { i -> Workout(id = i.toLong(), title = "Workout $i") },
                navigateToRoutine = {}
            )
        }
    }
}