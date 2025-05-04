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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import org.librefit.R
import org.librefit.data.ChartData
import org.librefit.ui.theme.LibreFitTheme
import java.text.DecimalFormat
import kotlin.random.Random

/**
 * A custom [com.patrykandpatrick.vico.core.cartesian.CartesianChart]
 *
 * @param format It is used by [VerticalAxis] to display Y axis values following the provided format
 * @param listChartData A list of [org.librefit.data.ChartData] containing the actual points of the chart.
 * If empty,a placeholder is shown.
 * Leave all [ChartData.xValue]s blank to display default ordinal numeration in  axis.
 *
 * @param columns When `false`, the chart becomes a line chart.
 *
 */
@Composable
fun CustomCartesianChart(
    format: DecimalFormat = DecimalFormat(),
    listChartData: List<ChartData>,
    columns: Boolean = false
) {
    val labelListKey = ExtraStore.Key<List<String>>()
    val modelProducer = remember { CartesianChartModelProducer() }

    val yValues = listChartData.map { it.yValue }
    val xValues = listChartData.map { it.xValue }

    val primaryColor = MaterialTheme.colorScheme.primary

    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 215.dp)
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (yValues.isNotEmpty()) {
                LaunchedEffect(yValues) {
                    modelProducer.runTransaction {
                        if (columns) {
                            columnSeries { series(yValues) }
                        } else {
                            lineSeries { series(yValues) }
                        }
                        if (xValues.all { it.isNotBlank() }) {
                            extras { it[labelListKey] = xValues }
                        }
                    }
                }

                ProvideVicoTheme(rememberM3VicoTheme()) {
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            if (columns) rememberColumnCartesianLayer(
                                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                    rememberLineComponent(
                                        fill = fill(MaterialTheme.colorScheme.primary),
                                        thickness = 32.dp,
                                        shape = CorneredShape.rounded(32, 32)
                                    )
                                ),
                                columnCollectionSpacing = 64.dp
                            ) else rememberLineCartesianLayer(
                                lineProvider = LineCartesianLayer.LineProvider.series(
                                    LineCartesianLayer.rememberLine(
                                        fill = LineCartesianLayer.LineFill.single(fill(primaryColor)),
                                        areaFill = LineCartesianLayer.AreaFill.single(
                                            fill(
                                                ShaderProvider.verticalGradient(
                                                    arrayOf(
                                                        primaryColor.copy(alpha = 0.4f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                        ),
                                        // Curved line
                                        pointConnector = LineCartesianLayer.PointConnector.cubic()
                                    )
                                ),
                                pointSpacing = 64.dp
                            ),
                            marker = rememberMarker(
                                valueFormatter = DefaultCartesianMarker.ValueFormatter.default(
                                    format
                                )
                            ),
                            startAxis = VerticalAxis.rememberStart(
                                valueFormatter = remember(format) {
                                    CartesianValueFormatter.decimal(
                                        format
                                    )
                                }
                            ),
                            bottomAxis = HorizontalAxis.rememberBottom(
                                valueFormatter = remember(yValues, xValues) {
                                    if (xValues.all { it.isNotBlank() })
                                        CartesianValueFormatter { context, x, _ ->
                                            context.model.extraStore.getOrNull(labelListKey)
                                                ?.get(x.toInt())
                                                ?: xValues.getOrNull(yValues.indexOf(x.toFloat()))
                                                ?: xValues.first()
                                        }
                                    else CartesianValueFormatter.decimal()
                                }
                            ),
                        ),
                        zoomState = rememberVicoZoomState(
                            zoomEnabled = false,
                            minZoom = Zoom.fixed(),
                            maxZoom = Zoom.fixed()
                        ),
                        modelProducer = modelProducer,
                    ) {
                        // Shown when loading modelProducer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                }
            } else {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_database_off),
                    modifier = Modifier.size(60.dp),
                    contentDescription = null
                )
                Spacer(Modifier.height(40.dp))
                Text(
                    text = stringResource(R.string.not_enough_data),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
private fun CustomCartesianChartPreview() {
    LibreFitTheme(false, true) {
        CustomCartesianChart(
            listChartData = (0..10).map { ChartData(Random.nextFloat()) },
            columns = false
        )
    }
}