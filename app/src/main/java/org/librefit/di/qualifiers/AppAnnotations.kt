/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.di.qualifiers

/**
 * Type-safe Koin qualifier for the application-wide [kotlinx.coroutines.CoroutineScope]
 * (a SupervisorJob on Dispatchers.Default — see CoroutineScopeModule).
 */
object ApplicationScope
