/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.models

import androidx.annotation.FloatRange
import kotlinx.serialization.Serializable
import org.librefit.enums.userPreferences.UnitSystem
import org.librefit.models.Weight.Companion.MAX_WEIGHT_IN_KILOGRAMS
import org.librefit.models.Weight.Companion.MIN_WEIGHT_IN_KILOGRAMS

/**
 * A type-safe wrapper for weight values, represented in kilograms.
 * 
 * This class uses [JvmInline] to provide a zero-cost abstraction, meaning that at runtime,
 * it is represented by the underlying [Double] value, preventing unnecessary object allocations.
 * It enforces business constraints on weight range during both construction and factory-based instantiation.
 *
 * @see <a href="https://kotlinlang.org/docs/inline-classes.html">Kotlin Inline Value Classes Documentation</a>
 * @property inKilograms The underlying weight value in kilograms.
 */
@Serializable
@JvmInline
value class Weight private constructor(
    @param:FloatRange(
        from = MIN_WEIGHT_IN_KILOGRAMS,
        to = MAX_WEIGHT_IN_KILOGRAMS
    ) val inKilograms: Double
) : Comparable<Weight> {

    /**
     * The weight value represented in pounds (lbs).
     */
    val inPounds: Double get() = inKilograms * KILOGRAMS_TO_POUNDS

    override fun compareTo(other: Weight): Int = inKilograms.compareTo(other.inKilograms)

    operator fun plus(other: Weight): Weight = kilograms(
        (this.inKilograms + other.inKilograms).coerceIn(
            MIN_WEIGHT_IN_KILOGRAMS,
            MAX_WEIGHT_IN_KILOGRAMS
        )
    )

    operator fun minus(other: Weight): Weight = kilograms(
        (this.inKilograms - other.inKilograms).coerceIn(
            MIN_WEIGHT_IN_KILOGRAMS,
            MAX_WEIGHT_IN_KILOGRAMS
        )
    )

    operator fun times(multiplier: Double): Weight = kilograms(
        (this.inKilograms * multiplier).coerceIn(MIN_WEIGHT_IN_KILOGRAMS, MAX_WEIGHT_IN_KILOGRAMS)
    )

    operator fun div(scalar: Double): Weight = kilograms(
        (this.inKilograms / scalar).coerceIn(MIN_WEIGHT_IN_KILOGRAMS, MAX_WEIGHT_IN_KILOGRAMS)
    )

    operator fun div(other: Weight): Double = this.inKilograms / other.inKilograms


    companion object {
        /** Minimum allowable weight in kilograms. */
        const val MIN_WEIGHT_IN_KILOGRAMS = 0.0

        /** Maximum allowable weight in kilograms. */
        const val MAX_WEIGHT_IN_KILOGRAMS = 999.0

        /** Standard conversion factor for pounds to kilograms (1 lb ≈ 0.45359237 kg). */
        const val POUNDS_TO_KILOGRAMS = 0.45359237

        /** Standard conversion factor for kilograms to pounds (1 kg ≈ 2.20462262 lbs). */
        const val KILOGRAMS_TO_POUNDS = 2.2046226218487757

        /** Self-explanatory **/
        const val NUMBER_OF_DECIMAL_DIGITS = 2

        /**
         * Factory method to create a [Weight] instance from a kilogram value.
         * 
         * @param value The weight in kilograms.
         * @throws IllegalArgumentException if the provided [value] is outside the permitted [MIN_WEIGHT_IN_KILOGRAMS] and [MAX_WEIGHT_IN_KILOGRAMS] range.
         * @return A valid [Weight] instance.
         */
        fun kilograms(
            @FloatRange(from = MIN_WEIGHT_IN_KILOGRAMS, to = MAX_WEIGHT_IN_KILOGRAMS) value: Double
        ): Weight {
            require(value in MIN_WEIGHT_IN_KILOGRAMS..MAX_WEIGHT_IN_KILOGRAMS) {
                "Weight must be between $MIN_WEIGHT_IN_KILOGRAMS and $MAX_WEIGHT_IN_KILOGRAMS. Actual: $value"
            }

            return Weight(value)
        }

        /**
         * Factory method to create a [Weight] instance from a pound value.
         * 
         * @param value The weight in pounds.
         * @throws IllegalArgumentException if the resulting kilogram conversion is outside the permitted range.
         * @return A valid [Weight] instance.
         */
        fun pounds(
            @FloatRange(
                from = MIN_WEIGHT_IN_KILOGRAMS * KILOGRAMS_TO_POUNDS,
                to = MAX_WEIGHT_IN_KILOGRAMS * KILOGRAMS_TO_POUNDS
            )
            value: Double
        ): Weight {
            val inKg = value * POUNDS_TO_KILOGRAMS
            return kilograms(inKg)
        }

        /**
         * Factory method to create a [Weight] instance from [value] automatically parsed based on [unitSystem]
         *
         * @param value The weight value
         * @param unitSystem The unit system value
         * @throws IllegalArgumentException if the resulting kilogram conversion is outside the permitted range.
         * @return A valid [Weight] instance.
         */
        fun auto(value: Double, unitSystem: UnitSystem): Weight {
            return when (unitSystem) {
                UnitSystem.METRIC -> kilograms(value)
                UnitSystem.IMPERIAL -> pounds(value)
            }
        }

        /**
         * Factory method to create a [Weight] instance with value zero kilograms/pounds
         *
         * @return A valid [Weight] instance.
         */
        fun zero(): Weight {
            return kilograms(0.0)
        }
    }
}