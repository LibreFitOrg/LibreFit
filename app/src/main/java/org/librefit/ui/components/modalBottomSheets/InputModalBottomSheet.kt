/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components.modalBottomSheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.librefit.R
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.NumberPicker
import org.librefit.ui.models.InputModalBottomSheetState
import org.librefit.ui.theme.LibreFitTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InputModalBottomSheet(
    onDismiss: () -> Unit,
    state: InputModalBottomSheetState,
    onValueChange: (InputModalBottomSheetState) -> Unit
) {

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 15.dp, bottom = 40.dp, end = 15.dp, start = 15.dp)
        ) {
            item {
                Text(
                    text = when (state) {
                        is InputModalBottomSheetState.HoursMinutesSeconds -> stringResource(R.string.time)
                        is InputModalBottomSheetState.MinutesSeconds -> stringResource(R.string.time)
                        is InputModalBottomSheetState.Reps -> stringResource(R.string.reps)
                        is InputModalBottomSheetState.Weight -> stringResource(R.string.weight)
                    },
                    style = MaterialTheme.typography.headlineLargeEmphasized
                )
            }
            item {
                val textStyle = MaterialTheme.typography.displaySmall

                OutlinedCard(
                    shape = MaterialTheme.shapes.largeIncreased
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        when (state) {
                            is InputModalBottomSheetState.HoursMinutesSeconds -> {
                                NumberPicker(
                                    value = state.hours,
                                    options = state.hoursRange,
                                    label = { it.toString().padStart(2, '0') },
                                    onValueChange = {
                                        onValueChange(
                                            state.copy(
                                                hours = it
                                            )
                                        )
                                    },
                                    textStyle = textStyle
                                )
                                Text(
                                    modifier = Modifier.padding(3.dp),
                                    text = ":",
                                    style = textStyle
                                )
                                NumberPicker(
                                    value = state.minutes,
                                    options = state.minutesRange,
                                    label = { it.toString().padStart(2, '0') },
                                    onValueChange = {
                                        onValueChange(
                                            state.copy(
                                                minutes = it
                                            )
                                        )
                                    },
                                    textStyle = textStyle
                                )
                                Text(
                                    modifier = Modifier.padding(3.dp),
                                    text = ":",
                                    style = textStyle
                                )
                                NumberPicker(
                                    value = state.seconds,
                                    options = state.secondsRange,
                                    label = { it.toString().padStart(2, '0') },
                                    onValueChange = {
                                        onValueChange(
                                            state.copy(
                                                seconds = it
                                            )
                                        )
                                    },
                                    textStyle = textStyle
                                )
                            }

                            is InputModalBottomSheetState.MinutesSeconds -> {
                                NumberPicker(
                                    value = state.minutes,
                                    options = state.minutesRange,
                                    label = { it.toString().padStart(2, '0') },
                                    onValueChange = {
                                        onValueChange(
                                            state.copy(
                                                minutes = it
                                            )
                                        )
                                    },
                                    textStyle = textStyle
                                )
                                Text(
                                    modifier = Modifier.padding(3.dp),
                                    text = ":",
                                    style = textStyle
                                )
                                NumberPicker(
                                    value = state.seconds,
                                    options = state.secondsRange,
                                    label = { it.toString().padStart(2, '0') },
                                    onValueChange = {
                                        onValueChange(
                                            state.copy(
                                                seconds = it
                                            )
                                        )
                                    },
                                    textStyle = textStyle
                                )
                            }

                            is InputModalBottomSheetState.Reps -> {
                                NumberPicker(
                                    value = state.reps,
                                    options = state.repsRange,
                                    onValueChange = {
                                        onValueChange(
                                            state.copy(
                                                reps = it
                                            )
                                        )
                                    },
                                    textStyle = textStyle
                                )
                            }

                            is InputModalBottomSheetState.Weight -> {
                                NumberPicker(
                                    value = state.integerWeight,
                                    options = state.integerWeightRange,
                                    onValueChange = {
                                        onValueChange(
                                            state.copy(
                                                integerWeight = it
                                            )
                                        )
                                    },
                                    textStyle = textStyle
                                )
                                Text(
                                    text = ".",
                                    style = textStyle
                                )
                                NumberPicker(
                                    value = state.decimalWeight,
                                    options = state.decimalWeightRange,
                                    label = { it.toString().padStart(2, '0') },
                                    onValueChange = {
                                        onValueChange(
                                            state.copy(
                                                decimalWeight = it
                                            )
                                        )
                                    },
                                    textStyle = textStyle
                                )

                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.kg),
                                    style = textStyle
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun InputModelBottomSheetPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        var state by remember {
            mutableStateOf<InputModalBottomSheetState>(
                InputModalBottomSheetState.Weight()
            )
        }
        InputModalBottomSheet(
            state = state,
            onValueChange = {
                state = it
            },
            onDismiss = {},
        )
    }
}