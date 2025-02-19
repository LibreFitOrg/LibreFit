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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.nav.Destination
import org.librefit.nav.checkPermissionsBeforeNavigateToWorkout
import org.librefit.ui.components.CustomButton
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.screens.shared.SharedViewModel

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
) {
    val viewModel: HomeScreenViewModel = hiltViewModel()

    val context = LocalContext.current

    val requestPermissionAgain by viewModel.requestPermissionAgain.collectAsState(initial = true)

    val routineList by viewModel.routineList



    LazyColumn(
        modifier = Modifier
            .padding(paddingValues = innerPadding)
            .padding(start = 15.dp, end = 15.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        item {
            //"Start empty workout" button
            CustomButton(
                text = stringResource(id = R.string.start_empty_workout),
                icon = Icons.Default.PlayArrow,
                onClick = {
                    checkPermissionsBeforeNavigateToWorkout(
                        requestPermissionAgain = requestPermissionAgain,
                        navController = navController,
                        sharedViewModel = sharedViewModel,
                        appContext = context.applicationContext
                    )
                },
            )
        }


        item {
            HeadlineText(stringResource(id = R.string.your_routines))
        }

        if (routineList.isEmpty()) {
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

        items(routineList, key = { it.id }) { routine ->
            ElevatedCard(
                modifier = Modifier
                    .padding(5.dp)
                    .clip(CardDefaults.elevatedShape)
                    .clickable {
                        sharedViewModel.updateWorkoutId(routine.id)
                        navController.navigate(Destination.InfoWorkoutScreen)
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
                                sharedViewModel.updateWorkoutId(routine.id)
                                navController.navigate(Destination.InfoWorkoutScreen)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.info)
                            )
                        }
                    }
                    CustomButton(
                        text = stringResource(R.string.start_routine),
                        icon = Icons.Default.PlayArrow,
                        elevated = false
                    ) {
                        checkPermissionsBeforeNavigateToWorkout(
                            workoutId = routine.id,
                            requestPermissionAgain = requestPermissionAgain,
                            navController = navController,
                            sharedViewModel = sharedViewModel,
                            appContext = context.applicationContext
                        )
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
    HomeScreen(
        innerPadding = PaddingValues(20.dp),
        rememberNavController(),
        viewModel()
    )
}