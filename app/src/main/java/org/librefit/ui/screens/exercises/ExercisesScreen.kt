/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.exercises

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.db.entity.ExerciseDC
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.FilterValue
import org.librefit.enums.userPreferences.ThemeMode
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

    val filteredExerciseList by viewModel.filteredExerciseList.collectAsStateWithLifecycle()

    val query by viewModel.query.collectAsStateWithLifecycle()

    val filterValue by viewModel.filterValue.collectAsStateWithLifecycle()

    val selectedExercisesList by viewModel.selectedExercises.collectAsStateWithLifecycle()

    val selectedExercisesIds by viewModel.selectedExerciseIds.collectAsStateWithLifecycle()

    val isSupporter by viewModel.isSupporter.collectAsStateWithLifecycle()



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
            navController.navigate(Route.InfoExerciseScreen(0L, it.id)) { launchSingleTop = true }
        },
        navigateToEditExercise = {
            navController.navigate(
                if (isSupporter) Route.EditExerciseScreen() else Route.SupportScreen(supporterInfo = true)
            ) { launchSingleTop = true }
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
    navigateToInfoExercise: (ExerciseDC) -> Unit,
    navigateToEditExercise: () -> Unit
) {


    var isFilterExpanded by rememberSaveable { mutableStateOf(false) }


    LibreFitScaffold(
        title = AnnotatedString(stringResource(id = R.string.exercises)),
        navigateBack = navigateBack,
        actions = actions,
        actionsDescription = listOf(stringResource(R.string.add)),
        actionsEnabled = listOf(selectedExercisesIdList.isNotEmpty()),
        fabAction = navigateToEditExercise,
        fabText = stringResource(R.string.create_exercise),
        fabIcon = painterResource(R.drawable.ic_add),
    ) { innerPadding ->
        LibreFitLazyColumn(
            innerPadding = innerPadding
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
                key = { _, exercise -> exercise.id }
            ) { _, exercise ->
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
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val model = remember { exercise.images.firstOrNull() }
            AsyncImage(
                model = model?.let { "file:///android_asset/${it}" },
                fallback = painterResource(R.drawable.no_image),
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                colorFilter = if (model == null) ColorFilter.tint(LocalContentColor.current) else null,
                filterQuality = FilterQuality.High,
                modifier = Modifier
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(exercise.id),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .size(100.dp)
                    .clip(MaterialTheme.shapes.large)
            )
            Column(
                modifier = Modifier.padding(start = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
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
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(device = "id:medium_phone")
@Composable
private fun ExercisesScreenPreview() {
    var query by remember { mutableStateOf("running") }

    var filterValue by remember { mutableStateOf(FilterValue()) }

    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                ExercisesScreenContent(
                    animatedVisibilityScope = this,
                    addExercises = false,
                    selectedExercisesIdList = setOf("1"),
                    filteredExerciseList = listOf(
                        UiExerciseDC(
                            id = "1",
                            name = "Running, Treadmill",
                            images = persistentListOf("Running_Treadmill/0.webp"),
                            equipment = Equipment.BODY_ONLY,
                            category = Category.STRENGTH
                        ),
                        UiExerciseDC(
                            id = "2",
                            name = "Trail Running/Walking",
                            images = persistentListOf("Trail_Running_Walking/0.webp"),
                            equipment = Equipment.BODY_ONLY,
                            category = Category.STRETCHING
                        ),
                        UiExerciseDC(
                            id = "3",
                            name = "Alternating Cable Shoulder Press",
                            images = persistentListOf("Alternating_Cable_Shoulder_Press/0.webp"),
                            equipment = Equipment.MACHINE,
                            category = Category.STRENGTH
                        ),
                        UiExerciseDC(
                            id = "4",
                            name = "Alternating Deltoid Raise",
                            images = persistentListOf("Alternating_Deltoid_Raise/0.webp"),
                            equipment = Equipment.OTHER,
                            category = Category.STRENGTH
                        ),
                        UiExerciseDC(
                            id = "5",
                            name = "Alternating Floor Press",
                            images = persistentListOf("Alternating_Floor_Press/0.webp"),
                            equipment = Equipment.FOAM_ROLL,
                            category = Category.STRETCHING
                        ),
                    ),
                    query = query,
                    filterValue = filterValue,
                    toggleSelectedExercise = {},
                    updateQuery = { query = it },
                    updateFilter = { filterValue = it },
                    actions = listOf {},
                    navigateBack = {},
                    navigateToInfoExercise = {},
                    navigateToEditExercise = {}
                )
            }
        }
    }
}