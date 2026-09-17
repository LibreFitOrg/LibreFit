/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.exercises

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.db.entity.ExerciseDC
import org.librefit.enums.exercise.Category
import org.librefit.enums.exercise.Equipment
import org.librefit.enums.exercise.ExerciseProperty
import org.librefit.enums.exercise.FilterValue
import org.librefit.enums.exercise.Force
import org.librefit.enums.exercise.Level
import org.librefit.enums.exercise.Mechanic
import org.librefit.enums.exercise.Muscle
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.NoResultLottie
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.models.UiExerciseDC
import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter.exerciseEnumToStringId
import kotlin.reflect.KClass


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ExercisesScreen(
    addExercises: Boolean,
    sharedViewModel: SharedViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToInfoExercise: (ExerciseDC) -> Unit,
    onNavigateToEditExercise: () -> Unit,
    onNavigateToSupportScreen: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: ExercisesScreenViewModel = hiltViewModel()
) {

    val filteredExerciseList by viewModel.filteredExerciseList.collectAsStateWithLifecycle()

    val query by viewModel.query.collectAsStateWithLifecycle()

    val filterValue by viewModel.filterValue.collectAsStateWithLifecycle()

    val selectedExercisesList by viewModel.selectedExercises.collectAsStateWithLifecycle()

    val selectedExercisesIds by viewModel.selectedExerciseIds.collectAsStateWithLifecycle()

    val isSupporter by viewModel.isSupporter.collectAsStateWithLifecycle()

    val showExercisesImages by viewModel.showExercisesImages.collectAsStateWithLifecycle()

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
                onNavigateBack()
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    val actions = remember {
        if (addExercises) persistentListOf({
            onNavigateBack()
            sharedViewModel.setSelectedExercisesList(selectedExercisesList.map { it.toEntity() })
        }) else persistentListOf()
    }


    ExercisesScreenContent(
        addExercises = addExercises,
        selectedExercisesIdList = selectedExercisesIds,
        filteredExerciseList = filteredExerciseList,
        query = query,
        filterValue = filterValue,
        showExercisesImages = showExercisesImages,
        animatedVisibilityScope = animatedVisibilityScope,
        toggleSelectedExercise = viewModel::toggleSelectedExercise,
        updateQuery = viewModel::updateQuery,
        updateFilter = viewModel::updateFilter,
        actions = actions,
        navigateBack = onNavigateBack,
        navigateToInfoExercise = onNavigateToInfoExercise,
        navigateToEditExercise = {
            if (isSupporter) onNavigateToEditExercise() else onNavigateToSupportScreen()
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
    showExercisesImages: Boolean?,
    animatedVisibilityScope: AnimatedVisibilityScope,
    toggleSelectedExercise: (String) -> Unit,
    updateQuery: (String) -> Unit,
    updateFilter: (FilterValue) -> Unit,
    actions: ImmutableList<() -> Unit>,
    navigateBack: () -> Unit,
    navigateToInfoExercise: (ExerciseDC) -> Unit,
    navigateToEditExercise: () -> Unit
) {


    LibreFitScaffold(
        title = AnnotatedString(stringResource(id = R.string.exercises)),
        navigateBack = navigateBack,
        actions = actions,
        actionsDescription = persistentListOf(stringResource(R.string.add)),
        actionsEnabled = persistentListOf(selectedExercisesIdList.isNotEmpty()),
        fabAction = navigateToEditExercise,
        fabText = stringResource(R.string.create_exercise),
        fabIcon = painterResource(R.drawable.ic_add),
    ) { innerPadding ->
        LibreFitLazyColumn(
            innerPadding = innerPadding
        ) {
            // search bar + filters
            item {
                val searchBarState = rememberSearchBarState()
                val textFieldState = rememberTextFieldState(query)
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()

                // Context controllers for clearing focus and dismissing the keyboard
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current

                val hasQuery = textFieldState.text.isNotEmpty()
                val isSearchActive = isFocused || hasQuery

                // Central exit search action
                val onExitSearch: () -> Unit = {
                    textFieldState.edit { replace(0, length, "") }
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }

                // Intercept system back gesture when searching
                BackHandler(enabled = isSearchActive) {
                    onExitSearch()
                }

                // Width animation for search bar
                val animatedHorizontalPadding by animateDpAsState(
                    targetValue = if (isSearchActive) 4.dp else 16.dp,
                    label = "SearchBarWidthAnimation"
                )

                // The input is the single writer of the query; the ViewModel deduplicates and debounces it.
                LaunchedEffect(textFieldState) {
                    snapshotFlow { textFieldState.text }
                        .collect { text -> updateQuery(text.toString()) }
                }


                var isFilterExpanded by rememberSaveable { mutableStateOf(true) }



                ElevatedCard(
                    modifier = Modifier.padding(horizontal = animatedHorizontalPadding),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    // search bar
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = SearchBarDefaults.inputFieldShape,
                            color = SearchBarDefaults.colors().containerColor,
                            tonalElevation = SearchBarDefaults.TonalElevation,
                            shadowElevation = SearchBarDefaults.ShadowElevation,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SearchBarDefaults.InputField(
                                textFieldState = textFieldState,
                                searchBarState = searchBarState,
                                interactionSource = interactionSource,
                                onSearch = {
                                    // Hide keyboard when IME search action is tapped
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },
                                placeholder = { Text(stringResource(R.string.search_exercise_field)) },
                                leadingIcon = {
                                    // Animated transition between Search icon and Back button
                                    AnimatedContent(
                                        targetState = hasQuery,
                                        label = "LeadingIconCrossfade"
                                    ) { showBackArrow ->
                                        if (showBackArrow) {
                                            IconButton(onClick = onExitSearch) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_arrow_back),
                                                    contentDescription = stringResource(R.string.navigate_back)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_search),
                                                contentDescription = stringResource(R.string.search_exercise_field)
                                            )
                                        }
                                    }
                                },
                                trailingIcon = {
                                    IconToggleButton(
                                        checked = isFilterExpanded,
                                        onCheckedChange = { isFilterExpanded = it },
                                        colors = IconButtonDefaults.iconToggleButtonVibrantColors()
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_filter),
                                            contentDescription = stringResource(R.string.filters)
                                        )
                                    }
                                }
                            )
                        }
                        // Filters
                        AnimatedVisibility(
                            visible = isFilterExpanded,
                            label = "FiltersAnimation"
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(all = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    ExerciseProperty.propertiesPairsByEnum.forEach { propertiesPair ->
                                        ItemFilter(
                                            pair = propertiesPair,
                                            update = updateFilter,
                                            value = filterValue
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 15.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.show_only_custom_exercises),
                                        modifier = Modifier.weight(1f),
                                    )
                                    Switch(
                                        checked = filterValue.showOnlyCustomExercises,
                                        onCheckedChange = {
                                            updateFilter(filterValue.copy(showOnlyCustomExercises = it))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
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
                    showExercisesImages = showExercisesImages,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onAddToggle = { toggleSelectedExercise(exercise.id) },
                    isSelected = exercise.id in selectedExercisesIdList,
                    onInfo = { navigateToInfoExercise(exercise.toEntity()) }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemFilter(
    pair: Pair<List<ExerciseProperty?>, KClass<out ExerciseProperty>>,
    update: (FilterValue) -> Unit,
    value: FilterValue
) {
    val options: List<ExerciseProperty?> = pair.first

    val enumType = pair.second

    val propertyFilterValue: ExerciseProperty? = when (enumType) {
        Level::class -> value.level
        Force::class -> value.force
        Mechanic::class -> value.mechanic
        Muscle::class -> value.muscles
        Equipment::class -> value.equipment
        Category::class -> value.category
        else -> null
    }

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.width(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(
                id = when (enumType) {
                    Level::class -> R.string.level
                    Force::class -> R.string.force
                    Mechanic::class -> R.string.mechanic
                    Muscle::class -> R.string.muscles
                    Equipment::class -> R.string.equipment
                    Category::class -> R.string.category
                    else -> R.string.any
                }
            )
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                shape = MaterialTheme.shapes.large,
                readOnly = true,
                value = stringResource(exerciseEnumToStringId(propertyFilterValue)),
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
                            when (enumType) {
                                Force::class -> update(value.copy(force = enum as Force?))
                                Level::class -> update(value.copy(level = enum as Level?))
                                Mechanic::class -> update(value.copy(mechanic = enum as Mechanic?))
                                Muscle::class -> update(value.copy(muscles = enum as Muscle?))
                                Equipment::class -> update(value.copy(equipment = enum as Equipment?))
                                Category::class -> update(value.copy(category = enum as Category?))
                                else -> {}
                            }
                            expanded = false
                        },
                        text = {
                            Text(
                                text = stringResource(exerciseEnumToStringId(enum))
                            )
                        },
                        trailingIcon = if (propertyFilterValue == enum) {
                            {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = stringResource(R.string.checkbox)
                                )
                            }
                        } else null,
                        modifier = Modifier.background(
                            if (propertyFilterValue == enum) MaterialTheme.colorScheme.inversePrimary
                                .copy(0.3f) else Color.Unspecified
                        )
                    )
                }
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
    showExercisesImages: Boolean?,
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
        shapes = ToggleButtonShapes(
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
            if (showExercisesImages == true) {
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
            }
            Column(
                modifier = Modifier.padding(
                    start = (if (showExercisesImages == true) 20 else 10).dp
                ),
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
@Preview
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
                    showExercisesImages = false,
                    updateQuery = { query = it },
                    updateFilter = { filterValue = it },
                    actions = persistentListOf({}),
                    navigateBack = {},
                    navigateToInfoExercise = {},
                    navigateToEditExercise = {}
                )
            }
        }
    }
}