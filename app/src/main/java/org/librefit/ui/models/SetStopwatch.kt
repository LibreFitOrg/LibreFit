/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

/**
 * It prepares the set before its stopwatch is started. The first time, [UiSet.elapsedTime] is taken
 * as [UiSet.targetTime] and the stopwatch starts from zero. Otherwise the stopwatch continues from
 * [UiSet.elapsedTime].
 */
fun UiSet.withStartedStopwatch(): UiSet {
    return if (targetTime == null) copy(targetTime = elapsedTime, elapsedTime = 0) else this
}

/**
 * It applies a time changed by the user while the stopwatch of the set is not running. The new time
 * is the new target duration, so [UiSet.targetTime] is reset and the stopwatch will start again
 * from zero.
 */
fun UiSet.withEditedTime(time: Int): UiSet {
    return if (time != elapsedTime) copy(elapsedTime = time, targetTime = null) else this
}

/**
 * It returns `true` when the stopwatch started at [initialElapsedTime] has reached
 * [UiSet.targetTime] with [newElapsedTime]. If the stopwatch is resumed after the target duration
 * has already been reached, it returns always `false` so the alert is not repeated.
 */
fun UiSet.hasReachedTargetTime(initialElapsedTime: Int, newElapsedTime: Int): Boolean {
    val target = targetTime ?: return false
    return target > initialElapsedTime && newElapsedTime >= target
}
