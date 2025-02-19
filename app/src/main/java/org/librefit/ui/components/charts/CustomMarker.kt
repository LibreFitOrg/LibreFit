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

package org.librefit.ui.components.charts

import android.text.Layout
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shadow
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.shape.markerCorneredShape
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape

/**
 * A custom [CartesianMarker] to display information while tapping a chart
 */
@Composable
fun rememberMarker(
    valueFormatter: DefaultCartesianMarker.ValueFormatter =
        DefaultCartesianMarker.ValueFormatter.default(),
    showIndicator: Boolean = true,
): CartesianMarker {
    val labelBackgroundShape = markerCorneredShape(CorneredShape.Corner.Rounded)
    val labelBackground =
        rememberShapeComponent(
            fill = fill(MaterialTheme.colorScheme.surfaceContainer),
            shape = labelBackgroundShape,
            shadow =
            shadow(
                radius = LABEL_BACKGROUND_SHADOW_RADIUS_DP.dp,
                y = LABEL_BACKGROUND_SHADOW_DY_DP.dp
            ),
        )
    val label =
        rememberTextComponent(
            color = MaterialTheme.colorScheme.onSurface,
            textAlignment = Layout.Alignment.ALIGN_CENTER,
            padding = insets(8.dp, 4.dp),
            background = labelBackground,
            minWidth = TextComponent.MinWidth.fixed(40f),
        )
    val indicatorFrontComponent =
        rememberShapeComponent(fill(MaterialTheme.colorScheme.surface), CorneredShape.Pill)
    val indicatorCenterComponent = rememberShapeComponent(shape = CorneredShape.Pill)
    val indicatorRearComponent = rememberShapeComponent(shape = CorneredShape.Pill)
    val indicator =
        LayeredComponent(
            back = indicatorRearComponent,
            front =
            LayeredComponent(
                back = indicatorCenterComponent,
                front = indicatorFrontComponent,
                padding = insets(5.dp),
            ),
            padding = insets(10.dp),
        )
    val guideline = rememberAxisGuidelineComponent()
    val color = MaterialTheme.colorScheme.inversePrimary.toArgb()
    return remember(label, valueFormatter, indicator, showIndicator, guideline) {
        object :
            DefaultCartesianMarker(
                label = label,
                valueFormatter = valueFormatter,
                indicator =
                if (showIndicator) {
                    {
                        LayeredComponent(
                            back =
                            ShapeComponent(
                                Fill(ColorUtils.setAlphaComponent(color, 38)),
                                CorneredShape.Pill
                            ),
                            front =
                            LayeredComponent(
                                back =
                                ShapeComponent(
                                    fill = Fill(color),
                                    shape = CorneredShape.Pill,
                                    shadow = Shadow(radiusDp = 12f, color = color),
                                ),
                                front = indicatorFrontComponent,
                                padding = insets(5.dp),
                            ),
                            padding = insets(10.dp),
                        )
                    }
                } else {
                    null
                },
                indicatorSizeDp = 36f,
                guideline = guideline,
            ) {
            override fun updateLayerMargins(
                context: CartesianMeasuringContext,
                layerMargins: CartesianLayerMargins,
                layerDimensions: CartesianLayerDimensions,
                model: CartesianChartModel,
            ) {
                with(context) {
                    val baseShadowMarginDp =
                        CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER * LABEL_BACKGROUND_SHADOW_RADIUS_DP
                    var topMargin = (baseShadowMarginDp - LABEL_BACKGROUND_SHADOW_DY_DP).pixels
                    var bottomMargin = (baseShadowMarginDp + LABEL_BACKGROUND_SHADOW_DY_DP).pixels
                    when (labelPosition) {
                        LabelPosition.Top,
                        LabelPosition.AbovePoint -> topMargin += label.getHeight(context) + tickSizeDp.pixels

                        LabelPosition.Bottom -> bottomMargin += label.getHeight(context) + tickSizeDp.pixels
                        LabelPosition.AroundPoint -> {}
                    }
                    layerMargins.ensureValuesAtLeast(top = topMargin, bottom = bottomMargin)
                }
            }
        }
    }
}

private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
private const val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
private const val CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER = 1.4f