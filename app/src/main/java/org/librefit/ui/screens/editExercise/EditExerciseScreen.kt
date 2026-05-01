/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.editExercise

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import org.librefit.R
import org.librefit.db.entity.ExerciseDC
import org.librefit.enums.SuccessMessage
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.ExerciseProperty
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.nav.Route
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter.exerciseEnumToStringId

@Composable
fun SharedTransitionScope.EditExerciseScreen(
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    id: Long, // Used only for transition animation
    exerciseDCid: String,
    viewModel: EditExerciseScreenViewModel = hiltViewModel()
) {

    val exerciseDC by viewModel.exerciseDC.collectAsStateWithLifecycle()

    val isCreateMode = exerciseDC.id.isBlank()

    EditExerciseScreenContent(
        stringId = if (id == 0L) "" else id.toString(),
        exerciseDcId = exerciseDC.id,
        isCreateMode = isCreateMode,
        name = exerciseDC.name,
        force = exerciseDC.force,
        level = exerciseDC.level,
        mechanic = exerciseDC.mechanic,
        equipment = exerciseDC.equipment,
        primaryMuscles = exerciseDC.primaryMuscles,
        secondaryMuscles = exerciseDC.secondaryMuscles,
        instructions = exerciseDC.instructions,
        category = exerciseDC.category,
        images = exerciseDC.images,
        navigateBack = navController::navigateUp,
        animatedVisibilityScope = animatedVisibilityScope,
        updateValue = viewModel::updateValue,
        updatePrimaryMuscles = viewModel::updatePrimaryMuscles,
        updateSecondaryMuscles = viewModel::updateSecondaryMuscles,
        saveExercise = viewModel::saveExercise,
        updateName = viewModel::updateName,
        updateInstructions = viewModel::updateInstructions,
        navigateToSuccessScreen = {
            navController.navigate(Route.SuccessScreen(SuccessMessage.EXERCISE_SAVED)) {
                launchSingleTop = true
                popUpTo(
                    Route.EditExerciseScreen(
                        id = id,
                        exerciseDCid = exerciseDCid
                    )
                ) { inclusive = true }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharedTransitionScope.EditExerciseScreenContent(
    exerciseDcId: String,
    stringId: String,
    isCreateMode: Boolean,
    name: String,
    force: Force?,
    level: Level,
    mechanic: Mechanic?,
    equipment: Equipment?,
    primaryMuscles: List<Muscle>,
    secondaryMuscles: List<Muscle>,
    instructions: List<String>,
    images: List<String>,
    category: Category,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navigateBack: () -> Unit,
    updateValue: (ExerciseProperty) -> Unit,
    updatePrimaryMuscles: (Muscle) -> Unit,
    updateSecondaryMuscles: (Muscle) -> Unit,
    updateInstructions: (String) -> Unit,
    updateName: (String) -> Unit,
    saveExercise: () -> Unit,
    navigateToSuccessScreen: () -> Unit
) {
    val showConfirmExitDialog = rememberSaveable { mutableStateOf(false) }

    if (showConfirmExitDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.discard_changes_question),
            text = stringResource(R.string.discard_changes_text),
            confirmText = stringResource(R.string.discard_dialog),
            onConfirm = {
                navigateBack()
                showConfirmExitDialog.value = false
            },
            onDismiss = {
                showConfirmExitDialog.value = false
            }
        )
    }

    BackHandler(enabled = !showConfirmExitDialog.value) {
        showConfirmExitDialog.value = true
    }

    LibreFitScaffold(
        title = AnnotatedString(text = stringResource(if (isCreateMode) R.string.create_exercise else R.string.edit_exercise)),
        navigateBack = {
            showConfirmExitDialog.value = true
        },
        actions = listOf {
            saveExercise()
            navigateToSuccessScreen()
        },
        actionsEnabled = listOf(name.isNotEmpty()),
        actionsDescription = listOf(stringResource(R.string.save)),
    ) { innerPadding ->
        LibreFitLazyColumn(
            innerPadding = innerPadding,
            verticalSpacing = 20.dp
        ) {
            item {
                // TODO: implement display all images (like in a horizontal pager)
                val model = remember(images) { images.firstOrNull() }
                AsyncImage(
                    model = model?.let { "file:///android_asset/${it}" },
                    fallback = painterResource(R.drawable.no_image),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.High,
                    colorFilter = if (model == null) ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant) else null,
                    modifier = Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(stringId + exerciseDcId),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .border(
                            0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.extraLarge
                        ),
                )
            }
            item {
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = name,
                        placeholder = { Text(text = stringResource(R.string.name)) },
                        onValueChange = updateName,
                        shape = MaterialTheme.shapes.largeIncreased,
                        modifier = Modifier.weight(1f),
                        isError = name.isEmpty()
                    )
                    EditExercisePropertyItem(
                        label = stringResource(R.string.force),
                        values = listOf(force),
                        options = Force.entries,
                        updateValue = updateValue
                    )
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditExercisePropertyItem(
                        label = stringResource(R.string.level),
                        values = listOf(level),
                        options = Level.entries,
                        updateValue = updateValue
                    )
                    EditExercisePropertyItem(
                        label = stringResource(R.string.mechanic),
                        values = listOf(mechanic),
                        options = Mechanic.entries,
                        updateValue = updateValue
                    )
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditExercisePropertyItem(
                        label = stringResource(R.string.primary_muscles),
                        values = primaryMuscles,
                        options = Muscle.entries,
                        updateValue = { value ->
                            (value as? Muscle)?.let {
                                updatePrimaryMuscles(it)
                            }
                        }
                    )
                    EditExercisePropertyItem(
                        label = stringResource(R.string.secondary_muscles),
                        values = secondaryMuscles,
                        options = Muscle.entries,
                        updateValue = { value ->
                            (value as? Muscle)?.let {
                                updateSecondaryMuscles(it)
                            }
                        }
                    )
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditExercisePropertyItem(
                        label = stringResource(R.string.equipment),
                        values = listOf(equipment),
                        options = Equipment.entries,
                        updateValue = updateValue
                    )
                    EditExercisePropertyItem(
                        label = stringResource(R.string.category),
                        values = listOf(category),
                        options = Category.entries,
                        updateValue = updateValue
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = instructions.joinToString(separator = "\n") { it },
                    placeholder = { Text(text = stringResource(R.string.instructions)) },
                    onValueChange = updateInstructions,
                    shape = MaterialTheme.shapes.largeIncreased,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RowScope.EditExercisePropertyItem(
    label: String,
    values: List<ExerciseProperty?>,
    options: List<ExerciseProperty>,
    updateValue: (ExerciseProperty) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    val resources = LocalResources.current

    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier
                .clickable {
                    expanded = !expanded
                    focusRequester.requestFocus()
                }
                .focusRequester(focusRequester)
                .focusable()
        ) {
            OutlinedTextField(
                shape = MaterialTheme.shapes.largeIncreased,
                readOnly = true,
                value = if (values.isEmpty() || values.all { it == null }) {
                    ""
                } else {
                    values.joinToString { value ->
                        resources.getString(exerciseEnumToStringId(value))
                    }
                },
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
                options.forEach { enum ->
                    DropdownMenuItem(
                        onClick = {
                            updateValue(enum)
                            expanded = false
                        },
                        text = {
                            Text(
                                text = stringResource(exerciseEnumToStringId(enum))
                            )
                        },
                        trailingIcon = if (enum in values) {
                            {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = stringResource(R.string.checkbox)
                                )
                            }
                        } else null,
                        modifier = Modifier.background(
                            if (enum in values) MaterialTheme.colorScheme.inversePrimary
                                .copy(0.3f) else Color.Unspecified
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EditExerciseScreenContentPreview() {

    val e = ExerciseDC(
        primaryMuscles = listOf(Muscle.FOREARMS, Muscle.ABDOMINALS)
    )
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                EditExerciseScreenContent(
                    stringId = "",
                    exerciseDcId = "",
                    isCreateMode = true,
                    navigateBack = {},
                    name = e.name,
                    force = e.force,
                    level = e.level,
                    mechanic = e.mechanic,
                    equipment = e.equipment,
                    primaryMuscles = e.primaryMuscles,
                    secondaryMuscles = e.secondaryMuscles,
                    instructions = e.instructions,
                    category = e.category,
                    images = e.images,
                    animatedVisibilityScope = this,
                    saveExercise = {},
                    updatePrimaryMuscles = {},
                    updateSecondaryMuscles = {},
                    updateValue = {},
                    updateInstructions = {},
                    updateName = {},
                    navigateToSuccessScreen = {}
                )
            }
        }
    }
}