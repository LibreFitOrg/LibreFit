/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.theme.LibreFitTheme
import kotlin.math.abs

/**
 * @param visibleItemsCount it must be a positive and odd number. Default value is recommended.
 * @throws IllegalArgumentException when [visibleItemsCount] is not odd.
 */
@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    options: ImmutableList<Int>,
    label: (Int) -> String = { it.toString() },
    textStyle: TextStyle = LocalTextStyle.current,
    widthPadding: Dp = 0.dp,
    itemHeight: Dp = 48.dp, // Standard touch target size
    visibleItemsCount: Int = 3,
    enableDividers: Boolean = false,
    dividersColor: Color = MaterialTheme.colorScheme.primary
) {
    // These checks are necessary for this picker to work properly
    require(visibleItemsCount % 2 != 0) {
        "visibleItemsCount must be an odd number. Provided: $visibleItemsCount"
    }
    require(visibleItemsCount > 0) {
        "visibleItemsCount must be greater than 0."
    }

    // Calculate how many items to pad on each side
    val padCount = visibleItemsCount / 2
    val paddedOptions = remember(options) {
        List(padCount) { null } + options + List(padCount) { null }
    }

    val initialIndex = remember { options.indexOf(value).coerceAtLeast(0) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val centeredItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val center = layoutInfo.viewportSize.height / 2f
            layoutInfo.visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2f) - center)
            }?.index ?: 0
        }
    }

    // Calculate the widest item width once when options change
    val textMeasurer = rememberTextMeasurer()
    val widestItemWidth = remember(options, textStyle) {
        options.maxOfOrNull { option ->
            textMeasurer.measure(label(option), textStyle).size.width
        } ?: 0
    }

    // Convert pixels to Dp
    val density = LocalDensity.current
    val minWidthDp = with(density) { widestItemWidth.toDp() }


    // Haptic feedback when crossing an item
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        snapshotFlow { centeredItemIndex }
            .distinctUntilChanged()
            .collect {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
    }

    // Get the latest value and callback inside a long-running LaunchedEffect without restarting it
    val latestOnValueChange by rememberUpdatedState(onValueChange)

    // Update parent when scrolling stops
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it } // Only proceed when scroll has stopped
            .collect {
                val realIndex = centeredItemIndex - padCount
                options.getOrNull(realIndex)?.let { newItem ->
                    if (newItem != value) latestOnValueChange(newItem)
                }
            }
    }

    // Sync with external value changes
    LaunchedEffect(value) {
        if (!listState.isScrollInProgress) {
            options.indexOf(value).takeIf { it != -1 }?.let { targetIndex ->
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    val itemHeightPx = with(density) { itemHeight.toPx() }

    Box(contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(minWidthDp + widthPadding)
                .height(itemHeight * visibleItemsCount),
        ) {
            itemsIndexed(
                items = paddedOptions,
                // Using index + value as key to avoid duplicates
                key = { index, item -> "$index-$item" }
            ) { index, item ->
                // derivedStateOf only observes the state objects read within it.
                // Pass itemHeightPx as a plain float, which is safe.
                val progress by remember(itemHeightPx) {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }

                        if (itemInfo == null) {
                            0f
                        } else {
                            val centerOffset = layoutInfo.viewportSize.height / 2f
                            val itemCenter = itemInfo.offset + itemInfo.size / 2f
                            ((itemCenter - centerOffset) / itemHeightPx).coerceIn(-1.5f, 1.5f)
                        }
                    }
                }

                NumberPickerItem(
                    item = item,
                    progress = progress,
                    itemHeight = itemHeight,
                    textStyle = textStyle,
                    label = label
                )

            }
        }
        // Dividers overlay
        if (enableDividers) {
            Column(
                modifier = Modifier
                    .width(minWidthDp + widthPadding)
                    .height(itemHeight),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                HorizontalDivider(color = dividersColor)
                HorizontalDivider(color = dividersColor)
            }
        }
    }


}

@Composable
private fun NumberPickerItem(
    item: Int?,
    progress: Float,
    itemHeight: Dp,
    textStyle: TextStyle,
    label: (Int) -> String = { it.toString() }
) {
    // Calculate the transformation based on the item's position relative to the center
    val modifier = Modifier
        .height(itemHeight)
        .graphicsLayer {
            rotationX = -progress * 35f
            alpha = 0.3f + (0.7f * (1f - abs(progress) / 1.5f))
            scaleX = 0.7f + (0.3f * (1f - abs(progress) / 1.5f))
            scaleY = 0.7f + (0.3f * (1f - abs(progress) / 1.5f))
        }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        item?.let {
            Text(text = label(item), style = textStyle, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
fun NumberPickerPreview() {
    var selectedValue by remember { mutableIntStateOf(100) }
    LibreFitTheme(false, ThemeMode.DARK) {
        Surface {
            NumberPicker(
                value = selectedValue,
                onValueChange = { selectedValue = it },
                label = { "$it" },
                textStyle = MaterialTheme.typography.titleLargeEmphasized,
                options = (0..100).toImmutableList(),
                enableDividers = true
            )
        }
    }
}