/*
 * Copyright (c) 2025. LibreFit
 *
 * This file is part of LibreFit
 *
 * LibreFit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreFit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreFit.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.librefit.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.ThemeMode
import org.librefit.ui.screens.about.PrivacyScreen
import org.librefit.ui.theme.LibreFitTheme
import javax.inject.Inject

/**
 * Invoked if and only if the user taps the info icon next to a permission in the app system settings.
 * See `AndroidManifest.xml` and [this](https://developer.android.com/training/permissions/explaining-access#privacy-dashboard-show-rationale)
 */
@AndroidEntryPoint
class PrivacyActivity : ComponentActivity() {
    @Inject
    lateinit var userPreferences: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val theme = userPreferences.themeMode.collectAsState()
            val dynamicColor = userPreferences.materialMode.collectAsState()

            LibreFitTheme(
                dynamicColor = dynamicColor.value,
                darkTheme = when (theme.value) {
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                PrivacyScreen()
            }
        }
    }
}