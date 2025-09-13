/*
 * Copyright (c) 2025. LibreFit
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

package org.librefit.ui.screens.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import org.librefit.R
import org.librefit.enums.pages.MainScreenPages
import org.librefit.ui.components.LibreFitAppName.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.morphShape.AnimatedMorphShapes
import org.librefit.ui.theme.LibreFitTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryScreen(
    innerPadding: PaddingValues
) {
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

    /**
     * Only used to preview animation in android studio
     */
    val animatedColor by animateColorAsState(
        targetValue = currentColor,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        label = "ColorAnimation"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
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
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        LibreFitScaffold(
            title = buildAnnotatedString {
                GetAppNameInAnnotatedBuilder(MaterialTheme.typography.titleLargeEmphasized)
            },
            actions = listOf({ }, { }, { }),
            actionsIcons = listOf(
                painterResource(R.drawable.ic_favorite),
                painterResource(R.drawable.ic_info),
                painterResource(R.drawable.ic_settings)
            ),
            actionsElevated = listOf(false, false, false),
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
            LibraryScreen(innerPadding)
        }
    }
}