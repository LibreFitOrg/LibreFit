/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.lang.Math.toIntExact
import kotlin.math.abs
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Represents the state of various input modal bottom sheets used throughout the application,
 * such as for time, weight, or repetitions.
 */
sealed class InputModalBottomSheetState {
    /**
     * State holder for inputs requiring minutes and seconds.
     *
     * @property minutes The currently selected minutes value.
     * @property seconds The currently selected seconds value.
     * @property minutesRange The allowed range of values for minutes.
     * @property secondsRange The allowed range of values for seconds.
     */
    data class MinutesSeconds(
        val minutes: Int = 0,
        val seconds: Int = 0,
        val minutesRange: ImmutableList<Int> = (0..59).toImmutableList(),
        val secondsRange: ImmutableList<Int> = (0..59).toImmutableList()
    ) : InputModalBottomSheetState() {
        init {
            require(minutes in minutesRange) {
                "minutes $minutes must be in minutesRange: $minutesRange"
            }
            require(seconds in secondsRange) {
                "seconds $seconds must be in secondsRange: $secondsRange"
            }
        }


        /** Convenience property returning the combined duration of minutes and seconds. */
        val duration: Duration get() = minutes.minutes + seconds.seconds

        /** Convenience property returning the total duration in whole seconds. */
        val totalSeconds: Int get() = toIntExact(duration.inWholeSeconds)
    }

    /**
     * State holder for weight inputs with integer and decimal components, custom steps, and ranges.
     * Validation happens in factory methods.
     *
     * @property integerWeight The integer part of the weight.
     * @property decimalWeight The decimal part of the weight.
     * @property integerStep The step increment for the integer part.
     * @property decimalStep The step increment for the decimal part.
     * @property integerWeightRange The allowed range for the integer part.
     * @property decimalWeightRange The allowed range for the decimal part.
     */
    @ConsistentCopyVisibility
    data class Weight private constructor(
        val integerWeight: Int,
        val decimalWeight: Int,
        val integerStep: Int,
        val decimalStep: Int,
        val integerWeightRange: ImmutableList<Int>,
        val decimalWeightRange: ImmutableList<Int>
    ) : InputModalBottomSheetState() {

        companion object {
            /**
             * Creates a copy of this [Weight] instance with custom values, snapping or aligning
             * them to the valid weight ranges.
             */
            fun Weight.safeCopy(
                integerWeight: Int = this.integerWeight,
                decimalWeight: Int = this.decimalWeight,
                integerStep: Int = this.integerStep,
                decimalStep: Int = this.decimalStep,
                integerWeightRange: ImmutableList<Int> = this.integerWeightRange,
                decimalWeightRange: ImmutableList<Int> = this.decimalWeightRange
            ): Weight {
                return create(
                    integerWeight = integerWeight,
                    decimalWeight = decimalWeight,
                    integerStep = integerStep,
                    decimalStep = decimalStep,
                    decimalWeightRange = decimalWeightRange,
                    integerWeightRange = integerWeightRange
                )
            }

            /**
             * Factory method to create a [Weight] instance, ensuring that integer and decimal weights
             * are safely aligned to the closest available values within their respective ranges.
             */
            fun create(
                integerWeight: Int = 0,
                decimalWeight: Int = 0,
                integerStep: Int = 1,
                decimalStep: Int = 5,
                integerWeightRange: ImmutableList<Int> = (0..999 step integerStep).toImmutableList(),
                decimalWeightRange: ImmutableList<Int> = (0..99 step decimalStep).toImmutableList()
            ): Weight {
                return Weight(
                    integerWeight = integerWeightRange.minByOrNull { abs(it - integerWeight) } ?: 0,
                    decimalWeight = decimalWeightRange.minByOrNull { abs(it - decimalWeight) } ?: 0,
                    integerStep = integerStep,
                    decimalStep = decimalStep,
                    integerWeightRange = integerWeightRange,
                    decimalWeightRange = decimalWeightRange
                )
            }
        }

        init {
            require(integerWeight in integerWeightRange) {
                "integerWeight $integerWeight must be in integerWeightRange: $integerWeightRange"
            }
            require(decimalWeight in decimalWeightRange) {
                "decimalWeight $decimalWeight must be in decimalWeightRange: $decimalWeightRange"
            }
        }

        private val divisor: Double =
            10.0.pow(decimalWeightRange.max().toString().length.toDouble())

        /** Convenience property returning the total weight as a combined floating-point value. */
        val totalWeight: Double get() = integerWeight + (decimalWeight / divisor)
    }

    /**
     * State holder for repetition count inputs.
     *
     * @property reps The current repetition count.
     * @property repsRange The allowed range of repetition values.
     */
    data class Reps(
        val reps: Int = 0,
        val repsRange: ImmutableList<Int> = (0..999).toImmutableList()
    ) : InputModalBottomSheetState() {
        init {
            require(reps in repsRange) {
                "reps $reps must be in repsRange: $repsRange"
            }
        }
    }

    /**
     * State holder for inputs requiring hours, minutes, and seconds.
     *
     * @property hours The currently selected hours value.
     * @property minutes The currently selected minutes value.
     * @property seconds The currently selected seconds value.
     * @property hoursRange The allowed range for hours.
     * @property minutesRange The allowed range for minutes.
     * @property secondsRange The allowed range for seconds.
     */
    data class HoursMinutesSeconds(
        val hours: Int = 0,
        val minutes: Int = 0,
        val seconds: Int = 0,
        val hoursRange: ImmutableList<Int> = (0..23).toImmutableList(),
        val minutesRange: ImmutableList<Int> = (0..59).toImmutableList(),
        val secondsRange: ImmutableList<Int> = (0..59).toImmutableList()
    ) : InputModalBottomSheetState() {
        init {
            require(hours in hoursRange) {
                "hours $hours must be in hoursRange: $hoursRange"
            }
            require(minutes in minutesRange) {
                "minutes $minutes must be in minutesRange: $minutesRange"
            }
            require(seconds in secondsRange) {
                "seconds $seconds must be in secondsRange: $secondsRange"
            }
        }

        /** Convenience property returning the combined duration of hours, minutes, and seconds. */
        val duration: Duration get() = hours.hours + minutes.minutes + seconds.seconds

        /** Convenience property returning the total duration in whole seconds. */
        val totalSeconds: Int get() = toIntExact(duration.inWholeSeconds)
    }
}