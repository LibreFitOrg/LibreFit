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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
    }

    item {
        SuccessLottie(Modifier.requiredSize(min(maxHeight, maxWidth) / 2))
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

@Preview
@Composable
private fun SuccessScreenPreview() {
    LibreFitTheme(false, true) {
        SuccessScreen(SuccessMessage.WORKOUT_SAVED) { }
    }
}