/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.health.HealthConnectSyncManager
import org.librefit.nav.NavigationHost
import org.librefit.nav.Route
import org.librefit.nav.deepLinkKeyForAction
import org.librefit.services.WorkoutServiceManager
import org.librefit.ui.theme.LibreFitTheme

class MainActivity : AppCompatActivity() {
    private val userPreferences: UserPreferencesRepository by inject()
    private val workoutServiceManager: WorkoutServiceManager by inject()

    /**
     * Deep-link key parsed once from the launch intent (cold start). Only used to seed the
     * initial Navigation 3 back stack; `rememberNavBackStack` ignores it whenever saved state
     * exists, so it is never re-applied after configuration changes or process death.
     */
    private var initialDeepLink: Route? = null

    /**
     * Deep-link key waiting to be applied, set by [onNewIntent] while the activity is already
     * running. Applied exactly once by [NavigationHost] and then cleared via
     * `onPendingDeepLinkConsumed`.
     */
    private val pendingDeepLink = mutableStateOf<Route?>(null)

    private val healthConnectSyncManager: HealthConnectSyncManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        initialDeepLink = intent.action.deepLinkKeyForAction()

        setContent {
            val theme by userPreferences.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by userPreferences.materialMode.collectAsStateWithLifecycle()

            LibreFitTheme(
                dynamicColor = dynamicColor,
                themeMode = theme,
            ) {
                NavigationHost(
                    initialDeepLink = initialDeepLink,
                    pendingDeepLink = pendingDeepLink.value,
                    onPendingDeepLinkConsumed = { pendingDeepLink.value = null },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Health Connect does not notify LibreFit when another app adds new data.
        lifecycleScope.launch { healthConnectSyncManager.syncEnabledData() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingDeepLink.value = intent.action.deepLinkKeyForAction()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            workoutServiceManager.stopService()
        }
    }
}
