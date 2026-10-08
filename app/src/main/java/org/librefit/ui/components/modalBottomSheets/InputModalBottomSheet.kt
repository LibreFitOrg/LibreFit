/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components.modalBottomSheets

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.librefit.R
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.NumberPicker
import org.librefit.ui.models.InputModalBottomSheetState
import org.librefit.ui.models.InputModalBottomSheetState.HoursMinutesSeconds
import org.librefit.ui.models.InputModalBottomSheetState.MinutesSeconds
import org.librefit.ui.models.InputModalBottomSheetState.Reps
import org.librefit.ui.models.InputModalBottomSheetState.Weight
import org.librefit.ui.models.InputModalBottomSheetState.Weight.Companion.safeCopy
import org.librefit.ui.models.autoUnitSuffix
import org.librefit.ui.theme.LibreFitTheme
import kotlin.time.Duration.Companion.milliseconds

/** Seconds added or removed by the step buttons of the time-based input modes. */
private const val TIME_STEP_SECONDS = 30

/** Amount added or removed by the step buttons of the [InputModalBottomSheetState.Reps] input mode. */
private const val REPS_STEP = 5

/** Amount added or removed by the step buttons of the [InputModalBottomSheetState.Weight] input mode. */
private const val WEIGHT_STEP = 10

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InputModalBottomSheet(
    onDismiss: () -> Unit,
    state: InputModalBottomSheetState,
    onValueChange: (InputModalBottomSheetState) -> Unit,
    dismissAutomatically: Boolean,
) {

    // Save initial state so user can restore it
    val initialState = remember { state }

    var isAnyNumberPickerChanging by rememberSaveable { mutableStateOf(value = false) }

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden
    )

    if (dismissAutomatically) {
        // Dismiss automatically after 1 second of inactivity if value was changed
        LaunchedEffect(isAnyNumberPickerChanging, state) {
            // Only trigger if modified and not currently scrolling
            if ((initialState != state) && !isAnyNumberPickerChanging) {
                delay(700.milliseconds)
                sheetState.hide()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 15.dp, bottom = 40.dp, end = 15.dp, start = 15.dp)
        ) {
            item {
                Text(
                    text = when (state) {
                        is HoursMinutesSeconds -> stringResource(R.string.time)
                        is MinutesSeconds -> stringResource(R.string.time)
                        is Reps -> stringResource(R.string.reps)
                        is Weight -> stringResource(R.string.weight)
                    },
                    style = MaterialTheme.typography.headlineLargeEmphasized
                )
            }
            item {
                val textStyle = MaterialTheme.typography.displaySmall
                val iconSize = remember { 36.dp }

                OutlinedCard(
                    shape = MaterialTheme.shapes.largeIncreased
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StepButtonColumn(
                                state = state,
                                isIncrement = false,
                                iconSize = iconSize,
                                onValueChange = onValueChange
                            )
                            InputPickersRow(
                                state = state,
                                textStyle = textStyle,
                                onValueChange = onValueChange,
                                onNumberPickerScroll = {
                                    isAnyNumberPickerChanging = it
                                }
                            )
                            StepButtonColumn(
                                state = state,
                                isIncrement = true,
                                iconSize = iconSize,
                                onValueChange = onValueChange
                            )
                        }
                    }
                }
            }

            item {
                val interactionSources = remember { List(2) { MutableInteractionSource() } }
                Row {
                    ButtonGroup(
                        overflowIndicator = {}
                    ) {
                        customItem(
                            menuContent = {},
                            buttonGroupContent = {
                                LibreFitButton(
                                    modifier = Modifier
                                        .weight(1f)
                                        .animateWidth(interactionSources[0]),
                                    text = stringResource(R.string.undo),
                                    icon = painterResource(R.drawable.ic_undo),
                                    interactionSource = interactionSources[0]
                                ) {
                                    onValueChange(initialState)
                                }
                            }
                        )
                        customItem(
                            menuContent = {},
                            buttonGroupContent = {
                                LibreFitButton(
                                    modifier = Modifier
                                        .weight(1f)
                                        .animateWidth(interactionSources[1]),
                                    text = stringResource(R.string.clear),
                                    icon = painterResource(R.drawable.ic_cancel),
                                    interactionSource = interactionSources[1],
                                    elevated = false
                                ) {
                                    onValueChange(
                                        when (state) {
                                            is HoursMinutesSeconds -> state.copy(
                                                hours = state.hoursRange.first(),
                                                minutes = state.minutesRange.first(),
                                                seconds = state.secondsRange.first(),
                                            )

                                            is MinutesSeconds -> state.copy(
                                                minutes = state.minutesRange.first(),
                                                seconds = state.secondsRange.first(),
                                            )

                                            is Reps -> state.copy(
                                                reps = state.repsRange.first()
                                            )

                                            is Weight -> state.safeCopy(
                                                integerWeight = state.integerWeightRange.first(),
                                                decimalWeight = state.decimalWeightRange.first()
                                            )
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * The [NumberPicker] row of the input card, laid out according to the input mode of [state]:
 * three pickers for hours, minutes and seconds; two for minutes and seconds; a single picker
 * for repetitions; and integer, decimal pickers plus unit-suffix label for weight.
 *
 * @param state The current state of the sheet, defining both the visible pickers and their values.
 * @param textStyle The text style shared by all pickers and separators.
 * @param onValueChange Invoked with the new state whenever any picker value changes.
 * @param onNumberPickerScroll Invoked when a picker starts (true) or stops (false) scrolling.
 * @param modifier The modifier to apply to the row.
 */
@Composable
private fun InputPickersRow(
    state: InputModalBottomSheetState,
    textStyle: TextStyle,
    onValueChange: (InputModalBottomSheetState) -> Unit,
    onNumberPickerScroll: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        when (state) {
            is HoursMinutesSeconds -> {
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
                    onNumberPickerScroll = onNumberPickerScroll,
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
                    onNumberPickerScroll = onNumberPickerScroll,
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
                    onNumberPickerScroll = onNumberPickerScroll,
                    textStyle = textStyle
                )
            }

            is MinutesSeconds -> {
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
                    onNumberPickerScroll = onNumberPickerScroll,
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
                    onNumberPickerScroll = onNumberPickerScroll,
                    textStyle = textStyle
                )
            }

            is Reps -> {
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
                    onNumberPickerScroll = onNumberPickerScroll,
                    textStyle = textStyle
                )
            }

            is Weight -> {
                NumberPicker(
                    value = state.integerWeight,
                    options = state.integerWeightRange,
                    onValueChange = {
                        onValueChange(
                            state.safeCopy(
                                integerWeight = it
                            )
                        )
                    },
                    onNumberPickerScroll = onNumberPickerScroll,
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
                            state.safeCopy(
                                decimalWeight = it
                            )
                        )
                    },
                    onNumberPickerScroll = onNumberPickerScroll,
                    textStyle = textStyle
                )

                Spacer(Modifier.width(10.dp))
                Text(
                    text = autoUnitSuffix(),
                    style = textStyle
                )
            }
        }
    }
}

/**
 * Step button configuration (icons, content descriptions and step amount) for each input mode.
 *
 * @property decrementIconResId The drawable shown on the decrement button.
 * @property incrementIconResId The drawable shown on the increment button.
 * @property decrementContentDescriptionResId The content description of the decrement button.
 * @property incrementContentDescriptionResId The content description of the increment button.
 * @property step The unsigned amount added or removed by each button press.
 */
private data class StepButtonConfig(
    @DrawableRes val decrementIconResId: Int,
    @DrawableRes val incrementIconResId: Int,
    @StringRes val decrementContentDescriptionResId: Int,
    @StringRes val incrementContentDescriptionResId: Int,
    val step: Int
) {
    companion object {

        /** Factory method to create a [StepButtonConfig] instance automatically based on given [InputModalBottomSheetState]. */
        fun auto(state: InputModalBottomSheetState): StepButtonConfig {
            return when (state) {
                is HoursMinutesSeconds -> Time
                is MinutesSeconds -> Time
                is Reps -> Reps
                is Weight -> Weight
            }
        }

        /** Configuration shared by the time-based input modes, stepping by [TIME_STEP_SECONDS] seconds. */
        val Time = StepButtonConfig(
            decrementIconResId = R.drawable.ic_replay_30,
            incrementIconResId = R.drawable.ic_forward_30,
            decrementContentDescriptionResId = R.string.decrease_time_by_30_seconds,
            incrementContentDescriptionResId = R.string.increase_time_by_30_seconds,
            step = TIME_STEP_SECONDS
        )

        /** Configuration for the repetition input mode, stepping by [REPS_STEP] reps. */
        val Reps = StepButtonConfig(
            decrementIconResId = R.drawable.ic_replay_5,
            incrementIconResId = R.drawable.ic_forward_5,
            decrementContentDescriptionResId = R.string.decrease_reps_by_5,
            incrementContentDescriptionResId = R.string.increase_reps_by_5,
            step = REPS_STEP
        )

        /** Configuration for the weight input mode, stepping by [WEIGHT_STEP] units of the current unit system. */
        val Weight = StepButtonConfig(
            decrementIconResId = R.drawable.ic_replay_10,
            incrementIconResId = R.drawable.ic_forward_10,
            decrementContentDescriptionResId = R.string.decrease_weight_by_10,
            incrementContentDescriptionResId = R.string.increase_weight_by_10,
            step = WEIGHT_STEP
        )
    }
}

/**
 * A column holding the [IconButton] that steps the number pickers of [state] by the amount
 * configured for its input mode. Placed to the left (decrement) or right (increment) of the
 * pickers, mirroring the barbell calculator sheet.
 *
 * @param state The current state of the sheet, defining the input mode.
 * @param isIncrement Whether this column holds the increment button; otherwise the decrement one.
 * @param iconSize The size of the button icon.
 * @param onValueChange Invoked with the stepped state when the button is pressed.
 * @param modifier The modifier to apply to the column.
 */
@Composable
private fun StepButtonColumn(
    state: InputModalBottomSheetState,
    isIncrement: Boolean,
    iconSize: Dp,
    onValueChange: (InputModalBottomSheetState) -> Unit,
    modifier: Modifier = Modifier
) {
    val config = StepButtonConfig.auto(state)

    val iconResId = if (isIncrement) config.incrementIconResId else config.decrementIconResId
    val contentDescription = stringResource(
        if (isIncrement) {
            config.incrementContentDescriptionResId
        } else {
            config.decrementContentDescriptionResId
        }
    )
    val signedStep = if (isIncrement) config.step else -config.step

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(
            shapes = IconButtonDefaults.shapes(),
            onClick = {
                onValueChange(state.steppedBy(signedStep))
            }
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}


@Preview
@Composable
private fun InputModelBottomSheetPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        var state by remember {
            mutableStateOf<InputModalBottomSheetState>(
                Weight.create()
            )
        }
        InputModalBottomSheet(
            state = state,
            onValueChange = {
                state = it
            },
            onDismiss = {},
            dismissAutomatically = false
        )
    }
}