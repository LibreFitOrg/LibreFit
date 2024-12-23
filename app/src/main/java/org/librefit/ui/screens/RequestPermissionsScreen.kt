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

package org.librefit.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.data.DataStoreManager
import org.librefit.nav.checkPermissionsBeforeNavigateToWorkout
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.animations.PreferencesLottie

@SuppressLint("BatteryLife")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissionsScreen(
    userPreferences: DataStoreManager,
    workoutId: Int = 0,
    workoutTitle: String,
    navController: NavHostController
) {

    val askPermissionAgain = userPreferences.requestPermissionsAgain.collectAsState(initial = true)

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        //Permission granted by default below Tiramisu
        null
    }

    val context = LocalContext.current


    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager


    /**
     * A flow that continuously emits the current state of [android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS].
     * The [checkPermissionsBeforeNavigateToWorkout] method checks for permission
     * using [PowerManager.isIgnoringBatteryOptimizations]. Any approach better than this one are welcome
     */
    val isIgnoringBatteryOptimization = remember {
        flow {
            while (true) {
                emit(pm.isIgnoringBatteryOptimizations(context.packageName))
                delay(1000)
            }
        }
    }.collectAsState(initial = pm.isIgnoringBatteryOptimizations(context.packageName))

    val coroutineScope = rememberCoroutineScope()

    CustomScaffold(
        title = "",
        navigateBack = { navController.popBackStack() }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PreferencesLottie()
                    Text(
                        text = stringResource(R.string.before_starting_the_workout),
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.full_experience_permissions),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                    ) {
                        Text(
                            text = stringResource(R.string.notifications_permission),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.notifications_permission_desc),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Checkbox(
                        modifier = Modifier
                            .weight(0.1f)
                            .padding(start = 10.dp),
                        checked = notificationPermissionState?.status?.isGranted != false,
                        onCheckedChange = {
                            notificationPermissionState?.launchPermissionRequest()
                        }
                    )
                }
            }




            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                    ) {
                        Text(
                            text = stringResource(R.string.background_usage),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.background_usage_desc),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Checkbox(
                        modifier = Modifier
                            .weight(0.1f)
                            .padding(start = 10.dp),
                        checked = isIgnoringBatteryOptimization.value,
                        onCheckedChange = {
                            if (!isIgnoringBatteryOptimization.value) {
                                val intent =
                                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                intent.data = Uri.parse("package:${context.packageName}")
                                context.startActivity(intent)
                            }
                        }
                    )

                }
            }


            item {
                Text(
                    text = stringResource(R.string.app_works_without_permissions_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }


            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        enabled = !(notificationPermissionState?.status?.isGranted != false
                                && isIgnoringBatteryOptimization.value),
                        checked = !askPermissionAgain.value,
                        onCheckedChange = {
                            coroutineScope.launch {
                                userPreferences.savePreference(
                                    key = userPreferences.requestPermissionsAgainKey,
                                    value = !it
                                )
                            }
                        }
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.dont_ask_again),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                userPreferences.savePreference(
                                    key = userPreferences.requestPermissionsAgainKey,
                                    value = true
                                )
                            }
                            checkPermissionsBeforeNavigateToWorkout(
                                workoutId = workoutId,
                                title = workoutTitle,
                                navController = navController,
                                appContext = context.applicationContext
                            )
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.skip_for_now),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    TextButton(
                        enabled = ((notificationPermissionState?.status?.isGranted != false) &&
                                isIgnoringBatteryOptimization.value) || !askPermissionAgain.value,
                        colors = ButtonDefaults.buttonColors(),
                        onClick = {
                            checkPermissionsBeforeNavigateToWorkout(
                                workoutId = workoutId,
                                title = workoutTitle,
                                navController = navController,
                                appContext = context.applicationContext
                            )
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.label_continue),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun RequestPermissionsScreenPreview() {
    RequestPermissionsScreen(
        DataStoreManager(LocalContext.current),
        workoutTitle = "",
        navController = rememberNavController()
    )
}