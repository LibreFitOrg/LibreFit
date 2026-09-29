/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.util

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.librefit.enums.userPreferences.UnitSystem
import org.librefit.models.Weight
import org.librefit.models.Weight.Companion.NUMBER_OF_DECIMAL_DIGITS
import org.librefit.ui.models.doubleValue
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

/**
 * The result of a barbell plate calculation.
 *
 * @property platesPerSide A map of plate weight to the number of plates required per side,
 * ordered from heaviest to lightest.
 * @property remainder The leftover weight per side that could not be matched by the available
 * plates, rounded to [NUMBER_OF_DECIMAL_DIGITS]. Zero when the target is achieved exactly.
 * @property closestAchievable The exact total weight achievable with the calculated plates.
 * Computed from the unrounded leftover to avoid compounding rounding and unit conversion error.
 */
data class PlateBreakdown(
    val platesPerSide: Map<Weight, Int>,
    val remainder: Weight,
    val closestAchievable: Weight
)

/** Standard competition-style metric plates, including fractional plates. */
val StandardPlatesMetric: ImmutableList<Weight> =
    persistentListOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25, 0.5, 0.25)
        .map { Weight.kilograms(it) }
        .toImmutableList()

/** Standard imperial plates, including fractional plates. */
val StandardPlatesImperial: ImmutableList<Weight> =
    persistentListOf(45.0, 25.0, 10.0, 5.0, 2.5, 1.25)
        .map { Weight.pounds(it) }
        .toImmutableList()

/** Standard olympic barbell weights in kilograms. */
val BarbellWeightsMetric: ImmutableList<Weight> =
    persistentListOf(20.0, 15.0, 10.0, 5.0)
        .map { Weight.kilograms(it) }
        .toImmutableList()

/** Standard barbell weights in pounds. */
val BarbellWeightsImperial: ImmutableList<Weight> =
    persistentListOf(45.0, 35.0, 25.0)
        .map { Weight.pounds(it) }
        .toImmutableList()


/** Fractional "small" plate values in kilograms. */
private val SmallPlateValuesKg = setOf(0.25, 0.5, 1.25)

/** Fractional "small" plate values in pounds. */
private val SmallPlateValuesLb = setOf(1.25)

private const val PLATE_MATCH_TOLERANCE = 0.01

/**
 * Tolerance used when counting how many plates fit in the remaining weight. It absorbs floating
 * point error from unit conversions (e.g. lb values that are not exact in kg) without making a
 * plate "fit" when it genuinely does not.
 */
private const val PLATE_COUNT_EPSILON = 1e-4

/**
 * Tolerance (in kilograms) used when matching a barbell weight to its approximate equivalent in
 * the other unit system. Standard equivalents are approximate by definition (20 kg ≈ 45 lb).
 */


/**
 * Calculates the plates required per side to reach [target] with a bar of weight [bar],
 * using a greedy largest-first strategy.
 *
 * @param target The total weight to achieve, including the bar.
 * @param bar The weight of the barbell itself.
 * @param availablePlates The plates that may be loaded on each side.
 * @return A [PlateBreakdown] with the plates per side and the unmatched remainder per side.
 */
fun calculatePlates(
    target: Weight,
    bar: Weight,
    availablePlates: List<Weight>
): PlateBreakdown {
    // Do NOT round the intermediate per-side requirement: lb values converted to kg carry long
    // decimal fractions, and rounding here would make plates no longer fit (e.g. 2x45 lb =
    // 40.82331 kg would be rounded down to 40.82 kg). Rounding happens only on the output.
    val perSideRequired = (target - bar).inKilograms / 2.0

    if (perSideRequired <= 0.0) {
        return PlateBreakdown(
            platesPerSide = emptyMap(),
            remainder = Weight.zero(),
            closestAchievable = target
        )
    }

    var remaining = perSideRequired
    val result = LinkedHashMap<Weight, Int>()

    for (plate in availablePlates.sortedDescending()) {
        // Tolerant floor so that floating point noise from unit conversion cannot drop a plate
        // that genuinely fits (e.g. floor(1.9999999 + 1e-6) == 2).
        val count = ((remaining / plate.inKilograms) + PLATE_COUNT_EPSILON).toInt()
        if (count > 0) {
            result[plate] = count
            remaining -= plate.inKilograms * count
            if (remaining <= PLATE_MATCH_TOLERANCE / 10) break
        }
    }

    // `+ 0.0` normalizes a possible -0.0 to 0.0 so that Weight equality holds when boxed.
    val remainder = Weight.kilograms(remaining.roundToDecimalDigits().coerceAtLeast(0.0) + 0.0)

    // The achievable total is exact: bar + the per-side plate mass actually loaded. Deriving it
    // from the unrounded leftover (instead of the rounded remainder) keeps it accurate when
    // displayed in a different unit system than the calculation was performed in.
    val closestAchievable = Weight.kilograms(
        ((perSideRequired - remaining) * 2.0 + bar.inKilograms).coerceAtLeast(0.0)
    )
    return PlateBreakdown(
        platesPerSide = result,
        remainder = remainder,
        closestAchievable = closestAchievable
    )
}

/**
 * Determines whether the given plate counts as a "small" (fractional) plate in [unitSystem].
 *
 * @param unitSystem The unit system used to interpret the plate value.
 * @return `true` for kg plates of 0.25, 0.5 or 1.25, and lb plates of 1.25.
 */
fun Weight.isSmallPlate(unitSystem: UnitSystem): Boolean {
    val value = doubleValue(unitSystem)
    val smallValues = when (unitSystem) {
        UnitSystem.METRIC -> SmallPlateValuesKg
        UnitSystem.IMPERIAL -> SmallPlateValuesLb
    }
    return smallValues.any { abs(it - value) < 1e-6 }
}

/**
 * Filters a list of plates, optionally excluding small (fractional) plates.
 *
 * @param includeSmallPlates Whether small plates should be kept.
 * @param unitSystem The unit system used to classify plates.
 * @return The filtered list of plates.
 */
fun List<Weight>.filterSmallPlates(
    includeSmallPlates: Boolean,
    unitSystem: UnitSystem
): List<Weight> = if (includeSmallPlates) this else filterNot { it.isSmallPlate(unitSystem) }

private fun Double.roundToDecimalDigits(digits: Int = NUMBER_OF_DECIMAL_DIGITS): Double {
    val multiplier = 10.0.pow(digits.toDouble())
    return round(this * multiplier) / multiplier
}
