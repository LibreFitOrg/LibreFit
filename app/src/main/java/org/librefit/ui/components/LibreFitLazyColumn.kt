/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
 * @param innerPadding Padding values to be applied around the content. It usually comes from [LibreFitScaffold]
 * @param verticalSpacing The spacing applied between the items in [LazyColumn]
 * @param startEndPadding The padding applied in the start and in the end of [LazyColumn]
 * @param lazyListState A [LazyListState] to manage the list scroll
 * @param bottomSpacer If `true`, this lazy column will have a [Spacer] of `100.dp` at the bottom.
 * @param modifier The modifier applied to the inner [LazyColumn], e.g. to attach a
 * [androidx.compose.ui.input.nestedscroll.NestedScrollConnection] to it.
 * @param content A lambda with receiver of type [LazyListScope] used to populate the lazy list.
 */
@Composable
fun LibreFitLazyColumn(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(),
    verticalSpacing: Dp = 15.dp,
    startEndPadding: Dp = 15.dp,
    lazyListState: LazyListState = rememberLazyListState(),
    bottomSpacer: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val threshold = 600
        //Apply padding only when width is greater than 600.dp (so when screen orientation is landscape)
        val optionalPadding = if (maxWidth < threshold.dp) 0f else (maxWidth.value - threshold) / 2
        LazyColumn(
            modifier = modifier.consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr) + optionalPadding.dp
                        + startEndPadding,
                end = innerPadding.calculateRightPadding(LayoutDirection.Ltr) + optionalPadding.dp
                        + startEndPadding,
                bottom = innerPadding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = lazyListState
        ) {
            content()
            if (bottomSpacer) {
                item {
                    /**
                     * Is provides a standard blank space in the bottom of all the necessary lazy columns
                     */
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}