/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.settings

import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.enums.healthConnect.HealthConnectSyncOption
import org.librefit.enums.userPreferences.DialogPreference
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.nav.Route
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.dialogs.ConfirmDialog
import org.librefit.ui.components.dialogs.PreferenceDialog
import org.librefit.ui.models.HealthConnectState
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import kotlin.random.Random

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsScreenViewModel = hiltViewModel()
) {


    val selectedLanguage by viewModel.language.collectAsStateWithLifecycle()

    val selectedTheme by viewModel.themeMode.collectAsStateWithLifecycle()

    val keepWorkoutScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()

    val materialModeOn by viewModel.materialMode.collectAsStateWithLifecycle()

    val restTimerSoundOn by viewModel.restTimerSoundOn.collectAsStateWithLifecycle()

    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    val currentPreference by viewModel.currentPreference.collectAsStateWithLifecycle()

    val isSupporter by viewModel.isSupporter.collectAsStateWithLifecycle()

    val isWorkoutHeaderSticky by viewModel.isWorkoutHeaderSticky.collectAsStateWithLifecycle()

    val useScrollWheelForInput by viewModel.useScrollWheelForInput.collectAsStateWithLifecycle()

    val dismissScrollWheelInputAutomatically by viewModel.dismissScrollWheelInputAutomatically.collectAsStateWithLifecycle()

    val healthConnectState by viewModel.healthConnectState.collectAsStateWithLifecycle()
    var pendingHealthConnectOption by remember { mutableStateOf<HealthConnectSyncOption?>(null) }
    var pendingHealthConnectPermissions by remember { mutableStateOf<Set<String>?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshHealthConnectState()
    }

    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        viewModel.updateHealthConnectPermissions(pendingHealthConnectOption, grantedPermissions)
        pendingHealthConnectOption = null
    }

    preferences?.let {
        PreferenceDialog(
            currentPreference = currentPreference,
            preferences = it,
            updatePreference = viewModel::updateDialogPreference,
        ) {
            viewModel.updatePreferences(null)
        }

    }

    pendingHealthConnectPermissions?.let { permissions ->
        ConfirmDialog(
            title = stringResource(R.string.health_connect_permission_title),
            text = stringResource(
                if (pendingHealthConnectOption == null) {
                    R.string.health_connect_all_permissions_rationale
                } else {
                    R.string.health_connect_permission_rationale
                }
            ),
            confirmText = stringResource(R.string.health_connect_continue),
            onConfirm = {
                pendingHealthConnectPermissions = null
                healthConnectPermissionLauncher.launch(permissions)
            },
            onDismiss = {
                pendingHealthConnectPermissions = null
                pendingHealthConnectOption = null
            }
        )
    }

    SettingsScreenContent(
        navController = navController,
        selectedTheme = selectedTheme,
        materialModeOn = materialModeOn,
        selectedLanguage = selectedLanguage,
        keepWorkoutScreenOn = keepWorkoutScreenOn,
        restTimerSoundOn = restTimerSoundOn,
        isSupporter = isSupporter,
        healthConnectState = healthConnectState,
        useScrollWheelForInput = useScrollWheelForInput,
        isWorkoutHeaderSticky = isWorkoutHeaderSticky,
        dismissScrollWheelInputAutomatically = dismissScrollWheelInputAutomatically,
        updatePreferences = viewModel::updatePreferences,
        onMaterialModeChange = viewModel::saveMaterialMode,
        onKeepWorkoutScreenOnChange = viewModel::saveWorkoutScreenOn,
        onRestTimerSoundOnChange = viewModel::saveRestTimerSoundOn,
        onIsWorkoutHeaderStickyChange = viewModel::saveIsWorkoutHeaderSticky,
        onUseScrollWheelForInputChange = viewModel::saveUseScrollWheelForInput,
        onHealthConnectClick = {
            if (healthConnectState.isEnabled) {
                viewModel.updateHealthConnectEnabled(false)
            } else if (
                healthConnectState.grantedPermissions.containsAll(
                    viewModel.allHealthConnectPermissions
                )
            ) {
                viewModel.updateHealthConnectEnabled(true)
            } else {
                pendingHealthConnectOption = null
                pendingHealthConnectPermissions = viewModel.allHealthConnectPermissions
            }
        },
        onHealthConnectOptionClick = { option ->
            if (option in healthConnectState.enabledOptions) {
                viewModel.updateHealthConnectSyncOption(option, false)
            } else {
                val permissions = viewModel.permissionsFor(option)
                if (healthConnectState.grantedPermissions.containsAll(permissions)) {
                    viewModel.updateHealthConnectSyncOption(option, true)
                } else {
                    pendingHealthConnectOption = option
                    pendingHealthConnectPermissions = permissions
                }
            }
        },
        onManageHealthConnectPermissions = {
            navController.context.startActivity(
                Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
            )
        },
        onDismissScrollWhellInputAutomaticallyChange = viewModel::saveDismissScrollWheelInputAutomatically
    )
}


@Composable
private fun SettingsScreenContent(
    navController: NavHostController,
    selectedTheme: ThemeMode,
    materialModeOn: Boolean,
    selectedLanguage: Language,
    keepWorkoutScreenOn: Boolean,
    restTimerSoundOn: Boolean,
    isSupporter: Boolean,
    healthConnectState: HealthConnectState,
    isWorkoutHeaderSticky: Boolean,
    useScrollWheelForInput: Boolean,
    dismissScrollWheelInputAutomatically: Boolean,
    updatePreferences: (List<DialogPreference>) -> Unit,
    onMaterialModeChange: (Boolean) -> Unit,
    onKeepWorkoutScreenOnChange: (Boolean) -> Unit,
    onRestTimerSoundOnChange: (Boolean) -> Unit,
    onIsWorkoutHeaderStickyChange: (Boolean) -> Unit,
    onUseScrollWheelForInputChange: (Boolean) -> Unit,
    onHealthConnectClick: () -> Unit,
    onHealthConnectOptionClick: (HealthConnectSyncOption) -> Unit,
    onManageHealthConnectPermissions: () -> Unit,
    onDismissScrollWhellInputAutomaticallyChange: (Boolean) -> Unit,
) {
    LibreFitScaffold(
        title = AnnotatedString(stringResource(id = R.string.settings)),
        navigateBack = navController::navigateUp
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding) {
            item { HeadlineText(text = stringResource(id = R.string.appearance)) }

            item {
                SettingItem(
                    onClick = { updatePreferences(ThemeMode.entries) },
                    icon = painterResource(R.drawable.ic_dark_mode),
                    settingName = stringResource(id = R.string.theme),
                    settingDesc = stringResource(
                        id = Formatter.preferenceToStringId(selectedTheme)
                    )
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    SettingItem(
                        onClick = {
                            if (isSupporter) {
                                onMaterialModeChange(!materialModeOn)
                            } else {
                                navController.navigate(Route.SupportScreen(true)) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = painterResource(R.drawable.ic_material),
                        settingName = stringResource(id = R.string.material_you),
                        settingDesc = stringResource(
                            id = if (materialModeOn) R.string.dynamic_color_enabled else R.string.dynamic_color_disabled
                        ),
                        isChecked = materialModeOn
                    )
                }
            }


            item { HeadlineText(text = stringResource(id = R.string.settings_general)) }

            item {
                SettingItem(
                    onClick = { updatePreferences(Language.entries) },
                    icon = painterResource(R.drawable.ic_translate),
                    settingName = stringResource(id = R.string.language),
                    settingDesc = stringResource(
                        id = Formatter.preferenceToStringId(selectedLanguage)
                    )
                )
            }

            item {
                SettingItem(
                    onClick = { onKeepWorkoutScreenOnChange(!keepWorkoutScreenOn) },
                    icon = painterResource(R.drawable.ic_keep),
                    settingName = stringResource(id = R.string.keep_screen_on),
                    settingDesc = stringResource(
                        id = if (keepWorkoutScreenOn) R.string.screen_on_desc else R.string.screen_off_desc
                    ),
                    isChecked = keepWorkoutScreenOn
                )
            }

            item {
                SettingItem(
                    onClick = { onRestTimerSoundOnChange(!restTimerSoundOn) },
                    icon = painterResource(R.drawable.ic_notification_sound),
                    settingName = stringResource(id = R.string.rest_timer_sound),
                    settingDesc = stringResource(
                        id = if (restTimerSoundOn) R.string.rest_timer_sound_on_desc else R.string.rest_timer_sound_off_desc
                    ),
                    isChecked = restTimerSoundOn
                )
            }

            item {
                SettingItem(
                    isChecked = isWorkoutHeaderSticky,
                    onClick = { onIsWorkoutHeaderStickyChange(!isWorkoutHeaderSticky) },
                    icon = painterResource(R.drawable.ic_sticker),
                    settingDesc = stringResource(if (isWorkoutHeaderSticky) R.string.stick_status_bar_desc else R.string.not_stick_status_bar_desc),
                    settingName = stringResource(R.string.stick_status_bar)
                )
            }

            item {
                SettingItem(
                    isChecked = useScrollWheelForInput,
                    onClick = { onUseScrollWheelForInputChange(!useScrollWheelForInput) },
                    icon = painterResource(R.drawable.ic_scroll_vertical),
                    settingDesc = stringResource(if (useScrollWheelForInput) R.string.use_scroll_wheel_for_input_desc else R.string.not_use_scroll_wheel_for_input_desc),
                    settingName = stringResource(R.string.use_scroll_wheel_for_input)
                )
            }


            item {
                AnimatedVisibility(
                    visible = useScrollWheelForInput,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SettingItem(
                        isChecked = dismissScrollWheelInputAutomatically,
                        onClick = { onDismissScrollWhellInputAutomaticallyChange(!dismissScrollWheelInputAutomatically) },
                        icon = painterResource(R.drawable.ic_bottom_panel_close),
                        settingDesc = stringResource(if (dismissScrollWheelInputAutomatically) R.string.dismiss_scroll_wheel_automatically_desc else R.string.dismiss_scroll_wheel_manually_desc),
                        settingName = stringResource(R.string.dismiss_scroll_wheel_automatically)
                    )
                }
            }

            item { HeadlineText(text = stringResource(id = R.string.health_connect)) }

            item {
                SettingItem(
                    enabled = healthConnectState.isAvailable,
                    onClick = onHealthConnectClick,
                    icon = painterResource(R.drawable.ic_monitor_weight),
                    showIconInOriginalColors = true,
                    settingName = stringResource(id = R.string.health_connect),
                    settingDesc = when {
                        !healthConnectState.isAvailable -> stringResource(R.string.health_connect_unavailable_desc)
                        else -> stringResource(R.string.health_connect_permissions_desc)
                    },
                    isChecked = healthConnectState.isEnabled
                )
            }

            item {
                AnimatedVisibility(
                    visible = healthConnectState.isEnabled && healthConnectState.isAvailable,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HealthConnectDataTypeCard(
                            name = stringResource(R.string.body_weight),
                            icon = painterResource(R.drawable.ic_monitor_weight),
                            readOption = HealthConnectSyncOption.WEIGHT_READ,
                            writeOption = HealthConnectSyncOption.WEIGHT_WRITE,
                            state = healthConnectState,
                            onToggle = onHealthConnectOptionClick
                        )
                        HealthConnectDataTypeCard(
                            name = stringResource(R.string.fat_mass),
                            icon = painterResource(R.drawable.ic_monitor_weight),
                            readOption = HealthConnectSyncOption.FAT_READ,
                            writeOption = HealthConnectSyncOption.FAT_WRITE,
                            state = healthConnectState,
                            onToggle = onHealthConnectOptionClick
                        )
                        HealthConnectDataTypeCard(
                            name = stringResource(R.string.health_connect_workouts),
                            icon = painterResource(R.drawable.ic_timer),
                            readOption = null,
                            writeOption = HealthConnectSyncOption.WORKOUT_WRITE,
                            state = healthConnectState,
                            onToggle = onHealthConnectOptionClick
                        )
                    }
                }
            }

            if (healthConnectState.isAvailable) {
                item {
                    SettingItem(
                        onClick = onManageHealthConnectPermissions,
                        icon = painterResource(R.drawable.ic_settings),
                        settingName = stringResource(R.string.health_connect_manage_access),
                        settingDesc = stringResource(R.string.health_connect_manage_access_desc)
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthConnectDataTypeCard(
    name: String,
    icon: Painter,
    readOption: HealthConnectSyncOption?,
    writeOption: HealthConnectSyncOption,
    state: HealthConnectState,
    onToggle: (HealthConnectSyncOption) -> Unit
) {
    Surface(
        shape = ButtonDefaults.shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ButtonDefaults.ContentPadding)
                .padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.padding(start = 5.dp, end = 20.dp)
            )
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            HealthConnectAccessSwitch(
                label = stringResource(R.string.health_connect_read_short),
                option = readOption,
                state = state,
                onToggle = onToggle
            )
            HealthConnectAccessSwitch(
                label = stringResource(R.string.health_connect_write_short),
                option = writeOption,
                state = state,
                onToggle = onToggle
            )
        }
    }
}

@Composable
private fun HealthConnectAccessSwitch(
    label: String,
    option: HealthConnectSyncOption?,
    state: HealthConnectState,
    onToggle: (HealthConnectSyncOption) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (option != null) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Switch(
                checked = option in state.enabledOptions,
                onCheckedChange = { isChecked ->
                    haptic.performHapticFeedback(
                        if (isChecked) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                    )
                    onToggle(option)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingItem(
    onClick: () -> Unit,
    icon: Painter,
    settingName: String,
    settingDesc: String,
    enabled: Boolean = true,
    showIconInOriginalColors: Boolean = false,
    isChecked: Boolean? = null
) {
    val haptic = LocalHapticFeedback.current

    Button(
        modifier = Modifier.animateContentSize(),
        enabled = enabled,
        onClick = {
            haptic.performHapticFeedback(
                hapticFeedbackType = isChecked?.let {
                    if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                } ?: HapticFeedbackType.ContextClick
            )
            onClick()
        },
        shapes = ButtonDefaults.shapes(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Row(
            modifier = Modifier
                .padding(end = 10.dp)
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showIconInOriginalColors) {
                Image(
                    painter = painterResource(R.drawable.health_connect_logo),
                    contentDescription = settingName,
                    modifier = Modifier.padding(start = 5.dp, end = 20.dp).size(28.dp)
                )
            } else {
                Icon(
                    painter = icon,
                    contentDescription = settingName,
                    modifier = Modifier.padding(start = 5.dp, end = 20.dp)
                )
            }
            Column {
                Text(
                    text = settingName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = settingDesc,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        isChecked?.let {
            Switch(
                checked = it,
                enabled = enabled,
                onCheckedChange = null
            )
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun SettingsScreenPreview() {
    var materialModeOn by remember { mutableStateOf(Random.nextBoolean()) }
    var keepWorkoutScreenOn by remember { mutableStateOf(Random.nextBoolean()) }
    var restTimerSoundOn by remember { mutableStateOf(Random.nextBoolean()) }
    var isWorkoutHeaderSticky by remember { mutableStateOf(Random.nextBoolean()) }
    var useScrollWheelForInput by remember { mutableStateOf(Random.nextBoolean()) }

    val theme = ThemeMode.entries.random()

    LibreFitTheme(dynamicColor = materialModeOn, themeMode = theme) {
        SettingsScreenContent(
            navController = rememberNavController(),
            selectedTheme = theme,
            materialModeOn = materialModeOn,
            selectedLanguage = Language.SYSTEM,
            keepWorkoutScreenOn = keepWorkoutScreenOn,
            restTimerSoundOn = restTimerSoundOn,
            updatePreferences = {},
            isSupporter = Random.nextBoolean(),
            healthConnectState = HealthConnectState(isAvailable = true),
            isWorkoutHeaderSticky = isWorkoutHeaderSticky,
            useScrollWheelForInput = useScrollWheelForInput,
            dismissScrollWheelInputAutomatically = Random.nextBoolean(),
            onMaterialModeChange = { materialModeOn = it },
            onKeepWorkoutScreenOnChange = { keepWorkoutScreenOn = it },
            onRestTimerSoundOnChange = { restTimerSoundOn = it },
            onIsWorkoutHeaderStickyChange = { isWorkoutHeaderSticky = it },
            onUseScrollWheelForInputChange = { useScrollWheelForInput = it },
            onHealthConnectClick = {},
            onHealthConnectOptionClick = {},
            onManageHealthConnectPermissions = {},
            onDismissScrollWhellInputAutomaticallyChange = {}
        )
    }
}
