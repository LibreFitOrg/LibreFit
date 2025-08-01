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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.stacked
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
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.Shape
import org.librefit.R
import org.librefit.enums.chart.ChartMode
import org.librefit.enums.chart.MeasurementChart
import org.librefit.enums.chart.WorkoutChart
import org.librefit.nav.Route
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.theme.LibreFitTheme
import java.text.DecimalFormat
import kotlin.random.Random

/**
 * A custom [com.patrykandpatrick.vico.core.cartesian.CartesianChart]
 *
 * @param format It is used by [VerticalAxis] to display Y axis values following the provided format
 * @param listChartData A list of [ChartData] containing the actual points of the chart.
 * If empty,a placeholder is shown. Leave all [ChartData.xValue]s blank to display default ordinal numeration in x axis.
 * @param useColumns When `false`, the chart will use lines.
 * @param chartMode A [ChartMode] to display which [FilterChip] is selected. If `null`, none filter chips
 * will be displayed.
 * @param updateChartMode It's triggered when any [FilterChip] is clicked. It passes the corresponding
 * [ChartMode] value.
 */
@Composable
fun LibreFitCartesianChart(
    format: DecimalFormat = DecimalFormat(),
    listChartData: List<ChartData>,
    useColumns: Boolean = false,
    chartMode: ChartMode? = null,
    updateChartMode: ((ChartMode) -> Unit)? = null,
    navController: NavHostController? = null
) {
    val labelListKey = ExtraStore.Key<List<String>>()
    val modelProducer = remember { CartesianChartModelProducer() }

    val selectedWorkoutId = rememberSaveable { mutableStateOf<Long?>(null) }

    val selectedWorkoutDate = rememberSaveable { mutableStateOf<String?>(null) }

    val rawYValues = listChartData.map { it.yValues }
    val xValues = listChartData.map { it.xValue }

    val expectedSize = rawYValues.first().size
    require(rawYValues.all { it.size == expectedSize } && expectedSize <= 4) {
        "All yValues lists must have the same size which must be not over 4. Found sizes: ${rawYValues.map { it.size }}"
    }

    /**
     * A transposed list in order to be in a suitable format for modelProducer.
     * Each item represents the points in the same x value.
     * ```
     * val rawYValues = listOf(listOf(1,2),listOf(3,4),listOf(5,6))
     * //...
     * val yValues = val rawYValues = listOf(listOf(1,3,5),listOf(2,4,6))
     * ```
     */
    val yValues = (0 until expectedSize).map { index ->
        rawYValues.map { yList -> yList[index] }
    }


    LaunchedEffect(yValues) {
        if (yValues.all { it.isNotEmpty() }) {
            modelProducer.runTransaction {
                if (useColumns) {
                    columnSeries { yValues.forEach { series(it) } }
                } else {
                    lineSeries { yValues.forEach { series(it) } }
                }
                if (xValues.all { it.isNotBlank() }) {
                    extras { it[labelListKey] = xValues }
                }
            }
        }
    }

    val colorPalette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.onSurface
    )

    val colors = colorPalette.take(yValues.size)

    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (chartMode != null) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(
                        when (chartMode) {
                            is WorkoutChart -> WorkoutChart.entries
                            is MeasurementChart -> MeasurementChart.entries
                        }
                    ) { mode: ChartMode ->
                        FilterChip(
                            selected = chartMode == mode,
                            onClick = { updateChartMode?.invoke(mode) },
                            label = {
                                Text(
                                    stringResource(
                                        when (mode) {
                                            WorkoutChart.DURATION -> R.string.duration
                                            WorkoutChart.VOLUME -> R.string.volume
                                            WorkoutChart.REPS -> R.string.reps
                                            MeasurementChart.BODY_WEIGHT -> R.string.body_weight
                                            MeasurementChart.FAT_MASS -> R.string.fat_mass
                                            MeasurementChart.LEAN_MASS -> R.string.lean_mass
                                        }
                                    )
                                )
                            },
                            leadingIcon = {
                                if (chartMode == mode) {
                                    Icon(
                                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_check),
                                        contentDescription = null
                                    )
                                }
                            },
                        )
                    }
                }
            }

            AnimatedContent(targetState = yValues.isNotEmpty()) { yValuesNotEmpty ->
                if (yValuesNotEmpty) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // Show chart
                        ProvideVicoTheme(rememberM3VicoTheme()) {
                            CartesianChartHost(
                                chart = rememberCartesianChart(
                                    if (useColumns) rememberColumnCartesianLayer(
                                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                            colors.mapIndexed { i, c ->
                                                rememberLineComponent(
                                                    fill = fill(c),
                                                    thickness = 32.dp,
                                                    shape = if (i == colors.lastIndex) CorneredShape.rounded(
                                                        32,
                                                        32
                                                    )
                                                    else Shape.Rectangle
                                                )
                                            }
                                        ),
                                        columnCollectionSpacing = 64.dp,
                                        mergeMode = { ColumnCartesianLayer.MergeMode.stacked() }
                                    ) else rememberLineCartesianLayer(
                                        lineProvider = LineCartesianLayer.LineProvider.series(
                                            colors.map {
                                                LineCartesianLayer.rememberLine(
                                                    fill = LineCartesianLayer.LineFill.single(
                                                        fill(it)
                                                    ),
                                                    areaFill = LineCartesianLayer.AreaFill.single(
                                                        fill(
                                                            ShaderProvider.verticalGradient(
                                                                arrayOf(
                                                                    it.copy(alpha = 0.4f),
                                                                    Color.Transparent
                                                                )
                                                            )
                                                        )
                                                    ),
                                                    // Curved line
                                                    pointConnector = LineCartesianLayer.PointConnector.cubic()
                                                )
                                            }
                                        ),
                                        pointSpacing = 64.dp
                                    ),
                                    marker = rememberLibreFitMarker(
                                        valueFormatter = DefaultCartesianMarker.ValueFormatter.default(
                                            format
                                        )
                                    ),
                                    markerVisibilityListener = object :
                                        CartesianMarkerVisibilityListener {
                                        override fun onShown(
                                            marker: CartesianMarker,
                                            targets: List<CartesianMarker.Target>,
                                        ) {
                                            selectedWorkoutId.value =
                                                listChartData[targets.first().x.toInt()].workoutId

                                            selectedWorkoutDate.value =
                                                listChartData[targets.first().x.toInt()].xValue

                                            super.onShown(marker, listOf(targets.first()))
                                        }

                                        override fun onUpdated(
                                            marker: CartesianMarker,
                                            targets: List<CartesianMarker.Target>,
                                        ) {
                                            selectedWorkoutId.value =
                                                listChartData[targets.first().x.toInt()].workoutId

                                            selectedWorkoutDate.value =
                                                listChartData[targets.first().x.toInt()].xValue

                                            super.onShown(marker, listOf(targets.first()))
                                        }
                                    },
                                    startAxis = VerticalAxis.rememberStart(
                                        valueFormatter = remember(format) {
                                            CartesianValueFormatter.decimal(
                                                format
                                            )
                                        }
                                    ),
                                    bottomAxis = HorizontalAxis.rememberBottom(
                                        valueFormatter = remember(yValues, xValues) {
                                            if (xValues.all { it.isNotBlank() } && xValues.isNotEmpty())
                                                CartesianValueFormatter { context, x, _ ->
                                                    context.model.extraStore.getOrNull(labelListKey)
                                                        ?.get(x.toInt())
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
                                // Shown when modelProducer is loading
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
                        if (navController != null) {
                            LibreFitButton(
                                elevated = false,
                                enabled = selectedWorkoutId.value != null,
                                text = if (selectedWorkoutDate.value == null) stringResource(R.string.tap_a_workout)
                                else stringResource(R.string.open_the_workout) + " ${selectedWorkoutDate.value}",
                                icon = ImageVector.vectorResource(R.drawable.ic_open_new)
                            ) {
                                navController.navigate(Route.InfoWorkoutScreen(selectedWorkoutId.value!!))
                            }
                        }
                    }
                } else {
                    // Inform user that data is insufficient to display the chart
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp, bottom = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(30.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_database_off),
                            modifier = Modifier.size(60.dp),
                            contentDescription = null
                        )
                        Text(
                            text = stringResource(R.string.not_enough_data),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

        }
    }
}

@Preview
@Composable
private fun LibreFitCartesianChartPreview() {
    val chartMode = remember {
        mutableStateOf<ChartMode?>(
            listOf(
                *WorkoutChart.entries.toTypedArray(),
                *MeasurementChart.entries.toTypedArray()
            ).random()
        )
    }

    val numRandomEntries = Random.nextInt(0, 4)

    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        LibreFitCartesianChart(
            listChartData = (0..10).map {
                ChartData(
                    yValues = (0..numRandomEntries).map { Random.nextFloat() },
                    xValue = "${it + 1}°",
                    workoutId = Random.nextLong()
                )
            },
            useColumns = Random.nextBoolean(),
            chartMode = chartMode.value,
            updateChartMode = {
                chartMode.value = it
            },
            navController = if (WorkoutChart.entries.contains(chartMode.value))
                rememberNavController() else null
        )
    }
}