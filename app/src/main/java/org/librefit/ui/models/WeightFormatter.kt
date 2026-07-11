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

@Composable
fun Weight.doubleValue(): Double {
    val unitSystem = LocalUnitSystem.current

    return this.doubleValue(unitSystem)
}

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