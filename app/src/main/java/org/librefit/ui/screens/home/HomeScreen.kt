/*
 * Copyright (c) 2024 LibreFit
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.db.Workout
import org.librefit.nav.Destination
import org.librefit.ui.components.ConfirmDialog
import org.librefit.ui.components.CustomTextButton

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
) {
    val viewModel: HomeScreenViewModel = viewModel()

    val routineList by viewModel.routineList

    var selectedRoutine by remember { mutableStateOf<Workout>(Workout()) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(R.string.label_delete),
            text = stringResource(id = R.string.label_confirm_delete),
            onConfirm = {
                viewModel.deleteRoutine(selectedRoutine)
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues = innerPadding)
            .padding(start = 15.dp, end = 15.dp)
            .fillMaxSize()
    ) {
        item {
            Text(
                text = stringResource(id = R.string.label_quick_start),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            //"Start empty workout" button
            CustomTextButton(
                text = stringResource(id = R.string.label_start_empty_workout),
                icon = Icons.Default.PlayArrow,
                onClick = { navController.navigate(Destination.WorkoutScreen()) },
            )
        }
        item {
            Spacer(modifier = Modifier.height(15.dp))
        }

        item {
            Text(
                text = stringResource(id = R.string.label_your_routines),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
        }



        if (routineList.isNotEmpty()) {
            items(routineList) { routine ->
                ElevatedCard(
                    modifier = Modifier
                        .padding(5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = routine.title,
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    //TODO: show exercises of routine
                                },
                                enabled = false
                            ) {
                                Icon(Icons.Default.Info, null)
                            }
                            IconButton(onClick = {
                                selectedRoutine = routine
                                showConfirmDialog = true
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    stringResource(R.string.label_delete)
                                )
                            }
                        }
                        CustomTextButton(
                            text = stringResource(R.string.label_start_routine),
                            icon = Icons.Default.PlayArrow,
                            elevated = false
                        ) {
                            navController.navigate(
                                Destination.WorkoutScreen(
                                    workoutId = routine.id,
                                    workoutTitle = routine.title
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            //"Create a workout routine" button
            CustomTextButton(
                text = stringResource(id = R.string.label_create_routine),
                icon = Icons.Default.AddCircle,
                onClick = { navController.navigate(Destination.CreateRoutineScreen) },
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(innerPadding = PaddingValues(20.dp), navController)
}