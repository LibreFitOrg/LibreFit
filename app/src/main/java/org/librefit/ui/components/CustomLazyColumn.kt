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

package org.librefit.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * A custom composable that displays a [LazyColumn] within a full size [BoxWithConstraints]
 * container, applying dynamic padding based on the available width.
 *
 * This composable creates a lazy column that applies horizontal padding if the available `maxWidth`
 * is greater than `600.dp`. For widths above `600.dp`, the extra space is divided evenly on the
 * left and right sides.
 *
 * The lambda [content] supplies the content of the list using the [LazyListScope].
 *
 * @param innerPadding Padding values to be applied around the content. It usually comes from [CustomScaffold]
 * @param content A lambda with receiver of type [LazyListScope] used to populate the lazy list.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CustomLazyColumn(
    innerPadding: PaddingValues = PaddingValues(),
    verticalPadding: Dp = 10.dp,
    content: LazyListScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val threshold = 600
        //Apply padding only when width is greater than 600.dp (i.e. when screen is in landscape mode)
        val optionalPadding = if (maxWidth < threshold.dp) 0f else (maxWidth.value - threshold) / 2
        LazyColumn(
            modifier = Modifier.padding(start = 15.dp, end = 15.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr) + optionalPadding.dp,
                end = innerPadding.calculateRightPadding(LayoutDirection.Ltr) + optionalPadding.dp,
                bottom = innerPadding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}