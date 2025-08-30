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

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.db.entity.ExerciseDC
import org.librefit.enums.exercise.FilterValue
import org.librefit.nav.Route
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.NoResultLottie
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter.exerciseEnumToStringId


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ExercisesScreen(
    addExercises: Boolean,
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel: ExercisesScreenViewModel = hiltViewModel()

    val filteredExerciseList by viewModel.filteredExerciseList.collectAsState()

    val query by viewModel.query.collectAsState()

    val filterValue by viewModel.filterValue.collectAsState()

    val selectedExercisesList by viewModel.selectedExercises.collectAsState()

    val selectedExercisesIds by viewModel.selectedExerciseIds.collectAsState()



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
                navController.navigateUp()
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    val actions = remember {
        if (addExercises) listOf {
            navController.navigateUp()
            sharedViewModel.setSelectedExercisesList(selectedExercisesList.map { it.toEntity() })
        } else listOf()
    }


    ExercisesScreenContent(
        addExercises = addExercises,
        selectedExercisesIdList = selectedExercisesIds,
        filteredExerciseList = filteredExerciseList,
        query = query,
        filterValue = filterValue,
        animatedVisibilityScope = animatedVisibilityScope,
        toggleSelectedExercise = viewModel::toggleSelectedExercise,
        updateQuery = viewModel::updateQuery,
        updateFilter = viewModel::updateFilter,
        actions = actions,
        navigateBack = navController::navigateUp,
        navigateToInfoExercise = {
            navController.navigate(Route.InfoExerciseScreen(0L, it)) { launchSingleTop = true }
        }
    )

}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.ExercisesScreenContent(
    addExercises: Boolean,
    selectedExercisesIdList: Set<String>,
    filteredExerciseList: List<UiExerciseDC>,
    query: String,
    filterValue: FilterValue,
    animatedVisibilityScope: AnimatedVisibilityScope,
    toggleSelectedExercise: (String) -> Unit,
    updateQuery: (String) -> Unit,
    updateFilter: (FilterValue) -> Unit,
    actions: List<() -> Unit>,
    navigateBack: () -> Unit,
    navigateToInfoExercise: (ExerciseDC) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()


    val lazyListState = rememberLazyListState()

    val isAtTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    val scrollToTop: () -> Unit = remember {
        {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
            }
        }
    }


    var isFilterExpanded by rememberSaveable { mutableStateOf(false) }


    LibreFitScaffold(
        title = AnnotatedString(stringResource(id = R.string.exercises)),
        navigateBack = navigateBack,
        actions = actions,
        actionsDescription = listOf(stringResource(R.string.add)),
        actionsEnabled = listOf(selectedExercisesIdList.isNotEmpty()),
        fabAction = if (isAtTop) null else scrollToTop,
        fabIcon = painterResource(R.drawable.ic_keyboard_double_arrow_up),
    ) { innerPadding ->
        LibreFitLazyColumn(
            innerPadding = innerPadding,
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
                        shape = CircleShape,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_search),
                                contentDescription = stringResource(R.string.search_exercise_field)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { updateQuery("") }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_cancel),
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
                ItemExerciseDC(
                    modifier = Modifier.animateItem(),
                    addExercises = addExercises,
                    exercise = exercise,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onAddToggle = { toggleSelectedExercise(exercise.id) },
                    isSelected = exercise.id in selectedExercisesIdList,
                    onInfo = { navigateToInfoExercise(exercise.toEntity()) }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharedTransitionScope.ItemExerciseDC(
    modifier: Modifier,
    addExercises: Boolean,
    exercise: UiExerciseDC,
    isSelected: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onAddToggle: () -> Unit,
    onInfo: () -> Unit,
) {
    val context = LocalContext.current

    val image = remember(exercise.images[0]) {
        BitmapFactory.decodeStream(
            context.assets.open(exercise.images[0])
        ).asImageBitmap()
    }

    ToggleButton(
        checked = isSelected,
        onCheckedChange = {
            if (addExercises) {
                onAddToggle()
            } else {
                onInfo()
            }
        },
        shapes = ToggleButtonDefaults.shapes(
            shape = MaterialTheme.shapes.extraLargeIncreased,
            pressedShape = MaterialTheme.shapes.extraSmall,
            checkedShape = MaterialTheme.shapes.medium
        ),
        contentPadding = ButtonDefaults.MediumContentPadding,
        modifier = modifier.height(130.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                bitmap = image,
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(exercise.id),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .width(110.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
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
                onClick = onInfo,
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = stringResource(R.string.details)
                )
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun ExercisesScreenPreview() {
    var query by remember { mutableStateOf("") }

    var filterValue by remember { mutableStateOf(FilterValue()) }

    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                ExercisesScreenContent(
                    animatedVisibilityScope = this,
                    addExercises = false,
                    selectedExercisesIdList = setOf(),
                    filteredExerciseList = List(20) {
                        UiExerciseDC(
                            id = "$it",
                            name = "Exercise $it",
                            images = persistentListOf("3_4_Sit-Up/0.jpg")
                        )
                    },
                    query = query,
                    filterValue = filterValue,
                    toggleSelectedExercise = {},
                    updateQuery = { query = it },
                    updateFilter = { filterValue = it },
                    actions = listOf {},
                    navigateBack = {},
                    navigateToInfoExercise = {}
                )
            }
        }
    }
}