/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.android.ext.android.inject
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.ui.screens.about.PrivacyScreen
import org.librefit.ui.theme.LibreFitTheme

/**
 * Invoked if and only if the user taps the info icon next to a permission in the app system settings.
 * See `AndroidManifest.xml` and [this](https://developer.android.com/training/permissions/explaining-access#privacy-dashboard-show-rationale)
 */
class PrivacyActivity : ComponentActivity() {
    private val userPreferences: UserPreferencesRepository by inject()

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
                PrivacyScreen()
            }
        }
    }
}