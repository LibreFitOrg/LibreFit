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

package org.librefit.ui.screens.exercises

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.librefit.MainApplication
import org.librefit.R
import org.librefit.ui.components.ConfirmDialog
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.animations.NoResultLottie
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.modalBottomSheets.ExerciseDetailModalBottomSheet
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.util.ExerciseDC
import org.librefit.util.exerciseEnumToStringId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    addExercises: Boolean,
    navigateBack: () -> Unit,
    viewModel: SharedViewModel
) {
    val selectedExercisesList = remember { mutableStateListOf<ExerciseDC>() }

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !showExitDialog && selectedExercisesList.isNotEmpty()) {
        showExitDialog = true
    }

    if (showExitDialog) {
        ConfirmDialog(
            title = stringResource(R.string.exit),
            text = stringResource(id = R.string.exit_add_exercise),
            onConfirm = {
                navigateBack()
                showExitDialog = false
            },
            onDismiss = { showExitDialog = false }
        )
    }

    val lazyListState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()

    CustomScaffold(
        title = stringResource(id = R.string.exercises),
        navigateBack = navigateBack,
        actions = if (addExercises) listOf {
            viewModel.addSelectedExerciseToList(selectedExercisesList)
            navigateBack()
        } else listOf(),
        actionsDescription = listOf(stringResource(R.string.add)),
        actionsEnabled = listOf(selectedExercisesList.isNotEmpty()),
        fabAction = {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
            }
        },
        fabIcon = Icons.Default.KeyboardArrowUp,
    ) { innerPadding ->
        AddExerciseScreenContent(
            addExercises = addExercises,
            innerPadding = innerPadding,
            selectedExercisesList = selectedExercisesList,
            listState = lazyListState
        )
    }
}

@Composable
private fun AddExerciseScreenContent(
    addExercises: Boolean,
    innerPadding: PaddingValues,
    selectedExercisesList: MutableList<ExerciseDC>,
    listState: LazyListState
) {
    val exerciseList = MainApplication.exercisesList

    val viewModel: ExercisesScreenViewModel = viewModel()


    var isFilterExpanded = rememberSaveable { mutableStateOf(false) }

    /**
     * Holds the information to show in [ExerciseDetailModalBottomSheet]
     */
    var selectedExercise by remember { mutableStateOf<ExerciseDC?>(null) }

    var isModalSheetOpen by remember { mutableStateOf(false) }

    /**
     * Query used to filter exercises based on the name
     */
    var query by remember { mutableStateOf("") }

    LazyColumn(
        state = listState,
        modifier = Modifier.padding(innerPadding)
    ) {
        // Search bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TextField(
                    value = query,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp),
                    onValueChange = {
                        query = it
                    },
                    shape = RoundedCornerShape(40.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_exercise_field)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.delete)
                                )
                            }
                        }
                    },
                    label = { Text(text = stringResource(id = R.string.search_exercise_field)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
            }
        }

        // Card to let the user filter the exercises list
        item { FiltersCard(isFilterExpanded = isFilterExpanded, viewModel = viewModel) }

        if (
            exerciseList.any {
                it.name.contains(query, ignoreCase = true) && viewModel.filterExercise(it)
            }
        ) {
            item { HorizontalDivider() }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NoResultLottie()
                    Text(
                        text = stringResource(id = R.string.no_exercise_found),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        //Filtered list of exercises
        items(
            items = exerciseList.filter { exercise ->
                exercise.name.contains(query, ignoreCase = true) && viewModel.filterExercise(
                    exercise
                )
            },
            key = { exercise -> exercise.id }
        ) { exercise ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
                    .height(100.dp)
                    .clickable(
                        enabled = addExercises
                    ) {
                        if (selectedExercisesList.contains(exercise)) {
                            selectedExercisesList.remove(exercise)
                        } else {
                            selectedExercisesList.add(exercise)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (addExercises) {
                    Checkbox(
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                        checked = selectedExercisesList.contains(exercise),
                        onCheckedChange = {
                            if (selectedExercisesList.contains(exercise)) {
                                selectedExercisesList.remove(exercise)
                            } else {
                                selectedExercisesList.add(exercise)
                            }
                        }
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (addExercises) 0.dp else 20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(id = exerciseEnumToStringId(exercise.category)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (exercise.equipment != null) {
                        Text(
                            text = stringResource(id = exerciseEnumToStringId(exercise.equipment)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                IconButton(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                    onClick = {
                        selectedExercise = exercise
                        isModalSheetOpen = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.details)
                    )
                }
            }
            HorizontalDivider(Modifier.animateItem())
        }
        bottomMargin()
    }

    // Opened by info icon (in the filtered list), it shows the details of an exercise
    if (isModalSheetOpen) {
        ExerciseDetailModalBottomSheet(exercise = selectedExercise!!) { isModalSheetOpen = false }
    }
}


@Preview
@Composable
private fun AddExerciseScreenPreview() {
    ExercisesScreen(
        true,
        {},
        viewModel()
    )
}