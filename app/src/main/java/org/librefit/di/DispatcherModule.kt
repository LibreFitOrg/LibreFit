/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.librefit.di.qualifiers.DefaultDispatcher
import org.librefit.di.qualifiers.IoDispatcher
import org.librefit.di.qualifiers.MainDispatcher

/**
 * Provides the standard coroutine dispatchers under type-safe qualifiers
 * (see [org.librefit.di.qualifiers]).
 */
val dispatcherModule = module {
    // Type parameters are explicit on purpose: Koin's runtime registry resolves by EXACT
    // (type, qualifier) key, and `Dispatchers.Main`'s inferred type is the subtype
    // MainCoroutineDispatcher — without the explicit CoroutineDispatcher type the definition
    // would be registered under a key no consumer looks up (see KoinRuntimeResolutionTest).
    single<CoroutineDispatcher>(named<IoDispatcher>()) { Dispatchers.IO }
    single<CoroutineDispatcher>(named<DefaultDispatcher>()) { Dispatchers.Default }
    single<CoroutineDispatcher>(named<MainDispatcher>()) { Dispatchers.Main }
}