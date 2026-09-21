/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.librefit.di.qualifiers.ApplicationScope

/**
 * Provides the application-wide [CoroutineScope] under a type-safe qualifier
 * (see [ApplicationScope]).
 */
val coroutineScopeModule = module {
    single(named<ApplicationScope>()) {
        // SupervisorJob means if one child coroutine fails, the others are not cancelled.
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
