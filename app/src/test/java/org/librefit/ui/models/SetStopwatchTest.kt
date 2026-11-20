/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.models

import org.librefit.ui.models.mappers.toEntity
import org.librefit.ui.models.mappers.toUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SetStopwatchTest {

    @Test
    fun `withStartedStopwatch takes time as target and starts from zero the first time`() {
        val set = UiSet(elapsedTime = 60).withStartedStopwatch()

        assertEquals(60, set.targetTime)
        assertEquals(0, set.elapsedTime)
    }

    @Test
    fun `withStartedStopwatch continues from elapsed time when already started`() {
        val set = UiSet(elapsedTime = 20, targetTime = 60)

        assertEquals(set, set.withStartedStopwatch())
    }

    @Test
    fun `withStartedStopwatch keeps progress of a restored workout`() {
        // A set restored from the database after the app has been killed
        val restoredSet = UiSet(elapsedTime = 20, targetTime = 60).toEntity().toUi()

        val set = restoredSet.withStartedStopwatch()

        assertEquals(60, set.targetTime)
        assertEquals(20, set.elapsedTime)
    }

    @Test
    fun `withEditedTime sets new target when time is changed while paused`() {
        val set = UiSet(elapsedTime = 20, targetTime = 60)
            .withEditedTime(90)
            .withStartedStopwatch()

        assertEquals(90, set.targetTime)
        assertEquals(0, set.elapsedTime)
    }

    @Test
    fun `withEditedTime ignores unchanged time`() {
        val set = UiSet(elapsedTime = 20, targetTime = 60)

        assertEquals(set, set.withEditedTime(20))
    }

    @Test
    fun `withEditedTime resets target`() {
        assertNull(UiSet(elapsedTime = 20, targetTime = 60).withEditedTime(30).targetTime)
    }

    @Test
    fun `hasReachedTargetTime is true only when target is reached`() {
        val set = UiSet(targetTime = 60)

        assertFalse(set.hasReachedTargetTime(initialElapsedTime = 0, newElapsedTime = 59))
        assertTrue(set.hasReachedTargetTime(initialElapsedTime = 0, newElapsedTime = 60))
        assertTrue(set.hasReachedTargetTime(initialElapsedTime = 20, newElapsedTime = 61))
    }

    @Test
    fun `hasReachedTargetTime is false without target`() {
        assertFalse(UiSet(targetTime = 0).hasReachedTargetTime(0, 10))
        assertFalse(UiSet(targetTime = null).hasReachedTargetTime(0, 10))
    }

    @Test
    fun `hasReachedTargetTime is false when resumed after target`() {
        val set = UiSet(elapsedTime = 60, targetTime = 60)

        assertFalse(set.hasReachedTargetTime(initialElapsedTime = 60, newElapsedTime = 61))
    }
}
