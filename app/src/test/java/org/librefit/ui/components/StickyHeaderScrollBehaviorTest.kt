/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class StickyHeaderScrollBehaviorTest {

    private companion object {
        const val HEADER_HEIGHT = 200f
    }

    private fun measuredState(headerHeight: Float = HEADER_HEIGHT): StickyHeaderScrollState =
        StickyHeaderScrollState().apply { onHeaderSizeChanged(headerHeight.toInt()) }

    private fun connection(
        state: StickyHeaderScrollState,
        isEnabled: Boolean = true,
    ): StickyHeaderScrollConnection =
        StickyHeaderScrollConnection(state) { isEnabled }

    @Test
    fun `consumeScrollDelta collapses header on scroll down and reports consumed delta`() {
        val state = measuredState()

        val consumed = state.consumeScrollDelta(-120f)

        assertThat(consumed).isEqualTo(-120f)
        assertThat(state.collapsedPx).isEqualTo(120f)
    }

    @Test
    fun `consumeScrollDelta clamps collapse at the header height`() {
        val state = measuredState()

        val consumed = state.consumeScrollDelta(-300f)

        assertThat(consumed).isEqualTo(-HEADER_HEIGHT)
        assertThat(state.collapsedPx).isEqualTo(HEADER_HEIGHT)
    }

    @Test
    fun `consumeScrollDelta reveals header on scroll up with priority over the list`() {
        val state = measuredState().apply { consumeScrollDelta(-HEADER_HEIGHT) }

        val consumed = state.consumeScrollDelta(150f)

        assertThat(consumed).isEqualTo(150f)
        assertThat(state.collapsedPx).isEqualTo(50f)
    }

    @Test
    fun `consumeScrollDelta clamps reveal at the fully expanded position`() {
        val state = measuredState().apply { consumeScrollDelta(-100f) }

        val consumed = state.consumeScrollDelta(150f)

        assertThat(consumed).isEqualTo(100f)
        assertThat(state.collapsedPx).isEqualTo(0f)
    }

    @Test
    fun `consumeScrollDelta ignores scroll down when fully collapsed`() {
        val state = measuredState().apply { consumeScrollDelta(-HEADER_HEIGHT) }

        val consumed = state.consumeScrollDelta(-50f)

        assertThat(consumed).isEqualTo(0f)
        assertThat(state.collapsedPx).isEqualTo(HEADER_HEIGHT)
    }

    @Test
    fun `consumeScrollDelta ignores scroll up when fully expanded`() {
        val state = measuredState()

        val consumed = state.consumeScrollDelta(50f)

        assertThat(consumed).isEqualTo(0f)
        assertThat(state.collapsedPx).isEqualTo(0f)
    }

    @Test
    fun `consumeScrollDelta ignores delta when the header height is not measured`() {
        val state = StickyHeaderScrollState()

        val consumed = state.consumeScrollDelta(-50f)

        assertThat(consumed).isEqualTo(0f)
        assertThat(state.collapsedPx).isEqualTo(0f)
    }

    @Test
    fun `onHeaderSizeChanged clamps existing collapse to the new height`() {
        val state = measuredState().apply { consumeScrollDelta(-150f) }

        state.onHeaderSizeChanged(100)

        assertThat(state.headerHeightPx).isEqualTo(100f)
        assertThat(state.collapsedPx).isEqualTo(100f)
        assertThat(state.isFullyCollapsed).isTrue()
    }

    @Test
    fun `settleTargetPx reveals on upward fling`() {
        val target = StickyHeaderScrollState.settleTargetPx(
            collapsedPx = 120f,
            headerHeightPx = HEADER_HEIGHT,
            flingVelocityY = 500f,
        )

        assertThat(target).isEqualTo(0f)
    }

    @Test
    fun `settleTargetPx collapses on downward fling`() {
        val target = StickyHeaderScrollState.settleTargetPx(
            collapsedPx = 50f,
            headerHeightPx = HEADER_HEIGHT,
            flingVelocityY = -500f,
        )

        assertThat(target).isEqualTo(HEADER_HEIGHT)
    }

    @Test
    fun `settleTargetPx collapses past half height when fling velocity is zero`() {
        val target = StickyHeaderScrollState.settleTargetPx(
            collapsedPx = 120f,
            headerHeightPx = HEADER_HEIGHT,
            flingVelocityY = 0f,
        )

        assertThat(target).isEqualTo(HEADER_HEIGHT)
    }

    @Test
    fun `settleTargetPx reveals before half height when fling velocity is zero`() {
        val target = StickyHeaderScrollState.settleTargetPx(
            collapsedPx = 50f,
            headerHeightPx = HEADER_HEIGHT,
            flingVelocityY = 0f,
        )

        assertThat(target).isEqualTo(0f)
    }

    @Test
    fun `settleTargetPx ignores velocity when the header height is not measured`() {
        val target = StickyHeaderScrollState.settleTargetPx(
            collapsedPx = 0f,
            headerHeightPx = 0f,
            flingVelocityY = -500f,
        )

        assertThat(target).isEqualTo(0f)
    }

    @Test
    fun `connection consumes scroll delta only while enabled`() {
        val state = measuredState().apply { updateScrollPosition(isAtTop = false, 0) }

        val consumedEnabled = connection(state)
            .onPreScroll(Offset(0f, -120f), NestedScrollSource.SideEffect)

        assertThat(consumedEnabled).isEqualTo(Offset(0f, -120f))
        assertThat(state.collapsedPx).isEqualTo(120f)

        val consumedDisabled = connection(state, isEnabled = false)
            .onPreScroll(Offset(0f, -50f), NestedScrollSource.SideEffect)

        assertThat(consumedDisabled).isEqualTo(Offset.Zero)
        assertThat(state.collapsedPx).isEqualTo(120f)
    }

    @Test
    fun `connection reports zero consumption when the header cannot move further`() {
        val fullyCollapsed = measuredState().apply {
            updateScrollPosition(isAtTop = false, 0)
            consumeScrollDelta(-HEADER_HEIGHT)
        }
        val fullyExpanded = measuredState().apply {
            updateScrollPosition(isAtTop = false, 0)
        }

        assertThat(
            connection(fullyCollapsed)
                .onPreScroll(Offset(0f, -10f), NestedScrollSource.SideEffect),
        ).isEqualTo(Offset.Zero)

        assertThat(
            connection(fullyExpanded)
                .onPreScroll(Offset(0f, 10f), NestedScrollSource.SideEffect),
        ).isEqualTo(Offset.Zero)
    }

    @Test
    fun `onPostFling leaves the state untouched while disabled`() = runBlocking {
        val state = measuredState().apply { consumeScrollDelta(-100f) }

        val result = connection(state, isEnabled = false)
            .onPostFling(Velocity.Zero, Velocity(x = 0f, y = -500f))

        assertThat(result).isEqualTo(Velocity.Zero)
        assertThat(state.collapsedPx).isEqualTo(100f)
    }

    @Test
    fun `updateScrollPosition tracks scroll position and calculates collapsedPx when at top`() {
        val state = measuredState()

        // Initially at top (isAtTop = true by default)
        assertThat(state.isAtTop).isTrue()
        assertThat(state.translationY).isEqualTo(0f)

        // At top with scroll offset
        state.updateScrollPosition(isAtTop = true, firstVisibleItemScrollOffset = 50)
        assertThat(state.isAtTop).isTrue()
        assertThat(state.collapsedPx).isEqualTo(50f)
        assertThat(state.translationY).isEqualTo(0f) // translationY is 0f when at top

        // Scroll offset clamped to header height
        state.updateScrollPosition(isAtTop = true, firstVisibleItemScrollOffset = 300)
        assertThat(state.isAtTop).isTrue()
        assertThat(state.collapsedPx).isEqualTo(HEADER_HEIGHT)
        assertThat(state.translationY).isEqualTo(0f)

        // Not at top anymore
        state.updateScrollPosition(isAtTop = false, firstVisibleItemScrollOffset = 100)
        assertThat(state.isAtTop).isFalse()
        assertThat(state.translationY).isEqualTo(-HEADER_HEIGHT) // translationY is -collapsedPx when not at top
    }

    @Test
    fun `connection ignores downward scroll delta when positioned at the top of the list`() {
        val state = measuredState()
        state.updateScrollPosition(isAtTop = true, firstVisibleItemScrollOffset = 0)

        // Downward scroll (negative delta available.y) at top should not be consumed by connection
        val consumed = connection(state)
            .onPreScroll(Offset(0f, -50f), NestedScrollSource.SideEffect)

        assertThat(consumed).isEqualTo(Offset.Zero)
    }

    @Test
    fun `connection consumes downward scroll delta when not at top of the list`() {
        val state = measuredState()
        state.updateScrollPosition(isAtTop = false, firstVisibleItemScrollOffset = 0)

        // Downward scroll (negative delta available.y) when not at top should be consumed by connection
        val consumed = connection(state)
            .onPreScroll(Offset(0f, -50f), NestedScrollSource.SideEffect)

        assertThat(consumed).isEqualTo(Offset(0f, -50f))
        assertThat(state.collapsedPx).isEqualTo(50f)
    }
}
