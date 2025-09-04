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

package org.librefit.ui.screens.shared

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.min
import org.librefit.R
import org.librefit.enums.SuccessMessage
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.SuccessLottie
import org.librefit.ui.theme.LibreFitTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SuccessScreen(
    message: SuccessMessage,
    navigateBack: () -> Unit
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
                    successScreenContent(message, navigateBack, maxHeight, maxWidth)
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
                    successScreenContent(message, navigateBack, maxHeight, maxWidth)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.successScreenContent(
    message: SuccessMessage,
    navigateBack: () -> Unit,
    maxHeight: Dp,
    maxWidth: Dp
) {
    item {
        Text(
            text = when (message) {
                SuccessMessage.ROUTINE_SAVED -> stringResource(R.string.routine_saved)
                SuccessMessage.WORKOUT_SAVED -> stringResource(R.string.workout_saved)
            },
            style = MaterialTheme.typography.displaySmallEmphasized,
            textAlign = TextAlign.Center
        )
    }

    item {
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

    // TODO: add donation notice when workout is saved

    item {
        Button(
            onClick = navigateBack
        ) {
            Text(stringResource(R.string.label_continue))
        }
    }
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
private fun SuccessScreenPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SuccessScreen(SuccessMessage.WORKOUT_SAVED) { }
    }
}