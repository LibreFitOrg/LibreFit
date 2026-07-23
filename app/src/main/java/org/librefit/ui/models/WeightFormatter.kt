/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import org.librefit.R
import org.librefit.enums.userPreferences.UnitSystem
import org.librefit.models.Weight
import org.librefit.models.Weight.Companion.NUMBER_OF_DECIMAL_DIGITS
import org.librefit.nav.LocalUnitSystem
import java.util.Locale
import kotlin.math.floor
import kotlin.math.pow


@Composable
fun Weight.formatToText(
    numberOfDecimalDigits: Int = NUMBER_OF_DECIMAL_DIGITS
): String {
    val unitSystem = LocalUnitSystem.current

    return remember(this, unitSystem) {
        // Automatically translates "kg"/"lbs" depending on device language
        val format = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT)

        val multiplier = 10.0.pow(numberOfDecimalDigits)
        if (unitSystem == UnitSystem.METRIC) {
            format.format(
                Measure(
                    floor(inKilograms * multiplier) / multiplier,
                    MeasureUnit.KILOGRAM
                )
            )
        } else {
            format.format(Measure(floor(inPounds * multiplier) / multiplier, MeasureUnit.POUND))
        }
    }
}

/**
 * It returns value of [Weight] instance in current [UnitSystem] without any suffix or unit
 */
@Composable
fun Weight.doubleValue(): Double {
    val unitSystem = LocalUnitSystem.current

    return remember(this, unitSystem) {
        this.doubleValue(unitSystem)
    }
}

/**
 * It returns value of [Weight] instance in current [UnitSystem] as string without any suffix or unit
 */
@Composable
fun Weight.doubleValueAsString(): String {
    return this.doubleValue().toString()
}

/**
 * It returns value of [Weight] instance based on passed [unitSystem]
 */
fun Weight.doubleValue(
    unitSystem: UnitSystem,
    numberOfDecimalDigits: Int = NUMBER_OF_DECIMAL_DIGITS
): Double {
    val multiplier = 10.0.pow(numberOfDecimalDigits.toDouble())
    return floor(
        if (unitSystem == UnitSystem.METRIC) {
            inKilograms
        } else {
            inPounds
        } * multiplier
    ) / multiplier
}

fun Double.toWeight(unitSystem: UnitSystem): Weight {
    return Weight.auto(this, unitSystem)
}

@Composable
fun Double.toWeight(): Weight {
    val unitSystem = LocalUnitSystem.current
    return remember(this, unitSystem) {
        this.toWeight(unitSystem)
    }
}

/**
 * Returns the correct string resource unit suffix based on current [UnitSystem]
 */
fun autoUnitSuffix(unitSystem: UnitSystem): Int {
    return when (unitSystem) {
        UnitSystem.METRIC -> R.string.kg
        UnitSystem.IMPERIAL -> R.string.lb
    }
}

/**
 * Returns the correct unit suffix based on current [UnitSystem]
 */
@Composable
fun autoUnitSuffix(): String {
    val unitSystem = LocalUnitSystem.current

    val id = rememberSaveable(unitSystem) { autoUnitSuffix(unitSystem) }

    return stringResource(id)
}