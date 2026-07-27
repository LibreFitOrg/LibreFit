/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.health.HealthConnectSyncManager
import org.librefit.nav.NavigationHost
import org.librefit.services.WorkoutServiceManager
import org.librefit.ui.theme.LibreFitTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var userPreferences: UserPreferencesRepository

    @Inject
    lateinit var workoutServiceManager: WorkoutServiceManager

    @Inject
    lateinit var healthConnectSyncManager: HealthConnectSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()


        setContent {
            val theme by userPreferences.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by userPreferences.materialMode.collectAsStateWithLifecycle()

            LibreFitTheme(
                dynamicColor = dynamicColor,
                themeMode = theme
            ) {
                NavigationHost()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Health Connect does not notify LibreFit when another app adds new data.
        lifecycleScope.launch { healthConnectSyncManager.syncEnabledData() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            workoutServiceManager.stopService()
        }
    }
}
