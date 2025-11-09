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
 *
 * LibreFit is subject to additional terms covering author attribution and
 * trademark usage, as found in the accompanying ADDITIONAL_TERMS.md file.
 */

package org.librefit.ui.screens.shared

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import androidx.compose.ui.unit.min
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.enums.SuccessMessage
import org.librefit.nav.Route
import org.librefit.ui.components.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.SuccessLottie
import org.librefit.ui.theme.LibreFitTheme

@Composable
fun SuccessScreen(
    message: SuccessMessage,
    navController: NavHostController
) {
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
                    successScreenContent(
                        message = message,
                        navigateBack = navController::navigateUp,
                        navigateToSupportScreen = {
                            navController.navigate(Route.SupportScreen()) {
                                launchSingleTop = true
                                popUpTo(Route.MainScreen)
                            }
                        },
                        maxHeight = maxHeight,
                        maxWidth = maxWidth
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
                    successScreenContent(
                        message = message,
                        navigateBack = navController::navigateUp,
                        navigateToSupportScreen = {
                            navController.navigate(Route.SupportScreen()) {
                                launchSingleTop = true
                                popUpTo(Route.MainScreen)
                            }
                        },
                        maxHeight = maxHeight,
                        maxWidth = maxWidth
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.successScreenContent(
    message: SuccessMessage,
    navigateBack: () -> Unit,
    navigateToSupportScreen: () -> Unit,
    maxHeight: Dp,
    maxWidth: Dp
) {
    item {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.padding(bottom = 20.dp),
                text = when (message) {
                    SuccessMessage.ROUTINE_SAVED -> stringResource(R.string.routine_saved)
                    SuccessMessage.WORKOUT_SAVED -> stringResource(R.string.workout_saved)
                },
                style = MaterialTheme.typography.displaySmallEmphasized,
                textAlign = TextAlign.Center
            )
            val size = remember(maxHeight, maxWidth) { min(maxHeight, maxWidth) / 2 }
            val infiniteTransition = rememberInfiniteTransition()
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(4000))
            )
            Box(contentAlignment = Alignment.Center) {
                ElevatedCard(
                    modifier = Modifier
                        .rotate(rotation)
                        .size(size.times(1.2f)),
                    shape = MaterialShapes.Cookie7Sided.toShape()
                ) { }
                SuccessLottie(Modifier.size(size))
            }
        }
    }

    item {
        ElevatedCard(
            modifier = Modifier.padding(20.dp),
            shape = MaterialTheme.shapes.extraLargeIncreased
        ) {
            Column(
                modifier = Modifier.padding(25.dp),
                verticalArrangement = Arrangement.spacedBy(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = buildAnnotatedString {
                        GetAppNameInAnnotatedBuilder(MaterialTheme.typography.titleLargeEmphasized)
                        append(" ")
                        append(stringResource(R.string.librefit_made_by_you))
                    },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
                BoxWithConstraints {
                    val spaceMaxWidth = with(LocalDensity.current) {
                        (this@BoxWithConstraints.maxWidth.takeIf { it.isFinite } ?: maxWidth).toPx()
                    }

                    val infiniteTransition = rememberInfiniteTransition()
                    val animationProgress = infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = spaceMaxWidth * 5,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000),
                            repeatMode = RepeatMode.Restart
                        )
                    )

                    val a = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.inversePrimary,
                            MaterialTheme.colorScheme.primary
                        ),
                        radius = animationProgress.value
                    )

                    Button(
                        onClick = navigateToSupportScreen,
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = ButtonDefaults.MediumContentPadding,
                        border = BorderStroke(
                            width = 3.dp,
                            brush = a
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_favorite),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text(
                                text = stringResource(R.string.lets_build_it_together),
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                LibreFitButton(
                    onClick = navigateBack,
                    text = stringResource(R.string.home),
                    icon = painterResource(R.drawable.ic_home),
                    elevated = false
                )
            }
        }
    }
}

@Preview
@Composable
private fun SuccessScreenPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SuccessScreen(SuccessMessage.WORKOUT_SAVED, rememberNavController())
    }
}