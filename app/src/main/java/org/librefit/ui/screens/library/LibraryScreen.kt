/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.enums.pages.MainScreenPages
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.morphShape.AnimatedMorphShapes
import org.librefit.ui.theme.LibreFitTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryScreen() {
    //TODO: implement archived routines

    //TODO: implement a default routine

    val morphIntervalMillis = remember { 4000L }

    val polygons = remember {
        listOf(
            MaterialShapes.SoftBurst,
            MaterialShapes.Cookie6Sided,
            MaterialShapes.Pentagon,
            MaterialShapes.Pill,
            MaterialShapes.Diamond,
            MaterialShapes.Slanted,
            MaterialShapes.Gem,
            MaterialShapes.Oval,
        ).shuffled()
    }

    val colors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primaryFixed,
        MaterialTheme.colorScheme.surfaceContainerHighest,
    )

    var currentColor by remember {
        mutableStateOf(colors.random())
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedMorphShapes(
            morphIntervalMillis = morphIntervalMillis,
            globalRotationDurationMillis = 8000,
            colors = colors,
            shapeSize = 400.dp,
            roundedPolygons = polygons,
            onColorUpdate = { currentColor = it }
        )
        Text(
            text = stringResource(R.string.coming_soon),
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = contentColorFor(currentColor)
        )
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
            HorizontalPager(
                state = rememberPagerState { 0 },
                contentPadding = innerPadding
            ) {
                LibraryScreen()
            }
        }
    }
}