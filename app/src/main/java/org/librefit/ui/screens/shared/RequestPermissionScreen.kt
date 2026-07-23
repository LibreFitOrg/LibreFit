/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.shared

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.nav.Route
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.PreferencesLottie
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.theme.LibreFitTheme
import kotlin.random.Random

@Composable
fun RequestPermissionScreen(
    navController: NavHostController,
    workoutId: Long,
    requestPermissionNextTime: Boolean,
    saveRequestPermissionAgainPreference: (Boolean) -> Unit
) {
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var showSettingsDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        ConfirmDialog(
            title = stringResource(R.string.notifications_permission),
            text = stringResource(R.string.notifications_permission_permanently_denied_desc),
            confirmText = stringResource(R.string.open_settings),
            onConfirm = {
                showSettingsDialog = false
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    // This is the standard way to target app settings
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            },
            onDismiss = {
                showSettingsDialog = false
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (!isGranted) {
            val activity = context as? Activity
            if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                if (!showRationale) {
                    // If it's denied and shouldShowRequestPermissionRationale is false,
                    // it means the user checked "Don't ask again" or it is permanently blocked.
                    showSettingsDialog = true
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasNotificationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val handleNotificationPermissionRequest = {
        val activity = context as? Activity
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
            val checkPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (checkPermission == PackageManager.PERMISSION_DENIED && !showRationale) {
                // We are in a permanently denied state (or pre-request state)
                // To be safe, we attempt to launch the launcher. If the launcher returns
                // immediately with false, we then trigger the Settings Redirect Dialog.
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    RequestPermissionsScreenContent(
        navController = navController,
        requestPermissionNextTime = requestPermissionNextTime,
        hasNotificationPermission = hasNotificationPermission,
        handleNotificationPermissionRequest = handleNotificationPermissionRequest,
        saveRequestPermissionAgainPreference = saveRequestPermissionAgainPreference,
        navigateToWorkoutScreen = {
            navController.navigate(Route.WorkoutScreen(workoutId = workoutId)) {
                launchSingleTop = true
                popUpTo(Route.RequestPermissionScreen(workoutId = workoutId)) { inclusive = true }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RequestPermissionsScreenContent(
    navController: NavHostController,
    requestPermissionNextTime: Boolean,
    hasNotificationPermission: Boolean,
    handleNotificationPermissionRequest: () -> Unit,
    saveRequestPermissionAgainPreference: (Boolean) -> Unit,
    navigateToWorkoutScreen: () -> Unit
) {

    LibreFitScaffold(
        navigateBack = navController::navigateUp
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding, 30.dp) {
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
                ElevatedCard(
                    shape = MaterialTheme.shapes.extraLargeIncreased
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(30.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.best_experience_permissions),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
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
                                enabled = requestPermissionNextTime,
                                modifier = Modifier
                                    .padding(start = 10.dp),
                                checked = hasNotificationPermission,
                                onCheckedChange = { handleNotificationPermissionRequest() }
                            )
                        }
                    }
                }
            }


            item {
                Text(
                    text = stringResource(R.string.app_works_without_permission),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }


            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dont_ask_again),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.width(10.dp))
                    Checkbox(
                        enabled = !hasNotificationPermission,
                        checked = !requestPermissionNextTime,
                        onCheckedChange = {
                            saveRequestPermissionAgainPreference(!it)
                        }
                    )
                }
            }

            item {
                val interactionSources = remember { List(2) { MutableInteractionSource() } }
                ButtonGroup(
                    overflowIndicator = {}
                ) {
                    customItem(
                        buttonGroupContent = {
                            TextButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .animateWidth(interactionSources[0]),
                                enabled = !hasNotificationPermission && requestPermissionNextTime,
                                interactionSource = interactionSources[0],
                                onClick = {
                                    saveRequestPermissionAgainPreference(true)
                                    navigateToWorkoutScreen()
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.skip_for_now),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        },
                        menuContent = {}
                    )
                    customItem(
                        buttonGroupContent = {
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .animateWidth(interactionSources[1]),
                                enabled = hasNotificationPermission || !requestPermissionNextTime,
                                interactionSource = interactionSources[1],
                                onClick = {
                                    navigateToWorkoutScreen()
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.label_continue),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        },
                        menuContent = {}
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun RequestPermissionsScreenPreview() {
    val hasNotificationPermission = remember { mutableStateOf(false) }
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        RequestPermissionsScreenContent(
            navController = rememberNavController(),
            requestPermissionNextTime = Random.nextBoolean(),
            hasNotificationPermission = hasNotificationPermission.value,
            handleNotificationPermissionRequest = {
                hasNotificationPermission.value = !hasNotificationPermission.value
            },
            saveRequestPermissionAgainPreference = {},
            navigateToWorkoutScreen = {}
        )
    }
}