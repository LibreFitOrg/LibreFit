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

package org.librefit.ui.screens.settings

import android.os.Build
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.db.repository.UserPreferencesRepository
import org.librefit.enums.userPreferences.DialogPreference
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
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


    val selectedLanguage by viewModel.language.collectAsState()

    val selectedTheme by viewModel.themeMode.collectAsState()

    val keepWorkoutScreenOn by viewModel.keepScreenOn.collectAsState()

    val materialModeOn by viewModel.materialMode.collectAsState()

    val restTimerSoundOn by viewModel.restTimerSoundOn.collectAsState()

    val preferences by viewModel.preferences.collectAsState()

    val currentPreference by viewModel.currentPreference.collectAsState()

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
    updatePreferences: (List<DialogPreference>) -> Unit,
    saveBooleanValue: (Preferences.Key<Boolean>, value: Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val iconPaddingModifier = Modifier.padding(start = 15.dp, end = 25.dp)

    val preferencesPadding = 10.dp

    LibreFitScaffold(
        title = AnnotatedString(stringResource(id = R.string.settings)),
        navigateBack = navController::navigateUp
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding, verticalSpacing = 0.dp, startEndPadding = 0.dp) {
            item { HeadlineText(text = stringResource(id = R.string.appearance)) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .clickable { updatePreferences(ThemeMode.entries) }
                ) {
                    Row(
                        modifier = Modifier.padding(preferencesPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dark_mode),
                            contentDescription = stringResource(R.string.theme),
                            modifier = iconPaddingModifier
                        )
                        Column {
                            Text(
                                text = stringResource(id = R.string.theme),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(
                                    id = Formatter.preferenceToStringId(
                                        selectedTheme
                                    )
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .clickable {
                                haptic.performHapticFeedback(
                                    hapticFeedbackType = if (!materialModeOn) HapticFeedbackType.ToggleOn
                                    else HapticFeedbackType.ToggleOff
                                )
                                saveBooleanValue(
                                    UserPreferencesRepository.materialModeKey,
                                    !materialModeOn
                                )
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(preferencesPadding)
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_material),
                                contentDescription = stringResource(R.string.material_you),
                                modifier = iconPaddingModifier
                            )
                            Column(verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = stringResource(id = R.string.material_you),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(
                                        id = if (materialModeOn) R.string.dynamic_color_enabled else R.string.dynamic_color_disabled
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Switch(
                            modifier = iconPaddingModifier,
                            checked = materialModeOn,
                            onCheckedChange = null
                        )
                    }
                }
            }


            item { HeadlineText(text = stringResource(id = R.string.settings_general)) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .clickable { updatePreferences(Language.entries) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.padding(preferencesPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_translate),
                            contentDescription = stringResource(R.string.translate),
                            modifier = iconPaddingModifier
                        )
                        Column {
                            Text(
                                text = stringResource(id = R.string.language),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(
                                    id = Formatter.preferenceToStringId(
                                        selectedLanguage
                                    )
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .clickable {
                            haptic.performHapticFeedback(
                                hapticFeedbackType = if (!keepWorkoutScreenOn) HapticFeedbackType.ToggleOn
                                else HapticFeedbackType.ToggleOff
                            )
                            saveBooleanValue(
                                UserPreferencesRepository.keepOnWorkoutScreenKey,
                                !keepWorkoutScreenOn
                            )
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(preferencesPadding)
                            .weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_keep),
                            contentDescription = stringResource(R.string.keep_screen_on),
                            modifier = iconPaddingModifier
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.keep_screen_on),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(
                                    id = if (keepWorkoutScreenOn) R.string.screen_on_desc else R.string.screen_off_desc
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Switch(
                        modifier = iconPaddingModifier,
                        checked = keepWorkoutScreenOn,
                        onCheckedChange = null
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .clickable {
                            haptic.performHapticFeedback(
                                hapticFeedbackType = if (!restTimerSoundOn) HapticFeedbackType.ToggleOn
                                else HapticFeedbackType.ToggleOff
                            )
                            saveBooleanValue(
                                UserPreferencesRepository.restTimerSoundKey,
                                !restTimerSoundOn
                            )
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(preferencesPadding)
                            .weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notification_sound),
                            contentDescription = stringResource(R.string.rest_timer_sound),
                            modifier = iconPaddingModifier
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.rest_timer_sound),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(
                                    id = if (restTimerSoundOn) R.string.rest_timer_sound_on_desc else R.string.rest_timer_sound_off_desc
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Switch(
                        modifier = iconPaddingModifier,
                        checked = restTimerSoundOn,
                        onCheckedChange = null
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun SettingsScreenPreview() {
    val materialModeOn = Random.nextBoolean()
    val theme = ThemeMode.entries.random()

    LibreFitTheme(dynamicColor = materialModeOn, darkTheme = theme != ThemeMode.LIGHT) {
        SettingsScreenContent(
            selectedTheme = theme,
            materialModeOn = materialModeOn,
            updatePreferences = {},
            selectedLanguage = Language.SYSTEM,
            keepWorkoutScreenOn = Random.nextBoolean(),
            restTimerSoundOn = Random.nextBoolean(),
            saveBooleanValue = { _, _ -> },
            navController = rememberNavController()
        )
    }
}