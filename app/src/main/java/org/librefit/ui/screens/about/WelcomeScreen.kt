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
 *
 * LibreFit is subject to additional terms covering author attribution and
 * trademark usage, as found in the accompanying ADDITIONAL_TERMS.md file.
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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.nav.Route
import org.librefit.ui.components.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.morphShape.AnimatedMorphShapes
import org.librefit.ui.theme.LibreFitTheme


@Composable
fun WelcomeScreen(
    navController: NavHostController,
    doNotShowWelcomeScreenAgain: () -> Unit
) {

    WelcomeScreenContent(
        navController = navController,
        doNotShowWelcomeScreenAgain = doNotShowWelcomeScreenAgain,
    )

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WelcomeScreenContent(
    navController: NavHostController,
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
                    welcomeScreenContent(navController, doNotShowWelcomeScreenAgain)
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
                    welcomeScreenContent(navController, doNotShowWelcomeScreenAgain)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.welcomeScreenContent(
    navController: NavController,
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
                                    navController.navigate(Route.TutorialScreen(fromWelcomeScreen = true)) {
                                        launchSingleTop = true
                                        popUpTo(Route.WelcomeScreen) { inclusive = true }
                                    }
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
                                    navController.navigate(Route.MainScreen) {
                                        launchSingleTop = true
                                        popUpTo(Route.WelcomeScreen) { inclusive = true }
                                    }
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
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        WelcomeScreenContent(rememberNavController()) {}
    }
}