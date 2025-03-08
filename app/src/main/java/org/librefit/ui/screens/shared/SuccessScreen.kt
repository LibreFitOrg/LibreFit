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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.librefit.R
import org.librefit.enums.SuccessMessage
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.animations.SuccessLottie

@Composable
fun SuccessScreen(
    message: SuccessMessage,
    navigateBack: () -> Unit
) {
    CustomScaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (message) {
                    SuccessMessage.ROUTINE_SAVED -> stringResource(R.string.routine_saved)
                    SuccessMessage.WORKOUT_SAVED -> stringResource(R.string.workout_saved)
                },
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            SuccessLottie()
            // TODO: add donation notice when workout is saved
            Button(
                onClick = navigateBack
            ) {
                Text(stringResource(R.string.label_continue))
            }
        }
    }
}

@Preview
@Composable
private fun SuccessScreenPreview() {
    SuccessScreen(SuccessMessage.WORKOUT_SAVED) { }
}