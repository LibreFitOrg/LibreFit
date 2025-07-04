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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.data.ExerciseDC
import org.librefit.enums.exercise.FilterValue
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.NoResultLottie
import org.librefit.ui.components.bottomMargin
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.components.modalBottomSheets.ExerciseDetailModalBottomSheet
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter.exerciseEnumToStringId


@Composable
fun ExercisesScreen(
    addExercises: Boolean,
    navigateBack: () -> Unit,
    sharedViewModel: SharedViewModel
) {
    val selectedExercisesList = remember { mutableStateListOf<ExerciseDC>() }

    var showConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !showConfirmDialog && selectedExercisesList.isNotEmpty()) {
        showConfirmDialog = true
    }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(R.string.quit_adding_exercises_question),
            text = stringResource(R.string.quit_adding_exercises_text),
            confirmText = stringResource(R.string.quit_dialog),
            onConfirm = {
                navigateBack()
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }


    val viewModel: ExercisesScreenViewModel = hiltViewModel()

    val filteredExerciseList by viewModel.filteredExerciseList.collectAsState()

    val query by viewModel.query.collectAsState()

    val filterValue by viewModel.filterValue.collectAsState()


    ExercisesScreenContent(
        addExercises = addExercises,
        selectedExercisesList = selectedExercisesList,
        filteredExerciseList = filteredExerciseList,
        query = query,
        updateQuery = viewModel::updateQuery,
        updateFilter = viewModel::updateFilter,
        filterValue = filterValue,
        actions = if (addExercises) listOf {
            sharedViewModel.addSelectedExerciseToList(selectedExercisesList)
            navigateBack()
        } else listOf(),
        navigateBack = navigateBack,
    )

}

@Composable
private fun ExercisesScreenContent(
    addExercises: Boolean,
    selectedExercisesList: MutableList<ExerciseDC>,
    filteredExerciseList: List<ExerciseDC>,
    query: String,
    updateQuery: (String) -> Unit,
    updateFilter: (FilterValue) -> Unit,
    filterValue: FilterValue,
    actions: List<() -> Unit>,
    navigateBack: () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()


    val lazyListState = rememberLazyListState()

    val isAtTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    val scrollToTop: () -> Unit = {
        coroutineScope.launch {
            lazyListState.animateScrollToItem(0)
        }
    }


    var isFilterExpanded by rememberSaveable { mutableStateOf(false) }

    /**
     * Holds the information to show in [ExerciseDetailModalBottomSheet]
     */
    var selectedExercise by remember { mutableStateOf(ExerciseDC()) }

    var isModalSheetOpen by rememberSaveable { mutableStateOf(false) }


    LibreFitScaffold(
        title = AnnotatedString(stringResource(id = R.string.exercises)),
        navigateBack = navigateBack,
        actions = actions,
        actionsDescription = listOf(stringResource(R.string.add)),
        actionsEnabled = listOf(selectedExercisesList.isNotEmpty()),
        fabAction = if (isAtTop) null else scrollToTop,
        fabIcon = ImageVector.vectorResource(R.drawable.ic_keyboard_double_arrow_up),
    ) { innerPadding ->
        LibreFitLazyColumn(
            innerPadding = innerPadding,
            verticalSpacing = 15.dp,
            lazyListState = lazyListState
        ) {
            // Search bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TextField(
                        value = query,
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = updateQuery,
                        shape = RoundedCornerShape(40.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                                contentDescription = stringResource(R.string.search_exercise_field)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { updateQuery("") }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_cancel),
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
            item {
                FiltersCard(
                    isFilterExpanded = isFilterExpanded,
                    updateCardExpansion = { isFilterExpanded = !isFilterExpanded },
                    updateFilter = updateFilter,
                    filterValue = filterValue
                )
            }

            if (filteredExerciseList.isEmpty()) {
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

            //Filtered list of exercises sorted by matching score
            itemsIndexed(
                items = filteredExerciseList,
                key = { index, exercise -> exercise.id }
            ) { index, exercise ->
                ElevatedCard(
                    modifier = Modifier
                        .animateItem()
                        .clip(CardDefaults.elevatedShape)
                        .clickable(
                            enabled = addExercises
                        ) {
                            if (selectedExercisesList.contains(exercise)) {
                                selectedExercisesList.remove(exercise)
                            } else {
                                selectedExercisesList.add(exercise)
                            }
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(
                            top = 20.dp,
                            bottom = 20.dp,
                            start = 10.dp,
                            end = 10.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (addExercises) {
                            Checkbox(
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
                                .padding(start = 10.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(exerciseEnumToStringId(exercise.category)),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (exercise.equipment != null) {
                                Text(
                                    text = stringResource(exerciseEnumToStringId(exercise.equipment)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        IconButton(
                            modifier = Modifier.padding(start = 10.dp),
                            onClick = {
                                selectedExercise = exercise
                                isModalSheetOpen = true
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.details)
                            )
                        }
                    }
                }
            }
            bottomMargin()
        }
    }

    // Opened by info icon (in the filtered list), it shows the details of an exercise
    if (isModalSheetOpen) {
        ExerciseDetailModalBottomSheet(exercise = selectedExercise) { isModalSheetOpen = false }
    }
}


@Preview
@Composable
private fun ExercisesScreenPreview() {
    var query by remember { mutableStateOf("") }

    var filterValue by remember { mutableStateOf(FilterValue()) }

    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        ExercisesScreenContent(
            addExercises = false,
            selectedExercisesList = remember { mutableStateListOf() },
            filteredExerciseList = List(20) { ExerciseDC(id = "$it", name = "Exercise $it") },
            query = query,
            updateQuery = { query = it },
            updateFilter = { filterValue = it },
            filterValue = filterValue,
            actions = listOf {},
            navigateBack = {},
        )
    }
}