/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components.modalBottomSheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import org.librefit.R
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.enums.userPreferences.UnitSystem
import org.librefit.models.Weight
import org.librefit.nav.LocalUnitSystem
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.NumberPicker
import org.librefit.ui.models.autoUnitSuffix
import org.librefit.ui.models.doubleValue
import org.librefit.ui.models.doubleValueAsString
import org.librefit.ui.models.formatToText
import org.librefit.ui.models.toWeight
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.BarbellWeightsImperial
import org.librefit.util.BarbellWeightsMetric
import org.librefit.util.Formatter.getDecimalDigitsAsInteger
import org.librefit.util.StandardPlatesImperial
import org.librefit.util.StandardPlatesMetric
import org.librefit.util.calculatePlates
import org.librefit.util.filterSmallPlates
import kotlin.math.abs

/** IPCA-style plate colors for metric plates, keyed by plate value. */
private val PlateColorsMetric = mapOf(
    25.0 to Color(0xFFD32F2F), // Red
    20.0 to Color(0xFF1976D2), // Blue
    15.0 to Color(0xFFFBC02D), // Yellow
    10.0 to Color(0xFF388E3C), // Green
    5.0 to Color(0xFFF5F5F5),  // White
    2.5 to Color(0xFF212121)   // Black
)

/** IPCA-style plate colors for imperial plates, keyed by plate value. */
private val PlateColorsImperial = mapOf(
    45.0 to Color(0xFFD32F2F),  // Red
    25.0 to Color(0xFF1976D2),  // Blue
    10.0 to Color(0xFFFBC02D),  // Yellow
    5.0 to Color(0xFF388E3C),   // Green
    2.5 to Color(0xFFF5F5F5),   // White
    1.25 to Color(0xFF212121)   // Black
)

/** Maximum number of plates drawn in the visualization before showing a "+N" indicator. */
private const val MAX_VISIBLE_PLATES = 7

/**
 * A modal bottom sheet that calculates the plates needed for a given target weight and barbell weight.
 *
 * @param initialTargetWeight The weight to calculate the plates for.
 * @param onDismiss Triggered when the user dismisses the bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarbellCalculatorModalBottomSheet(
    initialTargetWeight: Weight,
    onDismiss: () -> Unit
) {
    val unitSystem = LocalUnitSystem.current
    var targetWeight by remember(initialTargetWeight) { mutableStateOf(initialTargetWeight) }
    var includeSmallPlates by rememberSaveable { mutableStateOf(true) }
    var isAnyNumberPickerChanging by rememberSaveable { mutableStateOf(false) }

    val standardPlates = remember(unitSystem) {
        when (unitSystem) {
            UnitSystem.IMPERIAL -> StandardPlatesImperial
            UnitSystem.METRIC -> StandardPlatesMetric
        }
    }

    val barbellWeights = remember(unitSystem) {
        when (unitSystem) {
            UnitSystem.IMPERIAL -> BarbellWeightsImperial
            UnitSystem.METRIC -> BarbellWeightsMetric
        }
    }

    var barbellWeight by remember { mutableStateOf(barbellWeights.last()) }

    val availablePlates = remember(standardPlates, includeSmallPlates, unitSystem) {
        standardPlates.filterSmallPlates(includeSmallPlates, unitSystem).toImmutableList()
    }

    val plateBreakdown by remember(availablePlates) {
        derivedStateOf { calculatePlates(targetWeight, barbellWeight, availablePlates) }
    }

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LibreFitLazyColumn {
            item {
                Text(
                    text = stringResource(R.string.barbell_calculator),
                    style = MaterialTheme.typography.headlineLargeEmphasized,
                    textAlign = TextAlign.Center
                )
            }

            // Target Weight Input
            item {
                Card(
                    shape = MaterialTheme.shapes.largeIncreased,
                ) {
                    OutlinedCard(
                        shape = MaterialTheme.shapes.largeIncreased,
                        border = CardDefaults.outlinedCardBorder(false)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.target_weight),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            HorizontalDivider()
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                val iconSize = remember { 36.dp }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    IconButton(
                                        shapes = IconButtonDefaults.shapes(),
                                        onClick = {
                                            targetWeight -= Weight.auto(10.0, unitSystem)
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_replay_10),
                                            contentDescription = null,
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
                                    IconButton(
                                        shapes = IconButtonDefaults.shapes(),
                                        onClick = {
                                            targetWeight -= Weight.auto(5.0, unitSystem)
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_replay_5),
                                            contentDescription = null,
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    val currentDouble = targetWeight.doubleValue(unitSystem)
                                    NumberPicker(
                                        value = currentDouble.toInt(),
                                        options = (0..999).toImmutableList(),
                                        onValueChange = {
                                            targetWeight =
                                                (it.toDouble() + (currentDouble % 1)).toWeight(
                                                    unitSystem
                                                )
                                        },
                                        onNumberPickerScroll = { isAnyNumberPickerChanging = it },
                                        textStyle = MaterialTheme.typography.displaySmall
                                    )
                                    Text(
                                        text = ".",
                                        style = MaterialTheme.typography.displaySmall
                                    )
                                    NumberPicker(
                                        value = currentDouble.getDecimalDigitsAsInteger(),
                                        options = (0..99).toImmutableList(),
                                        label = { it.toString().padStart(2, '0') },
                                        onValueChange = {
                                            targetWeight =
                                                (currentDouble.toInt() + (it / 100.0)).toWeight(
                                                    unitSystem
                                                )
                                        },
                                        onNumberPickerScroll = { isAnyNumberPickerChanging = it },
                                        textStyle = MaterialTheme.typography.displaySmall
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = autoUnitSuffix(),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    IconButton(
                                        shapes = IconButtonDefaults.shapes(),
                                        onClick = {
                                            targetWeight += Weight.auto(10.0, unitSystem)
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_forward_10),
                                            contentDescription = null,
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
                                    IconButton(
                                        shapes = IconButtonDefaults.shapes(),
                                        onClick = {
                                            targetWeight += Weight.auto(5.0, unitSystem)
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_forward_5),
                                            contentDescription = null,
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.barbell_weight),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(barbellWeights) { weight ->
                                val isSelected = barbellWeight == weight
                                val chipStateDescription = stringResource(
                                    if (isSelected) R.string.chip_selected else R.string.chip_not_selected
                                )
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { barbellWeight = weight },
                                    label = { Text(weight.formatToText()) },
                                    modifier = Modifier.semantics {
                                        stateDescription = chipStateDescription
                                    }
                                )
                            }
                        }
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.include_small_plates)
                            )
                            Switch(
                                checked = includeSmallPlates,
                                onCheckedChange = {
                                    includeSmallPlates = it
                                }
                            )
                        }
                    }

                }
            }

            item {
                HeadlineText(
                    text = stringResource(R.string.plates_per_side)
                )
            }

            // Visualization
            item {
                BarbellVisualization(plateBreakdown.platesPerSide)
            }

            // Summary
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (plateBreakdown.platesPerSide.isEmpty()) {
                        Text(
                            text = if (targetWeight <= barbellWeight) {
                                stringResource(R.string.weight_le_bar)
                            } else {
                                stringResource(R.string.no_plates_needed)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    } else {
                        plateBreakdown.platesPerSide.forEach { (plate, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${count}x",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = plate.formatToText(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                    if (plateBreakdown.remainder > Weight.zero()) {
                        // Use the exact value from the domain layer; deriving it here from the
                        // rounded remainder compounds rounding and unit conversion error.
                        val closestAchievable = plateBreakdown.closestAchievable
                        Text(
                            text = stringResource(
                                R.string.closest_achievable,
                                closestAchievable.formatToText()
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Draws a simplified barbell with the given plates loaded per side.
 * Plates are drawn largest-first; when they would overflow the available width,
 * the spacing shrinks and a "+N" indicator shows the number of hidden plates.
 *
 * @param plates A map of plate weight to the count loaded per side.
 */
@Composable
private fun BarbellVisualization(plates: Map<Weight, Int>) {
    val flattenedPlates = remember(plates) {
        plates.keys.sortedDescending().flatMap { plateWeight ->
            List(plates[plateWeight] ?: 0) { plateWeight }
        }
    }
    val visiblePlates = flattenedPlates.take(MAX_VISIBLE_PLATES)
    val hiddenPlatesCount = flattenedPlates.size - visiblePlates.size
    val plateSpacing = if (flattenedPlates.size > 8) 1.dp else 2.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Bar sleeve (simplified)
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(12.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )

        // Stopper
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .background(MaterialTheme.colorScheme.outline)
        )

        // Plates
        Row(
            horizontalArrangement = Arrangement.spacedBy(plateSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            visiblePlates.forEach { plateWeight ->
                PlateView(plateWeight)
            }
            if (hiddenPlatesCount > 0) {
                Text(
                    text = "+$hiddenPlatesCount",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Rest of the sleeve
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )
    }
}

@Composable
private fun PlateView(weight: Weight) {
    val unitSystem = LocalUnitSystem.current
    val weightValue = weight.doubleValue()

    val colors = when (unitSystem) {
        UnitSystem.METRIC -> PlateColorsMetric
        UnitSystem.IMPERIAL -> PlateColorsImperial
    }
    val color = colors.entries
        .firstOrNull { (plateValue, _) -> abs(plateValue - weightValue) < 0.01 }
        ?.value
        ?: MaterialTheme.colorScheme.secondaryContainer

    val textColor = if (color.luminance() > 0.5f) Color.Black else Color.White

    val height = when {
        weightValue >= 10.0 -> 120.dp
        weightValue >= 5.0 -> 100.dp
        weightValue >= 2.5 -> 90.dp
        else -> 70.dp
    }

    val width = when {
        weightValue >= 20.0 -> 40.dp
        weightValue >= 10.0 -> 35.dp
        weightValue >= 5.0 -> 30.dp
        else -> 25.dp
    }

    val plateDescription = stringResource(R.string.plate_weight_description, weight.formatToText())
    val shape = MaterialTheme.shapes.medium
    val isLightColored = color.luminance() > 0.9f

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(color)
            .then(
                if (isLightColored) {
                    // Keep the white plate readable in light themes with a subtle border.
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
                } else {
                    Modifier
                }
            )
            .semantics { contentDescription = plateDescription },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = weight.doubleValueAsString(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.graphicsLayer(rotationZ = 90f),
            maxLines = 1
        )
    }
}

@Preview
@Composable
private fun BarbellCalculatorPreviewMetricDark() {
    val unitSystem = UnitSystem.METRIC
    CompositionLocalProvider(LocalUnitSystem provides unitSystem) {
        LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
            BarbellCalculatorModalBottomSheet(
                initialTargetWeight = Weight.auto(100.0, unitSystem),
                onDismiss = {})
        }
    }
}

@Preview
@Composable
private fun BarbellCalculatorPreviewImperialDark() {
    val unitSystem = UnitSystem.IMPERIAL
    CompositionLocalProvider(LocalUnitSystem provides unitSystem) {
        LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
            BarbellCalculatorModalBottomSheet(
                initialTargetWeight = Weight.auto(225.0, unitSystem),
                onDismiss = {})
        }
    }
}

@Preview
@Composable
private fun BarbellCalculatorPreviewEmptyState() {
    val unitSystem = UnitSystem.METRIC
    CompositionLocalProvider(LocalUnitSystem provides unitSystem) {
        LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
            BarbellCalculatorModalBottomSheet(
                initialTargetWeight = Weight.auto(15.0, unitSystem),
                onDismiss = {})
        }
    }
}
