/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Replaces [Dispatchers.Main] with a [TestDispatcher] for the duration of a test.
 *
 * KMP-portable replacement for the former JUnit 4 `TestWatcher` rule: it carries no
 * framework-specific base class, so it compiles unchanged once tests move to a common
 * source set. Wire it up with kotlin-test lifecycle callbacks:
 *
 * ```
 * private val mainDispatcherRule = MainDispatcherRule()
 *
 * @BeforeTest
 * fun setUp() { mainDispatcherRule.setUp() }
 *
 * @AfterTest
 * fun tearDown() { mainDispatcherRule.tearDown() }
 * ```
 *
 * @property testDispatcher the dispatcher installed as [Dispatchers.Main] while the
 * test runs; reuse it (or its [TestDispatcher.scheduler]) to advance virtual time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) {

    /** Installs [testDispatcher] as [Dispatchers.Main]. */
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    /** Restores the original [Dispatchers.Main]. */
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
