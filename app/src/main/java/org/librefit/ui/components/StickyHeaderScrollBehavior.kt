/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * Animation used to settle the header to a fully visible or fully hidden position once a
 * scroll gesture ends, matching the feel of Material 3 app bar scroll behaviors.
 */
private val SettleAnimationSpec: AnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

/**
 * Holds the transient scroll state of a sticky header that disappears when the list is
 * scrolled down and re-appears when the list is scrolled up, mirroring the `enterAlways`
 * behavior of Material 3 app bars.
 *
 * Create it with [rememberStickyHeaderScrollState], attach the [StickyHeaderScrollConnection]
 * of the same state to the scrollable list (or any of its ancestors) with the
 * `Modifier.nestedScroll` modifier, measure the header by forwarding its size to
 * [onHeaderSizeChanged], and apply [translationY] to the sticky header item with a
 * `Modifier.graphicsLayer` block. Translating the sticky header keeps it sliding up and out
 * of view without shrinking or shifting the scrollable list container;
 * clip the list's parent (`Modifier.clipToBounds`) so the translated header disappears
 * underneath the screen's app bar instead of drawing over it.
 *
 * @param headerHeightPxState backing state for the measured header height, in pixels.
 * @param collapsedPxState backing state for the hidden portion of the header, in pixels.
 * @param isAtTopState backing state indicating whether the scrollable list is at index 0.
 */
@Stable
class StickyHeaderScrollState(
    private val headerHeightPxState: MutableFloatState = mutableFloatStateOf(0f),
    private val collapsedPxState: MutableFloatState = mutableFloatStateOf(0f),
    private val isAtTopState: MutableState<Boolean> = mutableStateOf(true),
) {
    /** The measured height of the sticky header, in pixels. `0` until measured. */
    val headerHeightPx: Float
        get() = headerHeightPxState.floatValue

    /** How many pixels of the sticky header are currently hidden, in the range `[0, headerHeightPx]`. */
    val collapsedPx: Float
        get() = collapsedPxState.floatValue

    /** Whether the scrollable list is currently positioned at the top (first item / index 0). */
    var isAtTop: Boolean
        get() = isAtTopState.value
        internal set(value) {
            isAtTopState.value = value
        }

    /**
     * Vertical translation, in pixels, to apply to the sticky header content while it hides,
     * with a `Modifier.graphicsLayer` block.
     *
     * When at the top of the list ([isAtTop] is `true`), translation is `0f` so that the
     * header moves naturally with the list layout. When scrolled down past the first item
     * ([isAtTop] is `false`), translation is `-collapsedPx` to slide the pinned header in/out.
     */
    val translationY: Float
        get() = if (isAtTop) 0f else -collapsedPx

    /** `true` when the header is completely hidden from the viewport. */
    val isFullyCollapsed: Boolean
        get() = (headerHeightPx > 0f) && (collapsedPx >= headerHeightPx)

    /**
     * Updates the scroll position tracking from the list state.
     * When at the top (index 0), [collapsedPx] tracks the item scroll offset,
     * and [translationY] remains `0f` so the header item scrolls naturally with the list.
     */
    fun updateScrollPosition(isAtTop: Boolean, firstVisibleItemScrollOffset: Int) {
        this.isAtTop = isAtTop
        if (isAtTop && collapsedPxState.floatValue < headerHeightPxState.floatValue) {
            val headerHeight = headerHeightPxState.floatValue
            collapsedPxState.floatValue = if (headerHeight > 0f) {
                firstVisibleItemScrollOffset.toFloat().coerceIn(0f, headerHeight)
            } else {
                0f
            }
        }
    }

    /**
     * Records the measured height of the sticky header, reported from a size observer such as
     * `Modifier.onSizeChanged`, and clamps the current collapse to the new height.
     *
     * @param heightPx the measured height of the header content, in pixels.
     */
    fun onHeaderSizeChanged(heightPx: Int) {
        headerHeightPxState.floatValue = heightPx.toFloat()
        if (collapsedPxState.floatValue > headerHeightPx) {
            collapsedPxState.floatValue = headerHeightPx
        }
    }

    /** Fully re-appears the header. Call this when the sticky behavior becomes disabled. */
    fun expand() {
        collapsedPxState.floatValue = 0f
    }

    /**
     * Applies a vertical scroll [deltaY] to the header and returns the portion consumed, in
     * the nested scroll delta convention (negative when scrolling down).
     *
     * Scrolling down (negative [deltaY]) collapses the header; scrolling up (positive
     * [deltaY]) reveals it with priority over the list scroll, matching the `enterAlways`
     * behavior. The collapse is clamped to `[0, headerHeightPx]`, so no delta is consumed
     * when the header is already fully collapsed or fully expanded in that direction.
     *
     * @param deltaY the available vertical scroll delta, in pixels.
     * @return the consumed delta with the same sign as [deltaY], or `0f` when nothing was
     * consumed.
     */
    internal fun consumeScrollDelta(deltaY: Float): Float {
        val headerHeight = headerHeightPxState.floatValue
        if (headerHeight <= 0f) return 0f
        val previous = collapsedPxState.floatValue
        val updated = (previous - deltaY).coerceIn(0f, headerHeight)
        collapsedPxState.floatValue = updated
        return previous - updated
    }

    /**
     * Animates the header to [targetPx], updating [collapsedPx] on every animation frame.
     *
     * @param targetPx the collapse target, in pixels.
     * @param animationSpec the animation driving the settle.
     */
    internal suspend fun settleTo(
        targetPx: Float,
        animationSpec: AnimationSpec<Float> = SettleAnimationSpec,
    ) {
        val target = targetPx.coerceIn(0f, headerHeightPxState.floatValue)
        val start = collapsedPxState.floatValue
        if (target == start) return
        animate(
            initialValue = start,
            targetValue = target,
            animationSpec = animationSpec,
        ) { value, _ ->
            collapsedPxState.floatValue = value
        }
    }

    companion object {
        /**
         * Selects the collapse position, in pixels, the header should settle to when a fling
         * ends, so that it is never left partially visible.
         *
         * @param collapsedPx the collapse position at the moment the fling ended, in pixels.
         * @param headerHeightPx the measured header height, in pixels.
         * @param flingVelocityY the leftover fling velocity, in pixels per second.
         * @return the target collapse position: reveal for upward flings, hide for downward
         * flings, and otherwise the nearest edge.
         */
        internal fun settleTargetPx(
            collapsedPx: Float,
            headerHeightPx: Float,
            flingVelocityY: Float,
        ): Float = when {
            headerHeightPx <= 0f -> 0f
            flingVelocityY > 0f -> 0f
            flingVelocityY < 0f -> headerHeightPx
            collapsedPx >= (headerHeightPx / 2f) -> headerHeightPx
            else -> 0f
        }
    }
}

/**
 * A [NestedScrollConnection] that makes a sticky header appear and disappear based on the
 * scroll direction of the list it belongs to. See [StickyHeaderScrollState] for usage.
 *
 * While enabled, scrolling down hides the header and scrolling up reveals it before the list
 * scrolls, matching the `enterAlways` behavior of Material 3 app bars. When the gesture
 * ends, the header settles to a fully visible or fully hidden position.
 *
 * @property state the scroll state shared with the header content.
 * @property isEnabled called before consuming any delta; typically backed by a user
 * preference such as whether the header is sticky at all.
 */
class StickyHeaderScrollConnection(
    private val state: StickyHeaderScrollState,
    private val isEnabled: () -> Boolean,
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (!isEnabled() || (available.y == 0f)) return Offset.Zero

        // When positioned at the top of the list (index 0), allow the list to scroll
        // naturally in BOTH directions without consuming pre-scroll.
        // updateScrollPosition will automatically keep collapsedPx in sync.
        if (state.isAtTop) {
            return Offset.Zero
        }

        val consumed = state.consumeScrollDelta(available.y)
        return if (consumed == 0f) Offset.Zero else Offset(0f, consumed)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (!isEnabled() || state.isAtTop) return Velocity.Zero
        val target = StickyHeaderScrollState.settleTargetPx(
            collapsedPx = state.collapsedPx,
            headerHeightPx = state.headerHeightPx,
            flingVelocityY = available.y,
        )
        if (target == state.collapsedPx) return Velocity.Zero
        state.settleTo(target)
        return Velocity(x = 0f, y = available.y)
    }
}

/**
 * Remembers a [StickyHeaderScrollState] whose hidden amount survives configuration changes.
 *
 * @return the remembered state instance.
 */
@Composable
fun rememberStickyHeaderScrollState(): StickyHeaderScrollState {
    val headerHeightPx = remember { mutableFloatStateOf(0f) }
    val collapsedPx = rememberSaveable { mutableFloatStateOf(0f) }
    return remember { StickyHeaderScrollState(headerHeightPx, collapsedPx) }
}

/**
 * Remembers a [StickyHeaderScrollConnection] bound to [state] that only consumes deltas
 * while [isEnabled] is `true`.
 *
 * @param state the scroll state shared with the header content.
 * @param isEnabled whether the appear/disappear behavior is active.
 * @return the remembered connection instance.
 */
@Composable
fun rememberStickyHeaderScrollConnection(
    state: StickyHeaderScrollState,
    isEnabled: Boolean,
): NestedScrollConnection {
    val isEnabledState = rememberUpdatedState(isEnabled)
    return remember(state) {
        StickyHeaderScrollConnection(state) { isEnabledState.value }
    }
}
