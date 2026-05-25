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
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

sealed class InputModalBottomSheetState {
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


        val duration: Duration get() = minutes.minutes + seconds.seconds

        val totalSeconds: Int get() = toIntExact(duration.inWholeSeconds)
    }

    data class Weight(
        val integerWeight: Int = 0,
        val decimalWeight: Int = 0,
        val integerWeightRange: ImmutableList<Int> = (0..999).toImmutableList(),
        val decimalWeightRange: ImmutableList<Int> = (0..99).toImmutableList()
    ) : InputModalBottomSheetState() {

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

        val totalWeight: Double get() = integerWeight + (decimalWeight / divisor)
    }

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

        val duration: Duration get() = hours.hours + minutes.minutes + seconds.seconds

        val totalSeconds: Int get() = toIntExact(duration.inWholeSeconds)
    }
}