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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.nav.Route
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.EmptyLottie
import org.librefit.ui.models.UiWorkout
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter.formatTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CalendarScreen(
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel: CalendarScreenViewModel = hiltViewModel()

    val yearRange by viewModel.yearRange.collectAsState()

    val workoutsFromDate by viewModel.workoutsFromDate.collectAsState()

    val selectableDates by viewModel.selectableDates.collectAsState()

    key(yearRange) {

        val datePickerState = rememberDatePickerState(
            selectableDates = selectableDates,
            yearRange = yearRange,
            initialSelectedDateMillis = LocalDateTime.now().atZone(ZoneOffset.UTC).toInstant()
                .toEpochMilli()
        )

        LaunchedEffect(datePickerState.selectedDateMillis) {
            viewModel.updateSelectedDateInMillis(datePickerState.selectedDateMillis)
        }

        CalendarScreenContent(
            navController = navController,
            datePickerState = datePickerState,
            workoutsFromDate = workoutsFromDate,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CalendarScreenContent(
    navController: NavHostController,
    datePickerState: DatePickerState,
    workoutsFromDate: List<UiWorkout>,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    LibreFitScaffold(
        title = AnnotatedString(stringResource(R.string.calendar)),
        navigateBack = { navController.popBackStack() }
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding) {
            item {
                DatePicker(
                    modifier = Modifier.clip(MaterialTheme.shapes.large),
                    state = datePickerState,
                    showModeToggle = false,
                )
            }

            item { HeadlineText(stringResource(R.string.your_workouts)) }

            if (workoutsFromDate.isEmpty()) {
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

            items(workoutsFromDate) { workout ->
                ElevatedCard(
                    onClick = {
                        navController.navigate(Route.InfoWorkoutScreen(workoutId = workout.id))
                    },
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(workout.id),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
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
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.sharedElement(
                                        sharedContentState = rememberSharedContentState(
                                            key = workout.id.toString() + workout.title
                                        ),
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(R.string.duration) + ": "
                                            + formatTime(workout.timeElapsed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Text(
                                    text = stringResource(R.string.label_when) + ": "
                                            + workout.completed.format(
                                        DateTimeFormatter.ofPattern("HH:mm")
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(
                                onClick = {
                                    navController.navigate(Route.InfoWorkoutScreen(workoutId = workout.id))
                                },
                            ) {
                                Icon(
                                    ImageVector.vectorResource(R.drawable.ic_info),
                                    stringResource(R.string.about)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun CalendarScreenPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                CalendarScreenContent(
                    navController = rememberNavController(),
                    datePickerState = rememberDatePickerState(),
                    workoutsFromDate = listOf(UiWorkout(title = "Name workout")),
                    animatedVisibilityScope = this,
                )
            }
        }
    }
}