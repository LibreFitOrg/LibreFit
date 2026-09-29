/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.di.qualifiers

/**
 * Type-safe Koin qualifier for the IO coroutine dispatcher
 * (see [kotlinx.coroutines.Dispatchers.IO]).
 */
object IoDispatcher

/**
 * Type-safe Koin qualifier for the default (CPU-bound) coroutine dispatcher
 * (see [kotlinx.coroutines.Dispatchers.Default]).
 */
object DefaultDispatcher

/**
 * Type-safe Koin qualifier for the main (UI) coroutine dispatcher
 * (see [kotlinx.coroutines.Dispatchers.Main]).
 */
object MainDispatcher
