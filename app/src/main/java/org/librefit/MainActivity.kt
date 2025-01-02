/*
 * Copyright (c) 2024-2025. LibreFit
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

package org.librefit

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.librefit.data.DataStoreManager
import org.librefit.enums.ThemeMode
import org.librefit.enums.WorkoutServiceActions
import org.librefit.nav.NavigationHost
import org.librefit.services.WorkoutService
import org.librefit.ui.theme.LibreFitTheme

class MainActivity : AppCompatActivity() {
    private lateinit var userPreferences: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        userPreferences = DataStoreManager(this)

        setContent {
            val theme = userPreferences.themeMode.collectAsState(ThemeMode.SYSTEM)
            val dynamicColor = userPreferences.materialMode.collectAsState(false)

            LibreFitTheme(
                dynamicColor = dynamicColor.value,
                darkTheme = when (theme.value) {
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                NavigationHost(
                    userPreferences = userPreferences
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val workoutService = Intent(this, WorkoutService::class.java).apply {
            action = WorkoutServiceActions.STOP_SERVICE.string
        }
        startService(workoutService)
    }
}