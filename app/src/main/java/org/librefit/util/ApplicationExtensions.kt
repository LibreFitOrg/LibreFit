/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.util

import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * Emits the updated [Configuration] whenever an application configuration change occurs
 * (e.g., orientation, language/locale, or night mode updates).
 */
fun Application.configurationChanges(): Flow<Configuration> = callbackFlow {
    val callback = object : ComponentCallbacks {
        override fun onConfigurationChanged(newConfig: Configuration) {
            trySend(newConfig)
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun onLowMemory() {
            // Unused; required by ComponentCallbacks contract on API < 35
        }
    }

    registerComponentCallbacks(callback)

    awaitClose {
        unregisterComponentCallbacks(callback)
    }
}.conflate()
