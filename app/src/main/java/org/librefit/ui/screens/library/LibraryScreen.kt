/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.enums.pages.MainScreenPages
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.morphShape.AnimatedMorphShapes
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.models.UiLibraryCategory
import org.librefit.ui.models.UiLibraryRoutine
import org.librefit.ui.theme.LibreFitTheme

/**
 * The library screen shows the ready-to-use routine templates shipped with the app. Each template
 * can be added to the user's routines as an independent copy, or removed from the library with a
 * long press.
 */
@Composable
fun LibraryScreen(
    viewModel: LibraryScreenViewModel = hiltViewModel()
) {
    val categories by viewModel.libraryCategories.collectAsStateWithLifecycle()
    val justAddedIds by viewModel.justAddedRoutineIds.collectAsStateWithLifecycle()

    LibraryScreenContent(
        categories = categories,
        justAddedRoutineIds = justAddedIds,
        onAddToMyRoutines = viewModel::addToMyRoutines,
        onDeleteLibraryRoutine = viewModel::deleteLibraryRoutine
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryScreenContent(
    categories: List<UiLibraryCategory>,
    justAddedRoutineIds: Set<String>,
    onAddToMyRoutines: (String) -> Unit,
    onDeleteLibraryRoutine: (UiLibraryRoutine) -> Unit
) {
    if (categories.isEmpty()) {
        // Shown while templates are loading (first launch) or when the user removed them all
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.coming_soon),
                style = MaterialTheme.typography.headlineSmallEmphasized
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(top = 20.dp),
                    text = stringResource(R.string.library_header),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.library_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        categories.forEach { category ->
            item(key = category.categoryKey) {
                Text(
                    modifier = Modifier.padding(top = 15.dp),
                    text = category.categoryName,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            items(category.routines, key = { it.workoutId }) { routine ->
                LibraryRoutineCard(
                    routine = routine,
                    justAdded = routine.templateId in justAddedRoutineIds,
                    onAddToMyRoutines = onAddToMyRoutines,
                    onDeleteLibraryRoutine = onDeleteLibraryRoutine
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryRoutineCard(
    routine: UiLibraryRoutine,
    justAdded: Boolean,
    onAddToMyRoutines: (String) -> Unit,
    onDeleteLibraryRoutine: (UiLibraryRoutine) -> Unit
) {
    var expanded by rememberSaveable(routine.workoutId) { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    if (showDeleteDialog) {
        ConfirmDialog(
            title = routine.title,
            text = stringResource(R.string.remove_from_library_text),
            confirmText = stringResource(R.string.delete),
            onConfirm = {
                onDeleteLibraryRoutine(routine)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        expanded = !expanded
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteDialog = true
                    }
                )
                .padding(15.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = routine.title,
                    style = MaterialTheme.typography.titleLargeEmphasized
                )
                Icon(
                    painter = painterResource(R.drawable.ic_keyboard_double_arrow_up),
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (routine.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            val countText = stringResource(R.string.exercises_count, routine.exerciseNames.size)
            val durationText =
                if (routine.estimatedMinutes > 0) {
                    stringResource(R.string.estimated_duration, routine.estimatedMinutes)
                } else null
            Text(
                text = listOfNotNull(countText, durationText).joinToString(separator = " · "),
                style = MaterialTheme.typography.labelLargeEmphasized,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            AnimatedVisibility(visible = expanded && routine.exerciseNames.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    routine.exerciseNames.forEach { exerciseName ->
                        Text(
                            text = "• $exerciseName",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LibreFitButton(
                text = stringResource(
                    if (justAdded) R.string.added_to_my_routines else R.string.add_to_my_routines
                ),
                icon = painterResource(if (justAdded) R.drawable.ic_check else R.drawable.ic_add_circle),
                elevated = !justAdded,
                enabled = !justAdded,
                onClick = { onAddToMyRoutines(routine.templateId) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun LibraryScreenPreview() {

    val pagerState = rememberPagerState(
        initialPage = MainScreenPages.LIBRARY.ordinal,
        pageCount = { MainScreenPages.entries.size }
    )
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        LibreFitScaffold(
            title = buildAnnotatedString {
                GetAppNameInAnnotatedBuilder(MaterialTheme.typography.titleLargeEmphasized)
            },
            actions = persistentListOf({ }, { }, { }),
            actionsIcons = persistentListOf(
                painterResource(R.drawable.ic_favorite),
                painterResource(R.drawable.ic_info),
                painterResource(R.drawable.ic_settings)
            ),
            actionsElevated = persistentListOf(false, false, false),
            bottomBar = {
                NavigationBar {
                    MainScreenPages.entries.forEach { page ->
                        NavigationBarItem(
                            selected = pagerState.currentPage == page.ordinal,
                            onClick = { },
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        id = when (page) {
                                            MainScreenPages.LIBRARY -> R.drawable.ic_library
                                            MainScreenPages.HOME -> R.drawable.ic_home
                                            MainScreenPages.PROFILE -> R.drawable.ic_person
                                        }
                                    ),
                                    contentDescription = stringResource(
                                        id = when (page) {
                                            MainScreenPages.LIBRARY -> R.string.library
                                            MainScreenPages.HOME -> R.string.home
                                            MainScreenPages.PROFILE -> R.string.profile
                                        }
                                    )
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(
                                        id = when (page) {
                                            MainScreenPages.LIBRARY -> R.string.library
                                            MainScreenPages.HOME -> R.string.home
                                            MainScreenPages.PROFILE -> R.string.profile
                                        }
                                    )
                                )
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            androidx.compose.foundation.pager.HorizontalPager(
                state = androidx.compose.foundation.pager.rememberPagerState { 0 },
                contentPadding = innerPadding
            ) {
                LibraryScreenContent(
                    categories = emptyList(),
                    justAddedRoutineIds = emptySet(),
                    onAddToMyRoutines = {},
                    onDeleteLibraryRoutine = {}
                )
            }
        }
    }
}
