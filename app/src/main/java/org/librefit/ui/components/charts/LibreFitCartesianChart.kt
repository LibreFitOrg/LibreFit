/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.Legend
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import org.librefit.R
import org.librefit.enums.chart.BodyweightChart
import org.librefit.enums.chart.ChartMode
import org.librefit.enums.chart.LoadChart
import org.librefit.enums.chart.MeasurementChart
import org.librefit.enums.chart.StatisticsChart
import org.librefit.enums.chart.TimeChart
import org.librefit.enums.chart.WeightedBodyweightChart
import org.librefit.enums.chart.WorkoutChart
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import java.time.LocalDateTime
import kotlin.random.Random


val labelListKey = ExtraStore.Key<List<String>>()
val legendLabelKey = ExtraStore.Key<List<String>>()

/**
 * A custom [com.patrykandpatrick.vico.compose.cartesian.CartesianChart]
 *
 * @param decimalCount The number of decimal digits in the y-axis. Default is 2 decimal digits.
 * @param suffix to be applied at all values in y-axis. Keep in mind a blank space is added before suffix.
 * @param points A list of [Point]s containing the actual points of the chart. The sizes of [Point.yValues]
 * list must be not over 4. If [points] is empty, a placeholder is shown. Leave all [Point.xValue]s blank in order to display default ordinal numeration in x axis.
 * @param useColumns When `true`, the chart will use columns instead of lines.
 * @param chartMode A [ChartMode] to display which [FilterChip] is selected. If null, no chip is selected.
 * @param chartModes The list of [FilterChip]s displayed at the top of the chart. If empty, no chip will be displayed.
 * @param updateChartMode It's triggered when any [FilterChip] is clicked. It passes the corresponding
 * [ChartMode] value.
 * and to show info about a selected [org.librefit.db.entity.Workout]. It is intended to work only with workouts
 * rather than [org.librefit.db.entity.Measurement] or anything else.
 * @param legendList A list of [String] shown under the chart as a legend. The list must have a less
 * or equal size of [Point.yValues] and it must not contain blank strings. Leave empty to not shown a legend.
 * @param onEntrySelection When not null, a button is shown in order to (or at least intended to) navigate to open/navigate the associated entry with the given `Long` id.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T : ChartMode> LibreFitCartesianChart(
    decimalCount: Int = 2,
    suffix : String? = null,
    points: List<Point>,
    useColumns: Boolean = false,
    chartMode: T? = null,
    chartModes: List<T> = emptyList(),
    legendList: List<String>? = null,
    updateChartMode: (T) -> Unit = {},
    onEntrySelection: ((Long) -> Unit)? = null
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val selectedWorkoutId = rememberSaveable { mutableStateOf<Long?>(null) }

    val selectedWorkoutDate = rememberSaveable { mutableStateOf<String?>(null) }

    val rawYValues = points.map { it.yValues }
    val xValues = points.map { it.xValue }

    val expectedSize = rawYValues.firstOrNull()?.size ?: 0
    require(rawYValues.all { it.size == expectedSize } && expectedSize <= 4) {
        "All yValues lists must have the same size which must be not over 4. Found sizes: ${rawYValues.map { it.size }}"
    }

    if (legendList != null) {
        require(legendList.size <= expectedSize && legendList.all { it.isNotBlank() }) {
            "The legend list must be less than the size of yValues and it must not contain blank strings. " +
                    "Legend list size: ${legendList.size}. Expected size: $expectedSize."
        }
    }

    /**
     * A transposed list in order to be in a suitable format for modelProducer.
     * Each item represents a point in the same x value.
     * ```
     * val rawYValues = listOf(listOf(1,2),listOf(3,4),listOf(5,6))
     * //...
     * val yValues = listOf(listOf(1,3,5),listOf(2,4,6))
     * ```
     */
    val yValues = (0 until expectedSize).map { index ->
        rawYValues.map { yList -> yList[index] }
    }

    val yValuesArePresent = yValues.isNotEmpty() && yValues.all { it.isNotEmpty() }



    LaunchedEffect(yValues) {
        if (yValuesArePresent) {
            modelProducer.runTransaction {
                if (useColumns) {
                    columnSeries { yValues.forEach { series(it) } }
                } else {
                    lineSeries { yValues.forEach { series(it) } }
                }
                if (xValues.all { it.isNotBlank() }) {
                    extras { it[labelListKey] = xValues }
                }
                if (legendList != null) {
                    extras { it[legendLabelKey] = legendList }
                }
            }
        }
    }

    val colorPalette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.onSurfaceVariant
    )

    val materialStyle = MaterialTheme.typography.labelLarge

    val labelComponent = rememberTextComponent(
        style =materialStyle.copy(
            color = MaterialTheme.colorScheme.onSurface
        )
    )

    ElevatedCard(
        shape = MaterialTheme.shapes.extraLargeIncreased
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (chartModes.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    itemsIndexed(items = chartModes) { index, mode ->
                        OutlinedToggleButton(
                            checked = chartMode == mode,
                            onCheckedChange = { updateChartMode(mode) },
                            modifier = Modifier.semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                chartModes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        ) {
                            Text(
                                text = stringResource(
                                    when (mode) {
                                        WorkoutChart.DURATION -> R.string.duration
                                        WorkoutChart.VOLUME -> R.string.volume
                                        WorkoutChart.REPS -> R.string.reps
                                        MeasurementChart.BODY_WEIGHT -> R.string.body_weight
                                        MeasurementChart.FAT_MASS -> R.string.fat_mass
                                        MeasurementChart.LEAN_MASS -> R.string.lean_mass
                                        StatisticsChart.LOAD -> R.string.load
                                        StatisticsChart.REPS -> R.string.reps
                                        StatisticsChart.VOLUME -> R.string.volume
                                        StatisticsChart.DURATION -> R.string.duration
                                        TimeChart.BEST_TIME -> R.string.best_time
                                        TimeChart.TOTAL_TIME -> R.string.total_time
                                        WeightedBodyweightChart.HEAVIEST_WEIGHT -> R.string.heaviest_weight
                                        WeightedBodyweightChart.BEST_SET_VOLUME -> R.string.best_set_volume
                                        WeightedBodyweightChart.TOTAL_VOLUME -> R.string.total_volume
                                        WeightedBodyweightChart.TOTAL_REPS -> R.string.total_reps
                                        BodyweightChart.SESSION_REPS -> R.string.session_reps
                                        BodyweightChart.MOST_REPS -> R.string.most_reps
                                        LoadChart.HEAVIEST_WEIGHT -> R.string.heaviest_weight
                                        LoadChart.BEST_SET_VOLUME -> R.string.best_set_volume
                                        LoadChart.TOTAL_REPS -> R.string.total_reps
                                        LoadChart.SESSION_VOLUME -> R.string.session_volume
                                        LoadChart.ONE_REP_MAX -> R.string.one_rep_max
                                    }
                                )
                            )
                        }
                    }
                }
            }

            // Columns' style
            val columnComponents = colorPalette.map { c ->
                rememberLineComponent(
                    fill = Fill(c),
                    thickness = 32.dp,
                    shape = RoundedCornerShape(
                        32,
                        32
                    )
                )
            }

            // Lines' style
            val lineComponents = colorPalette.map {
                LineCartesianLayer.rememberLine(
                    fill = LineCartesianLayer.LineFill.single(
                        Fill(it)
                    ),
                    areaFill = LineCartesianLayer.AreaFill.single(
                        Fill(
                            Brush.verticalGradient(
                                listOf(
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

            // Legend style
            val legend: Legend<CartesianMeasuringContext, CartesianDrawingContext>? =
                if (legendList == null) null else rememberHorizontalLegend(
                    items = { extraStore ->
                        extraStore[legendLabelKey].forEachIndexed { index, label ->
                            add(
                                LegendItem(
                                    ShapeComponent(
                                        Fill(colorPalette[index]),
                                        RoundedCornerShape(percent = 50)
                                    ),
                                    labelComponent,
                                    label,
                                )
                            )
                        }
                    },
                    padding = Insets(top = 16.dp),
                )

            AnimatedContent(targetState = yValuesArePresent) { it ->
                if (it) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // Show chart
                        ProvideVicoTheme(rememberM3VicoTheme()) {
                            CartesianChartHost(
                                chart = rememberCartesianChart(
                                    if (useColumns) rememberColumnCartesianLayer(
                                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                            columnComponents
                                        ),
                                        columnCollectionSpacing = if (chartMode is StatisticsChart)
                                            32.dp else 64.dp,
                                    ) else rememberLineCartesianLayer(
                                        lineProvider = LineCartesianLayer.LineProvider.series(
                                            lineComponents
                                        ),
                                        pointSpacing = 80.dp
                                    ),
                                    marker = rememberLibreFitMarker(
                                        decimalCount = decimalCount,
                                        suffix = suffix?.let { " $it"} ?: "",
                                        style = materialStyle
                                    ),
                                    markerVisibilityListener = object :
                                        CartesianMarkerVisibilityListener {
                                        override fun onShown(
                                            marker: CartesianMarker,
                                            targets: List<CartesianMarker.Target>,
                                        ) {
                                            selectedWorkoutId.value =
                                                points[targets.first().x.toInt()].workoutId

                                            selectedWorkoutDate.value =
                                                points[targets.first().x.toInt()].xValue

                                            super.onShown(marker, listOf(targets.first()))
                                        }

                                        override fun onUpdated(
                                            marker: CartesianMarker,
                                            targets: List<CartesianMarker.Target>,
                                        ) {
                                            selectedWorkoutId.value =
                                                points[targets.first().x.toInt()].workoutId

                                            selectedWorkoutDate.value =
                                                points[targets.first().x.toInt()].xValue

                                            super.onShown(marker, listOf(targets.first()))
                                        }
                                    },
                                    startAxis = VerticalAxis.rememberStart(
                                        label = labelComponent,
                                        valueFormatter = remember(decimalCount, suffix){
                                            CartesianValueFormatter.decimal(
                                                decimalCount = decimalCount,
                                                suffix = suffix?.let { " $it" } ?: ""
                                            )
                                        }
                                    ),
                                    bottomAxis = HorizontalAxis.rememberBottom(
                                        label = labelComponent,
                                        valueFormatter = remember(xValues) {
                                            if (xValues.all { it.isNotBlank() } && xValues.isNotEmpty())
                                                CartesianValueFormatter { context, x, _ ->
                                                    context.model.extraStore.getOrNull(labelListKey)
                                                        ?.get(x.toInt())
                                                        ?: xValues.first()
                                                }
                                            else CartesianValueFormatter.decimal()
                                        }
                                    ),
                                    legend = legend,
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
                                    ContainedLoadingIndicator()
                                }
                            }
                        }
                        if (onEntrySelection != null) {
                            HorizontalDivider()
                            LibreFitButton(
                                elevated = false,
                                enabled = selectedWorkoutId.value != null,
                                text = if (selectedWorkoutId.value == null) stringResource(R.string.tap_a_workout)
                                else stringResource(R.string.open_the_workout) + " ${selectedWorkoutDate.value}",
                                icon = painterResource(R.drawable.ic_open_new)
                            ) {
                                selectedWorkoutId.value?.let {
                                    onEntrySelection(it)
                                }
                            }
                        }
                    }
                } else {
                    // Inform user that data is insufficient to display the chart
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_database_off),
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

    val legendList =
        listOf("First item", "Second item", "Third item", "Fourth item").take(numRandomEntries + 1)

    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        LibreFitCartesianChart(
            decimalCount = 2,
            suffix = " %",
            points = (0..10).map {
                Point(
                    yValues = (0..numRandomEntries).map { Random.nextDouble() },
                    xValue = Formatter.getShortDateFromLocalDate(
                        LocalDateTime.now().minusDays(it.toLong())
                    ),
                    workoutId = Random.nextLong()
                )
            },
            useColumns = Random.nextBoolean(),
            chartMode = if (Random.nextBoolean()) chartMode.value else null,
            onEntrySelection = if (WorkoutChart.entries.contains(chartMode.value)) {{}} else null,
            legendList = legendList,
            updateChartMode = { chartMode.value = it }
        )
    }
}