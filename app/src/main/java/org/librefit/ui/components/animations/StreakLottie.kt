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

package org.librefit.ui.components.animations

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import org.librefit.R

@Composable
fun StreakLottie(streak: Int) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    val inversePrimaryColor = MaterialTheme.colorScheme.inversePrimary.toArgb()

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.streak_lottie))

    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = streak != 0,
        speed = (streak / 26f).coerceIn(0.05f, 2f),
        iterations = LottieConstants.IterateForever
    )

    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.GRADIENT_COLOR,
            value = arrayOf(primaryColor, primaryColor, inversePrimaryColor),
            keyPath = arrayOf("Shape Layer 1", "Shape 1", "Gradient Fill 1")
        )
    )
    LottieAnimation(
        modifier = Modifier.fillMaxWidth(),
        composition = composition,
        progress = { progress },
        dynamicProperties = dynamicProperties
    )
}