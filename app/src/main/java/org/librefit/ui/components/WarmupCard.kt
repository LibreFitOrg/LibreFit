/*
 *
 *  * SPDX-License-Identifier: GPL-3.0-or-later
 *  * Copyright (c) 2025-2026. The LibreFit Contributors
 *  *
 *  * LibreFit is subject to additional terms covering author attribution and trademark usage;
 *  * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 *
 */

package org.librefit.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.enums.WarmupMode
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.modalBottomSheets.InputModalBottomSheet
import org.librefit.ui.models.InputModalBottomSheetState
import org.librefit.ui.models.UiExercise
import org.librefit.ui.models.UiSet
import org.librefit.ui.models.UiWarmup
import org.librefit.ui.models.UiWarmupWithSets
import org.librefit.ui.models.recalcWarmupSets
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import org.librefit.util.Formatter.getDecimalDigitsAsInteger
import kotlin.math.roundToInt

/**
 * A custom [ElevatedCard] designed to display an [UiWarmupWithSets] with a uniform appearance across
 * the app.
 *
 * @param modifier A [Modifier] that should be passed as `Modifier.animateItem` to enable
 * animation for the card within the list.
 * @param animatedVisibilityScope Used for image's animation transition
 * @param warmupWithSets An instance of [UiWarmupWithSets] containing all the relevant information
 * required for the card display.
 * @param addSet A lambda function invoked when the "Add set" button is clicked.
 * @param onDelete A lambda function executed when the *Delete* icon is clicked, it should result in
 * the removal of the card.
 * @param isCollapsed When `true`, the card collapses its editable body to provide clearer reorder feedback. So it's true only when reordering one of exercises in the list.
 * @param dragHandleModifier Modifier applied to the optional drag handle.
 * @param onReorderRequest A lambda triggered when the `reorder` option from dropdown menu is pressed.
 * @param isDragging when `true`, it applies a shadow to further emphasize with a shadow that the card is dragged.
 * @param useScrollWheelForInput If `true`, [InputModalBottomSheet] appears instead of keyboard
 * @param dismissScrollWheelInputAutomatically If both this and [useScrollWheelForInput] are `true`, the [InputModalBottomSheet] will be dismissed automatically after first edit.
 * @param updateExerciseNotes A function to update notes based on [UiExercise.id]. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateExerciseNotes] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateExerciseNotes].
 * @param updateExerciseRestTime A function to update rest time based on [UiExercise.id]. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateExerciseRestTime] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateExerciseRestTime].
 * @param updateWarmupSetMode A function to update the set mode based on.
 * For more details, refer to [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateWarmupSetMode]
 * and [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateWarmupSetMode].
 * @param updateWarmupTarget A function to update the target load of the warmup sets.
 * For more details, refer to [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateWarmupTarget]
 * and [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateWarmupTarget].
 * @param updateSetLoad A function to update load based on [UiSet.id]. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateSetLoad] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateSetLoad].
 * @param updateSetReps A function to update reps based on [UiSet.id]. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateSetReps] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateSetReps].
 * @param updateSetTime A function to update time based on [UiSet.id].. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateSetTime] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateSetTime].
 * @param updateSetCompleted A function to update completed state based on [UiSet.id]. For more details, refer to
 * [org.librefit.ui.screens.workout.WorkoutScreenViewModel.updateSetCompleted] and
 * [org.librefit.ui.screens.editWorkout.EditWorkoutScreenViewModel.updateSetCompleted].
 * @param deleteSet A function called when the user swipes the set to remove it.
 * @param showInfo A lambda function executed when info icon next to "type of set" or "rest time" text
 * is clicked. The passed parameter is used by [org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet] to show the relevant information.
 * @param workout A Boolean flag indicating whether a checkbox should be displayed next to each set.
 */
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SharedTransitionScope.WarmupCard(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    warmupWithSets: UiWarmupWithSets,
    workout: Boolean = false,
    addSet: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    isCollapsed: Boolean = false,
    dragHandleModifier: Modifier = Modifier,
    isDragging: Boolean,
    useScrollWheelForInput: Boolean,
    dismissScrollWheelInputAutomatically: Boolean,
    onReorderRequest: () -> Unit,
    deleteSet: (Long) -> Unit,
    updateExerciseNotes: (String, Long) -> Unit,
    updateExerciseRestTime: (Int, Long) -> Unit,
    updateWarmupSetMode: (WarmupMode, Long) -> Unit,
    updateWarmupTarget: (Double, Long) -> Unit,
    updateSetTime: (Int, Long) -> Unit,
    updateSetReps: (Int, Long) -> Unit,
    updateSetLoad: (Double, Long) -> Unit,
    updateSetCompleted: (Boolean, Long) -> Unit,
    showInfo: (InfoMode) -> Unit,
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val shape = MaterialTheme.shapes.extraLarge

    var inputModalBottomSheetState by remember { mutableStateOf<InputModalBottomSheetState?>(null) }
    var inputSetId by rememberSaveable { mutableStateOf<Long?>(null) }

    inputModalBottomSheetState?.let {
        InputModalBottomSheet(
            state = it,
            onValueChange = { newState ->
                inputModalBottomSheetState = newState
                inputSetId?.let { id ->
                    when (newState) {
                        is InputModalBottomSheetState.Weight -> {
                            updateWarmupTarget(
                                newState.totalWeight,
                                id
                            )
                        }

                        else -> error("newState in WarmupCard should not have this value: $newState")
                    }
                }
            },
            onDismiss = {
                inputModalBottomSheetState = null
                inputSetId = null
            },
            dismissAutomatically = dismissScrollWheelInputAutomatically
        )
    }

    ElevatedCard(
        modifier = modifier.then(
            if (isDragging) Modifier.shadow(
                10.dp,
                shape = shape
            ) else Modifier
        ),
        shape = shape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.warmup),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Column {
                    AnimatedContent(
                        targetState = isCollapsed,
                        label = "DragHandleTransition",
                    ) { isReordering ->
                        if (isReordering) {
                            IconButton(
                                modifier = dragHandleModifier,
                                onClick = {}
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_drag_handle),
                                    contentDescription = stringResource(R.string.reorder)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { showMenu = true }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_more_options),
                                    contentDescription = stringResource(R.string.more_options)
                                )
                            }
                            DropdownMenuPopup(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }) {
                                DropdownMenuGroup(
                                    shapes = MenuDefaults.groupShape(0, 1) // Top-level group shape
                                ) {
                                    // MenuDefaults.Label { Text("Header") }
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.reorder)) },
                                        leadingIcon = {
                                            Icon(
                                                painterResource(R.drawable.ic_reorder),
                                                stringResource(R.string.reorder)
                                            )
                                        },
                                        onClick = {
                                            onReorderRequest()
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.delete)) },
                                        leadingIcon = {
                                            Icon(
                                                painterResource(R.drawable.ic_delete),
                                                stringResource(R.string.delete)
                                            )
                                        },
                                        onClick = {
                                            onDelete(warmupWithSets.warmup.id)
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = !isCollapsed) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(id = R.string.notes)) },
                        value = warmupWithSets.warmup.notes,
                        onValueChange = { updateExerciseNotes(it, warmupWithSets.warmup.id) }
                    )

                    //Rest timer slider
                    Column {
                        var showSlider by rememberSaveable { mutableStateOf(false) }
                        var restTime by remember { mutableIntStateOf(warmupWithSets.warmup.restTime) }
                        val haptic = LocalHapticFeedback.current
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    // Read more at InfoModalBottomSheet
                                    onClick = { showInfo(InfoMode.REST_TIMER) }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_info),
                                        contentDescription = stringResource(R.string.info)
                                    )
                                }
                                Text(
                                    stringResource(R.string.rest_time) + ": " + restTime
                                            + " " + stringResource(R.string.seconds).replaceFirstChar { it.lowercase() })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconToggleButton(
                                    checked = showSlider,
                                    onCheckedChange = {
                                        showSlider = it
                                        haptic.performHapticFeedback(if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(if (showSlider) R.drawable.ic_check else R.drawable.ic_edit),
                                        contentDescription = stringResource(if (showSlider) R.string.save else R.string.edit)
                                    )
                                }
                            }
                        }
                        AnimatedVisibility(visible = showSlider) {
                            Slider(
                                value = restTime.toFloat(),
                                onValueChange = {
                                    // By dividing first and then multiplying by 5, it rounds to the closest number multiple of 5
                                    restTime = (it / 5).roundToInt() * 5
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                },
                                onValueChangeFinished = {
                                    updateExerciseRestTime(
                                        restTime,
                                        warmupWithSets.warmup.id
                                    )
                                },
                                valueRange = 0f..300f,
                                // 19 steps means values multiple of 5
                                steps = 19
                            )
                        }
                    }

                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(0.5f),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showInfo(InfoMode.WARMUP_TARGET) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = stringResource(R.string.info) + ":"
                                )
                            }
                            Text(stringResource(R.string.target) + " (" + stringResource(R.string.kg) + ")")
                        }
                        val focusRequester = remember { FocusRequester() }
                        var weightValue by rememberSaveable(warmupWithSets.warmup.target) {
                            mutableStateOf(
                                warmupWithSets.warmup.target.toString()
                            )
                        }
                        Row(
                            modifier = Modifier.weight(0.5f),
                        ) {
                            Box {
                                OutlinedTextField(
                                    shape = MaterialTheme.shapes.large,
                                    modifier = Modifier
                                        .padding(start = 10.dp, end = 10.dp)
                                        .clickable {
                                            focusRequester.requestFocus()
                                        }
                                        .focusRequester(focusRequester)
                                        .focusable(),
                                    value = weightValue,
                                    onValueChange = { string ->
                                        weightValue = Formatter.normalizeNumericString(string)
                                        updateWarmupTarget(
                                            Formatter.parseDoubleFromString(weightValue) ?: 0.0,
                                            warmupWithSets.warmup.id
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    readOnly = useScrollWheelForInput
                                )
                                if (useScrollWheelForInput) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(MaterialTheme.shapes.extraLarge)
                                            .clickable {
                                                inputModalBottomSheetState =
                                                    InputModalBottomSheetState.Weight(
                                                        integerWeight = warmupWithSets.warmup.target.toInt(),
                                                        decimalWeight = warmupWithSets.warmup.target.getDecimalDigitsAsInteger()
                                                    )
                                                inputSetId = warmupWithSets.warmup.id
                                            }
                                    ) { }
                                }
                            }
                        }
                    }


                    // Set mode selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(0.5f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            IconButton(
                                // Refer to InfoModalBottomSheet to know the reason behind this value.
                                // Do NOT change it.
                                onClick = { showInfo(InfoMode.TYPE_OF_WARMUP) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = stringResource(R.string.info) + ":"
                                )
                            }
                            Text(stringResource(R.string.type_of_warmup))
                        }

                        var expanded by remember { mutableStateOf(false) }

                        val focusRequester = remember { FocusRequester() }

                        // Type of set selector
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier
                                .padding(start = 10.dp, end = 10.dp)
                                .weight(0.5f)
                                .clickable {
                                    expanded = !expanded
                                    focusRequester.requestFocus()
                                }
                                .focusRequester(focusRequester)
                                .focusable()
                        ) {
                            OutlinedTextField(
                                shape = MaterialTheme.shapes.large,
                                readOnly = true,
                                value = stringResource(
                                    Formatter.setWarmupModeToStringId(
                                        warmupWithSets.warmup.warmupMode
                                    )
                                ),
                                onValueChange = {},
                                singleLine = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                WarmupMode.entries.forEachIndexed { _, mode ->
                                    DropdownMenuItem(
                                        onClick = {
                                            updateWarmupSetMode(mode, warmupWithSets.warmup.id)
                                            expanded = false
                                        },
                                        text = {
                                            Text(
                                                text = stringResource(
                                                    Formatter.setWarmupModeToStringId(
                                                        mode
                                                    )
                                                )
                                            )
                                        },
                                        trailingIcon = if (warmupWithSets.warmup.warmupMode == mode) {
                                            {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_check),
                                                    contentDescription = stringResource(R.string.checkbox)
                                                )
                                            }
                                        } else null,
                                        modifier = Modifier.background(
                                            if (warmupWithSets.warmup.warmupMode == mode) MaterialTheme.colorScheme.inversePrimary.copy(
                                                0.3f
                                            ) else Color.Unspecified
                                        )
                                    )
                                }
                            }
                        }
                    }

                    ElevatedCard(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        //Headline set
                        Row(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Spacer(Modifier)

                            Text(
                                text = stringResource(R.string.load) + " (" + stringResource(R.string.kg) + ")",
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Text(
                                text = stringResource(id = R.string.reps),
                                color = MaterialTheme.colorScheme.secondary
                            )

                            if (workout) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = stringResource(R.string.done)
                                )
                            }
                        }

                        //Sets
                        Column(modifier = Modifier.animateContentSize()) {
                            warmupWithSets.sets.forEachIndexed { i, set ->
                                key(set.id) {
                                    WarmupSet(
                                        i = i,
                                        set = set,
                                        lastIndex = warmupWithSets.sets.lastIndex,
                                        workout = workout,
                                        useScrollWheelForInput = useScrollWheelForInput,
                                        dismissScrollWheelInputAutomatically = dismissScrollWheelInputAutomatically,
                                        deleteSet = deleteSet,
                                        updateSetTime = updateSetTime,
                                        updateSetReps = updateSetReps,
                                        updateSetLoad = updateSetLoad,
                                        updateSetCompleted = updateSetCompleted,
                                    )
                                }
                            }
                        }
                    }

                    //Add set button
                    LibreFitButton(
                        text = stringResource(id = R.string.add_set),
                        icon = painterResource(R.drawable.ic_add_circle),
                        onClick = { addSet(warmupWithSets.warmup.id) },
                        elevated = false
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WarmupSet(
    i: Int,
    set: UiSet,
    lastIndex: Int,
    workout: Boolean,
    useScrollWheelForInput: Boolean,
    dismissScrollWheelInputAutomatically: Boolean,
    deleteSet: (Long) -> Unit,
    updateSetTime: (Int, Long) -> Unit,
    updateSetReps: (Int, Long) -> Unit,
    updateSetLoad: (Double, Long) -> Unit,
    updateSetCompleted: (Boolean, Long) -> Unit,
) {
    var repValue by rememberSaveable(set.reps) { mutableStateOf(set.reps.toString()) }
    var weightValue by rememberSaveable(set.load) {
        mutableStateOf(
            Formatter.normalizeNumericString(
                set.load.toString()
            )
        )
    }

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()

    var inputModalBottomSheetState by remember { mutableStateOf<InputModalBottomSheetState?>(null) }
    var inputSetId by rememberSaveable { mutableStateOf<Long?>(null) }

    inputModalBottomSheetState?.let {
        InputModalBottomSheet(
            state = it,
            onValueChange = { newState ->
                inputModalBottomSheetState = newState
                inputSetId?.let { id ->
                    when (newState) {
                        is InputModalBottomSheetState.Weight -> {
                            updateSetLoad(
                                newState.totalWeight,
                                id
                            )
                        }

                        is InputModalBottomSheetState.Reps -> {
                            updateSetReps(newState.reps, id)
                        }

                        is InputModalBottomSheetState.MinutesSeconds -> {
                            updateSetTime(newState.totalSeconds, id)
                        }

                        else -> error("newState in ExerciseCard should not have this value: $newState")
                    }
                }
            },
            onDismiss = {
                inputModalBottomSheetState = null
                inputSetId = null
            },
            dismissAutomatically = dismissScrollWheelInputAutomatically
        )
    }

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(swipeToDismissBoxState.currentValue) {
        if (swipeToDismissBoxState.currentValue != SwipeToDismissBoxValue.Settled) {
            haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        }
    }

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        onDismiss = { deleteSet(set.id) },
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(
                            topStart = CornerSize(if (i == 0) 45 else 0),
                            topEnd = CornerSize(if (i == 0) 45 else 0),
                            bottomEnd = CornerSize(
                                if (i == lastIndex) 45 else 0
                            ),
                            bottomStart = CornerSize(
                                if (i == lastIndex) 45 else 0
                            ),
                        )
                    )
                    .background(
                        when (swipeToDismissBoxState.dismissDirection) {
                            SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                        }
                    )
                    .padding(start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = when (swipeToDismissBoxState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                    SwipeToDismissBoxValue.Settled -> Arrangement.Start
                    SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        val backgroundColor by animateColorAsState(
            targetValue = if (set.completed) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            label = "animated_color_for_set_background"
        )
        val contentColor by animateColorAsState(
            targetValue = if (set.completed) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            label = "animated_color_for_set_content"
        )
        Row(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = CornerSize(if (i == 0) 45 else 0),
                        topEnd = CornerSize(if (i == 0) 45 else 0),
                        bottomEnd = CornerSize(
                            if (i == lastIndex) 45 else 0
                        ),
                        bottomStart = CornerSize(
                            if (i == lastIndex) 45 else 0
                        ),
                    )
                )
                .background(backgroundColor)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${i + 1}",
                color = contentColor,
                modifier = Modifier.padding(start = 20.dp)
            )

            //Weight
            Box {
                OutlinedTextField(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.width(80.dp),
                    value = weightValue,
                    onValueChange = { string ->
                        weightValue = Formatter.normalizeNumericString(string, maxDecimalDigits = 2)

                        updateSetLoad(
                            Formatter.parseDoubleFromString(weightValue) ?: 0.0,
                            set.id
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor,
                    ),
                    readOnly = useScrollWheelForInput
                )
                if (useScrollWheelForInput) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable {
                                inputModalBottomSheetState =
                                    InputModalBottomSheetState.Weight(
                                        integerWeight = set.load.toInt(),
                                        decimalWeight = set.load.getDecimalDigitsAsInteger()
                                    )
                                inputSetId = set.id
                            }
                    ) { }
                }
            }

            //Reps
            Box {
                OutlinedTextField(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.width(80.dp),
                    value = repValue,
                    onValueChange = { string ->
                        repValue = Formatter.normalizeNumericString(string)
                        Formatter.parseIntegerFromString(repValue)?.let {
                            updateSetReps(it, set.id)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor,
                    ),
                    readOnly = useScrollWheelForInput
                )
                if (useScrollWheelForInput) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable {
                                inputModalBottomSheetState = InputModalBottomSheetState.Reps(
                                    reps = repValue.toInt()
                                )
                                inputSetId = set.id
                            }
                    ) { }
                }

            }

            if (workout) {
                Checkbox(
                    checked = set.completed,
                    onCheckedChange = { checked ->
                        updateSetCompleted(checked, set.id)
                    }
                )
            }

        }
    }

}


@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Composable
private fun WarmupCardPreview() {
    val currentIdSetWithRunningSet = remember { mutableStateOf<Long?>(null) }

    val e = remember {
        mutableStateOf(
            UiWarmupWithSets(
                warmup = UiWarmup(
                    notes = "This is a note!",
                    restTime = 60,
                    warmupMode = WarmupMode.DEFAULT,
                    target = 100.98992346329347,
                ),
                sets = persistentListOf(UiSet(completed = true), UiSet(load = 100.0)),
            )
        )
    }

    LibreFitTheme(dynamicColor = true, themeMode = ThemeMode.DARK) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                WarmupCard(
                    animatedVisibilityScope = this,
                    warmupWithSets = e.value,
                    addSet = {
                        val newSets = e.value.sets.toMutableList() + UiSet()
                        e.value = e.value.copy(sets = newSets.toImmutableList())
                    },
                    onDelete = {},
                    deleteSet = { id ->
                        e.value = e.value.copy(sets = e.value.sets.filter { it.id != id }
                            .toImmutableList())
                        if (id == currentIdSetWithRunningSet.value) currentIdSetWithRunningSet.value =
                            null
                    },
                    showInfo = {},
                    workout = true,
                    isDragging = false,
                    useScrollWheelForInput = false,
                    dismissScrollWheelInputAutomatically = false,
                    updateExerciseNotes = { notes, _ ->
                        e.value = e.value.copy(warmup = e.value.warmup.copy(notes = notes))
                    },
                    updateExerciseRestTime = { restTime, _ ->
                        e.value =
                            e.value.copy(warmup = e.value.warmup.copy(restTime = restTime))
                    },
                    updateWarmupSetMode = { warmupMode, _ ->
                        e.value = e.value.copy(
                            warmup = e.value.warmup.copy(warmupMode = warmupMode),
                            sets = recalcWarmupSets(
                                e.value.warmup.target,
                                e.value.warmup.warmupMode
                            )
                        )
                    },
                    updateWarmupTarget = { warmupTarget, _ ->
                        e.value = e.value.copy(
                            warmup = e.value.warmup.copy(target = warmupTarget),
                            sets = recalcWarmupSets(
                                e.value.warmup.target,
                                e.value.warmup.warmupMode
                            )
                        )
                    },
                    updateSetTime = { time, id ->
                        e.value = e.value.copy(
                            sets = e.value.sets.map {
                                if (it.id == id) it.copy(elapsedTime = time) else it
                            }.toImmutableList()
                        )
                    },
                    updateSetReps = { reps, id ->
                        e.value = e.value.copy(
                            sets = e.value.sets.map {
                                if (it.id == id) it.copy(reps = reps) else it
                            }.toImmutableList()
                        )
                    },
                    updateSetLoad = { load, id ->
                        e.value = e.value.copy(
                            sets = e.value.sets.map {
                                if (it.id == id) it.copy(load = load) else it
                            }.toImmutableList()
                        )
                    },
                    updateSetCompleted = { completed, id ->
                        e.value = e.value.copy(
                            sets = e.value.sets.map {
                                if (it.id == id) it.copy(completed = completed) else it
                            }.toImmutableList()
                        )
                    },
                    onReorderRequest = {},
                )
            }
        }
    }
}
