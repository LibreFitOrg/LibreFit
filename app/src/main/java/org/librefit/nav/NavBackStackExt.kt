/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.nav

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Navigates forward by adding [key] to the top of this back stack, mirroring the Navigation 2
 * `NavController.navigate` options used across the app.
 *
 * @param key the navigation key to push onto the stack.
 * @param singleTop when `true` (default, mirrors `launchSingleTop = true`), the navigation is a
 * no-op if [key] is already the top of the stack.
 * @param popUpTo optional key whose last matching entry the stack is truncated to before [key]
 * is added (mirrors `popUpTo(route)`). Matching uses [equals], which holds for route data objects
 * and for data class keys constructed with the same arguments.
 * @param popUpToInclusive when `true`, the matching [popUpTo] entry is removed as well
 * (mirrors `popUpTo(route) { inclusive = true }`).
 * @return `true` if the back stack was modified (i.e., [key] was added).
 */
fun <T : NavKey> NavBackStack<T>.navigate(
    key: T,
    singleTop: Boolean = true,
    popUpTo: T? = null,
    popUpToInclusive: Boolean = false,
): Boolean {
    if (singleTop && (lastOrNull() == key)) return false

    if (popUpTo != null) {
        val index = lastIndexOf(popUpTo)
        if (index != -1) {
            val keep = if (popUpToInclusive) index else index + 1
            while (size > keep) removeAt(size - 1)
        }
    }

    add(key)
    return true
}

/**
 * Pops the top entry off this back stack, mirroring Navigation 2 `navigateUp()`.
 *
 * No-op when only the start destination remains, matching Nav2 behavior at the root of the
 * graph (back from there is handled by the system to exit the app).
 */
fun <T : NavKey> NavBackStack<T>.goBack() {
    if (size > 1) removeAt(size - 1)
}

/**
 * Returns a lifecycle-aware one-parameter navigation callback: [block] runs only while the
 * current [NavEntry][androidx.navigation3.runtime.NavEntry]'s lifecycle is at least
 * [Lifecycle.State.RESUMED].
 *
 * Wrapping click- and dialog-triggered navigation callbacks prevents double navigation from
 * rapid taps: as soon as a navigation starts, the entry leaves `RESUMED` and any further
 * invocations are silently dropped. This mirrors [androidx.lifecycle.compose.dropUnlessResumed]
 * for callbacks that carry navigation arguments.
 *
 * Do **not** use for navigation triggered automatically from background state (e.g. from a
 * coroutine observing a flow), as those invocations would be silently dropped.
 *
 * @param block the navigation action, receiving the navigation argument.
 * @return a decorated function invoking [block] only if the lifecycle state is at least
 * [Lifecycle.State.RESUMED].
 */
@CheckResult
@Composable
fun <P> dropUnlessResumedNav(block: (P) -> Unit): (P) -> Unit {
    val lifecycleOwner = LocalLifecycleOwner.current
    return { arg ->
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            block(arg)
        }
    }
}

/**
 * Returns a lifecycle-aware two-parameter navigation callback: [block] runs only while the
 * current [NavEntry][androidx.navigation3.runtime.NavEntry]'s lifecycle is at least
 * [Lifecycle.State.RESUMED].
 *
 * Wrapping click- and dialog-triggered navigation callbacks prevents double navigation from
 * rapid taps: as soon as a navigation starts, the entry leaves `RESUMED` and any further
 * invocations are silently dropped. This mirrors [androidx.lifecycle.compose.dropUnlessResumed]
 * for callbacks that carry navigation arguments.
 *
 * Do **not** use for navigation triggered automatically from background state (e.g. from a
 * coroutine observing a flow), as those invocations would be silently dropped.
 *
 * @param block the navigation action, receiving the navigation arguments.
 * @return a decorated function invoking [block] only if the lifecycle state is at least
 * [Lifecycle.State.RESUMED].
 */
@CheckResult
@Composable
fun <P1, P2> dropUnlessResumedNav(block: (P1, P2) -> Unit): (P1, P2) -> Unit {
    val lifecycleOwner = LocalLifecycleOwner.current
    return { arg1, arg2 ->
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            block(arg1, arg2)
        }
    }
}
