/*
 * Copyright (c) 2024. LibreFit
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

package org.librefit.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.librefit.R
import org.librefit.db.Workout
import org.librefit.ui.components.ConfirmDialog
import org.librefit.ui.components.animations.EmptyLottie
import org.librefit.util.formatTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun ProfileScreen(innerPadding: PaddingValues) {

    val viewModel: ProfileScreenViewModel = viewModel()

    val workoutList by viewModel.workoutList

    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(
        Locale.getDefault()
    )

    var selectedWorkout by remember { mutableStateOf<Workout>(Workout()) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(R.string.delete),
            text = stringResource(id = R.string.confirm_delete),
            onConfirm = {
                viewModel.deleteWorkout(selectedWorkout)
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
                text = stringResource(R.string.your_workouts),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        if (workoutList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyLottie()
                    Text(
                        text = stringResource(R.string.nothing_to_show),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        items(workoutList) { workout ->
            ElevatedCard(
                modifier = Modifier.padding(5.dp)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = workout.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = stringResource(R.string.finished_on) + ": "
                                        + workout.completed.format(formatter),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(R.string.duration) + ": "
                                        + formatTime(workout.timeElapsed),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(
                            onClick = {
                                //TODO: show stats of workout
                            },
                            enabled = false
                        ) {
                            Icon(Icons.Default.Info, stringResource(R.string.about))
                        }
                        IconButton(onClick = {
                            selectedWorkout = workout
                            showConfirmDialog = true
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                stringResource(R.string.delete)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(innerPadding = PaddingValues(20.dp))
}