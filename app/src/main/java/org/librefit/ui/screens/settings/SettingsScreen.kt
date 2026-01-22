/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.settings

import android.os.Build
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.userPreferences.DialogPreference
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.nav.Route
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.dialogs.PreferenceDialog
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter
import kotlin.random.Random

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SettingsScreen(
    navController: NavHostController
) {

    val viewModel: SettingsScreenViewModel = hiltViewModel()


    val selectedLanguage by viewModel.language.collectAsStateWithLifecycle()

    val selectedTheme by viewModel.themeMode.collectAsStateWithLifecycle()

    val keepWorkoutScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()

    val materialModeOn by viewModel.materialMode.collectAsStateWithLifecycle()

    val restTimerSoundOn by viewModel.restTimerSoundOn.collectAsStateWithLifecycle()

    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    val currentPreference by viewModel.currentPreference.collectAsStateWithLifecycle()

    val isSupporter by viewModel.isSupporter.collectAsStateWithLifecycle()

    val isWorkoutHeaderSticky by viewModel.isWorkoutHeaderSticky.collectAsStateWithLifecycle()

    preferences?.let {
        PreferenceDialog(
            currentPreference = currentPreference,
            preferences = it,
            updatePreference = viewModel::updatePreference,
        ) {
            viewModel.updatePreferences(null)
        }

    }

    SettingsScreenContent(
        navController = navController,
        selectedTheme = selectedTheme,
        materialModeOn = materialModeOn,
        selectedLanguage = selectedLanguage,
        keepWorkoutScreenOn = keepWorkoutScreenOn,
        restTimerSoundOn = restTimerSoundOn,
        isSupporter = isSupporter,
        isWorkoutHeaderSticky = isWorkoutHeaderSticky,
        updatePreferences = viewModel::updatePreferences,
        saveBooleanValue = viewModel::savePreference
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
    isWorkoutHeaderSticky: Boolean,
    updatePreferences: (List<DialogPreference>) -> Unit,
    saveBooleanValue: (Preferences.Key<Boolean>, value: Boolean) -> Unit
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
                                saveBooleanValue(
                                    UserPreferencesRepository.materialModeKey,
                                    !materialModeOn
                                )
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
                    onClick = {
                        saveBooleanValue(
                            UserPreferencesRepository.keepOnWorkoutScreenKey,
                            !keepWorkoutScreenOn
                        )
                    },
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
                    onClick = {
                        saveBooleanValue(
                            UserPreferencesRepository.restTimerSoundKey,
                            !restTimerSoundOn
                        )
                    },
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
                    onClick = {
                        saveBooleanValue(
                            UserPreferencesRepository.isWorkoutHeaderStickyKey,
                            !isWorkoutHeaderSticky
                        )
                    },
                    icon = painterResource(R.drawable.ic_sticker),
                    settingDesc = stringResource(if (isWorkoutHeaderSticky) R.string.stick_status_bar_desc else R.string.not_stick_status_bar_desc),
                    settingName = stringResource(R.string.stick_status_bar)
                )
            }
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
    isChecked: Boolean? = null
) {
    val haptic = LocalHapticFeedback.current

    Button(
        modifier = Modifier.animateContentSize(),
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
            Icon(
                painter = icon,
                contentDescription = stringResource(R.string.theme),
                modifier = Modifier.padding(start = 5.dp, end = 20.dp)
            )
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
            isWorkoutHeaderSticky = isWorkoutHeaderSticky,
            saveBooleanValue = { key, value ->
                when (key) {
                    UserPreferencesRepository.materialModeKey -> {
                        materialModeOn = value
                    }

                    UserPreferencesRepository.keepOnWorkoutScreenKey -> {
                        keepWorkoutScreenOn = value
                    }

                    UserPreferencesRepository.restTimerSoundKey -> {
                        restTimerSoundOn = value
                    }
                }
            },
        )
    }
}