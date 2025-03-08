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

package org.librefit.ui.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import org.librefit.R
import org.librefit.nav.Destination
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.animations.EmptyLottie
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.util.Formatter.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    sharedViewModel: SharedViewModel,
    navController: NavHostController
) {
    val viewModel: CalendarScreenViewModel = hiltViewModel()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = viewModel.getSelectableDatesFromWorkouts()
    )

    CustomScaffold(
        title = AnnotatedString(stringResource(R.string.calendar)),
        navigateBack = { navController.popBackStack() }
    ) {
        LazyColumn(
            contentPadding = it,
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                DatePicker(
                    modifier = Modifier.clip(MaterialTheme.shapes.large),
                    state = datePickerState,
                    showModeToggle = false,
                )
            }

            item { HeadlineText(stringResource(R.string.your_workouts)) }

            if (viewModel.getWorkoutsFromDate(datePickerState.selectedDateMillis).isEmpty()) {
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

            items(viewModel.getWorkoutsFromDate(datePickerState.selectedDateMillis)) { workout ->
                ElevatedCard {
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
                                    text = stringResource(R.string.duration) + ": "
                                            + formatTime(workout.timeElapsed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Text(
                                    text = stringResource(R.string.label_when) + ": "
                                            + viewModel.getTimeFromLocalDateTime(workout.completed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(
                                onClick = {
                                    sharedViewModel.updateWorkoutId(workout.id)
                                    navController.navigate(Destination.InfoWorkoutScreen)
                                },
                            ) {
                                Icon(Icons.Default.Info, stringResource(R.string.about))
                            }
                        }
                    }
                }
            }

            bottomMargin()
        }
    }
}