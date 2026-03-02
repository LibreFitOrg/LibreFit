/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components.charts

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LayeredComponent
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent

/**
 * A custom [CartesianMarker] to display information while tapping a chart
 */
@Composable
fun rememberLibreFitMarker(
    valueFormatter: DefaultCartesianMarker.ValueFormatter =
        DefaultCartesianMarker.ValueFormatter.default(),
    showIndicator: Boolean = true,
    style: TextStyle
): CartesianMarker {
    val labelBackground =
        rememberShapeComponent(
            fill = Fill(MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(percent = 50),
            strokeThickness = 1.dp,
            strokeFill = Fill(MaterialTheme.colorScheme.outline),
        )
    val label =
        rememberTextComponent(
            style = style,
            padding = Insets(8.dp, 4.dp),
            background = labelBackground,
        )
    val indicatorFrontComponent =
        rememberShapeComponent(Fill(MaterialTheme.colorScheme.surfaceContainerHigh), RoundedCornerShape(50))
    val guideline = rememberAxisGuidelineComponent()

    val strokeColor = MaterialTheme.colorScheme.surface

    return rememberDefaultCartesianMarker(
        label = label,
        valueFormatter = valueFormatter,
        indicator =
            if (showIndicator) {
                { color ->
                    LayeredComponent(
                        back = ShapeComponent(
                            fill = Fill(color.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(50)
                        ),
                        front =
                            LayeredComponent(
                                back = ShapeComponent(
                                    fill = Fill(color),
                                    shape = RoundedCornerShape(50),
                                    strokeFill = Fill(strokeColor),
                                    strokeThickness = 3.dp
                                ),
                                front = indicatorFrontComponent,
                                padding = Insets(5.dp),
                            ),
                        padding = Insets(6.dp),
                    )
                }
            } else {
                null
            },
        indicatorSize = 26.dp,
        guideline = guideline,
    )
}