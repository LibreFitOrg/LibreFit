/*
 * Copyright (c) 2024. LibreFit
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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.librefit.data.DataStoreManager
import org.librefit.data.ExerciseDeserializer
import org.librefit.enums.ThemeMode
import org.librefit.enums.WorkoutServiceActions
import org.librefit.nav.NavigationHost
import org.librefit.services.WorkoutService
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.ExerciseDC

class MainActivity : AppCompatActivity() {
    private lateinit var userPreferences: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        var keepSplashScreen = true
        splashscreen.setKeepOnScreenCondition { keepSplashScreen }
        lifecycleScope.launch {
            delay(1500)
            keepSplashScreen = false
        }

        enableEdgeToEdge()

        userPreferences = DataStoreManager(this)

        val list = mutableStateOf(emptyList<ExerciseDC>())

        lifecycleScope.launch {
            val loadedList = loadExercises(this@MainActivity)
            list.value = loadedList
        }

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
                    exerciseList = list.value,
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
        startForegroundService(workoutService)
        stopService(workoutService)
    }
}

private fun loadExercises(context: Context): List<ExerciseDC> {
    val inputStream = context.resources.openRawResource(R.raw.exercises)

    return inputStream.bufferedReader().use { reader ->
        val gson = GsonBuilder()
            .registerTypeAdapter(ExerciseDC::class.java, ExerciseDeserializer())
            .create()
        val listType = object : TypeToken<List<ExerciseDC>>() {}.type

        gson.fromJson(reader, listType)
    }
}