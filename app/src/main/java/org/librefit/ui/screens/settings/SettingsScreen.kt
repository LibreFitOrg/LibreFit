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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.enums.Language
import org.librefit.enums.ThemeMode
import org.librefit.nav.Destination
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.bottomMargin

@SuppressLint("BatteryLife")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController
) {

    val viewModel: SettingsScreenViewModel = hiltViewModel()


    val context = LocalContext.current

    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    viewModel.checkBatteryOptimization(pm, context.packageName)


    var selectedLanguage by remember {
        mutableStateOf(
            AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore("-")
        )
    }

    val selectedTheme by viewModel.themeMode.collectAsState(ThemeMode.SYSTEM)

    val keepWorkoutScreenOn by viewModel.keepScreenOn.collectAsState(initial = true)

    val materialModeOn by viewModel.materialMode.collectAsState(initial = false)


    var showPreferenceDialog by remember { mutableStateOf(false) }



    if (showPreferenceDialog) {
        AlertDialog(
            title = { Text(stringResource(id = R.string.language)) },
            onDismissRequest = { showPreferenceDialog = false },
            confirmButton = { /*The user doesn't need to confirm*/ },
            text = {
                LazyColumn(Modifier.heightIn(max = 200.dp)) {
                    items(Language.entries) { language ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language.code == selectedLanguage,
                                onClick = {
                                    selectedLanguage = language.code
                                    viewModel.changeLanguage(language)
                                }
                            )
                            Text(text = stringResource(id = languageCodeToId(language.code)))
                        }
                    }
                }
            }
        )
    }

    val view = LocalView.current

    CustomScaffold(
        title = AnnotatedString(stringResource(id = R.string.settings)),
        navigateBack = { navController.popBackStack() },
        actions = listOf { navController.navigate(Destination.AboutScreen) },
        actionsIcons = listOf(Icons.Default.Info),
        actionsElevated = listOf(false),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val paddingModifier = Modifier.padding(start = 20.dp, end = 20.dp)

            item { HeadlineText(text = stringResource(id = R.string.appearance)) }

            item {
                Column {
                    Row {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_dark_mode),
                            contentDescription = stringResource(R.string.theme),
                            modifier = paddingModifier
                        )
                        Text(
                            text = stringResource(id = R.string.theme),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SingleChoiceSegmentedButtonRow {
                            ThemeMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = selectedTheme == mode,
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstantsCompat.TOGGLE_ON)
                                        viewModel.savePreference(
                                            key = 0,
                                            value = index
                                        )
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = ThemeMode.entries.size
                                    )
                                ) { Text(stringResource(id = themeModeToId(mode))) }
                            }

                        }
                    }
                }
            }

            item {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_material),
                                contentDescription = stringResource(R.string.material_you),
                                modifier = paddingModifier
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
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Switch(
                            modifier = paddingModifier,
                            checked = materialModeOn,
                            onCheckedChange = {
                                view.performHapticFeedback(
                                    if (it) HapticFeedbackConstantsCompat.TOGGLE_ON
                                    else HapticFeedbackConstantsCompat.TOGGLE_OFF
                                )
                                viewModel.savePreference(
                                    key = 1,
                                    value = it
                                )
                            }
                        )
                    }
                }
            }


            item { HeadlineText(text = stringResource(id = R.string.settings_general)) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPreferenceDialog = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_translate),
                        contentDescription = stringResource(R.string.translate),
                        modifier = paddingModifier
                    )
                    Column {
                        Text(
                            text = stringResource(id = R.string.language),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(id = languageCodeToId(selectedLanguage)),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_keep),
                            contentDescription = stringResource(R.string.keep_screen_on),
                            modifier = paddingModifier
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
                        modifier = paddingModifier,
                        checked = keepWorkoutScreenOn,
                        onCheckedChange = {
                            view.performHapticFeedback(
                                if (it) HapticFeedbackConstantsCompat.TOGGLE_ON
                                else HapticFeedbackConstantsCompat.TOGGLE_OFF
                            )
                            viewModel.savePreference(
                                key = 2,
                                value = it
                            )
                        }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_speed),
                            contentDescription = stringResource(R.string.background_usage),
                            modifier = paddingModifier
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.background_usage),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.background_usage_desc
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Switch(
                        modifier = paddingModifier,
                        checked = viewModel.isIgnoringBatteryOptimization.value,
                        onCheckedChange = {
                            view.performHapticFeedback(
                                if (it) HapticFeedbackConstantsCompat.TOGGLE_ON
                                else HapticFeedbackConstantsCompat.TOGGLE_OFF
                            )

                            var intent: Intent
                            if (!viewModel.isIgnoringBatteryOptimization.value) {
                                intent =
                                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = "package:${context.packageName}".toUri()
                                    }
                            } else {
                                intent =
                                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
            bottomMargin()
            //TODO: toggle to enable/disable rest timer sound
        }
    }
}


private fun themeModeToId(themeMode: ThemeMode): Int {
    val id = when (themeMode) {
        ThemeMode.SYSTEM -> R.string.follow_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    }
    return id
}

private fun languageCodeToId(code: String): Int {
    val result = when (code) {
        "en" -> R.string.language_english_nt
        "it" -> R.string.language_italian_nt
        else -> R.string.follow_system
    }

    return result
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(rememberNavController())
}