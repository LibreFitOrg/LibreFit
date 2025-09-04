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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import kotlinx.coroutines.delay
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.morphShape.CustomRotatingMorphShape
import org.librefit.ui.theme.LibreFitTheme
import kotlin.random.Random

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryScreen(
    innerPadding: PaddingValues
) {
    //TODO: implement archived routines

    //TODO: implement a default routine

    var shapeA by remember {
        mutableStateOf(
            RoundedPolygon(
                numVertices = 6,
                rounding = CornerRounding(0.1f)
            )
        )
    }
    // Shape B can remain static for this example, but could also be stateful
    var shapeB by remember {
        mutableStateOf(
            RoundedPolygon.star(
                numVerticesPerRadius = 6,
                rounding = CornerRounding(1f)
            )
        )
    }
    val infiniteTransition = rememberInfiniteTransition("infinite outline movement")
    val morph = remember(shapeA, shapeB) {
        Morph(shapeA, shapeB)
    }
    val animatedProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress"
    )
    val animatedRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animatedMorphProgress"
    )

    val colors = mapOf(
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer,
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer,
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary,
        MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary,
        MaterialTheme.colorScheme.primaryFixed to MaterialTheme.colorScheme.onPrimaryFixed,
        MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurface,
    )

    var currentColor by remember {
        mutableStateOf(colors.keys.random())
    }


    val animatedColor by animateColorAsState(
        targetValue = currentColor,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        label = "ColorAnimation"
    )
    // Change shape and color
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2, 6) * 1000)

            shapeA = RoundedPolygon(
                numVertices = Random.nextInt(3, 8),
                rounding = CornerRounding(0.2f)
            )

            shapeB = RoundedPolygon.star(
                numVerticesPerRadius = Random.nextInt(3, 8),
                rounding = CornerRounding(1f)
            )

            delay(Random.nextLong(2, 6) * 1000)

            currentColor = colors.keys.random()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(
                    CustomRotatingMorphShape(
                        morph,
                        animatedProgress.value,
                        animatedRotation.value
                    )
                )
                .size(400.dp)
                .background(animatedColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Coming soon",
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = colors.getValue(currentColor)
            )
        }
    }

}

@Preview
@Composable
private fun LibraryScreenPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = false) {
        LibreFitScaffold { innerPadding ->
            LibraryScreen(innerPadding)
        }
    }
}