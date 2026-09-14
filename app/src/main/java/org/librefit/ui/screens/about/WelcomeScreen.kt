/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.about

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.librefit.R
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.morphShape.AnimatedMorphShapes
import org.librefit.ui.theme.LibreFitTheme


@Composable
fun WelcomeScreen(
    onNavigateToTutorialScreen: () -> Unit,
    onNavigateToMainScreen: () -> Unit,
    doNotShowWelcomeScreenAgain: () -> Unit
) {

    WelcomeScreenContent(
        onNavigateToTutorialScreen = onNavigateToTutorialScreen,
        onNavigateToMainScreen = onNavigateToMainScreen,
        doNotShowWelcomeScreenAgain = doNotShowWelcomeScreenAgain,
    )

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WelcomeScreenContent(
    onNavigateToTutorialScreen: () -> Unit,
    onNavigateToMainScreen: () -> Unit,
    doNotShowWelcomeScreenAgain: () -> Unit
) {
    /**
     * Only used to preview animation in android studio
     */
    val animatedColor by animateColorAsState(
        targetValue = Color.Unspecified,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        label = "ColorAnimation"
    )
    LibreFitScaffold { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            if (maxHeight > maxWidth) {
                LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier
                        .width(maxWidth)
                        .height(maxHeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    welcomeScreenContent(
                        onNavigateToTutorialScreen,
                        onNavigateToMainScreen,
                        doNotShowWelcomeScreenAgain
                    )
                }
            } else {
                LazyRow(
                    contentPadding = innerPadding,
                    modifier = Modifier
                        .width(maxWidth)
                        .height(maxHeight),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    welcomeScreenContent(
                        onNavigateToTutorialScreen,
                        onNavigateToMainScreen,
                        doNotShowWelcomeScreenAgain
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.welcomeScreenContent(
    onNavigateToTutorialScreen: () -> Unit,
    onNavigateToMainScreen: () -> Unit,
    doNotShowWelcomeScreenAgain: () -> Unit
) {
    item {
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedMorphShapes(
                morphIntervalMillis = 6000,
                globalRotationDurationMillis = 12000,
                colors = listOf(colorResource(R.color.ic_launcher_background)),
                shapeSize = 400.dp,
                roundedPolygons = polygons,
                onColorUpdate = {}
            )
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(220.dp)
            )
        }
    }
    item {
        ElevatedCard(
            shape = MaterialTheme.shapes.extraExtraLarge
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                verticalArrangement = Arrangement.spacedBy(60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.welcome_to))
                        appendLine()
                        GetAppNameInAnnotatedBuilder(MaterialTheme.typography.displayLargeEmphasized)
                    },
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center,
                )
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    val interactionSources = remember { List(2) { MutableInteractionSource() } }

                    ButtonGroup(
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                        overflowIndicator = {}
                    ) {
                        customItem(
                            buttonGroupContent = {
                                LibreFitButton(
                                    icon = painterResource(R.drawable.ic_help),
                                    modifier = Modifier

                                        .animateWidth(interactionSources[0]),
                                    text = stringResource(R.string.tutorial),
                                    interactionSource = interactionSources[0]
                                ) {
                                    onNavigateToTutorialScreen()
                                    doNotShowWelcomeScreenAgain()
                                }
                            },
                            menuContent = {}
                        )
                        customItem(
                            buttonGroupContent = {
                                LibreFitButton(
                                    icon = painterResource(R.drawable.ic_home),
                                    modifier = Modifier
                                        .animateWidth(interactionSources[1]),
                                    text = stringResource(R.string.home),
                                    interactionSource = interactionSources[1],
                                    elevated = false
                                ) {
                                    onNavigateToMainScreen()
                                    doNotShowWelcomeScreenAgain()
                                }
                            },
                            menuContent = {}
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        WelcomeScreenContent(
            onNavigateToTutorialScreen = {},
            onNavigateToMainScreen = {}
        ) {}
    }
}