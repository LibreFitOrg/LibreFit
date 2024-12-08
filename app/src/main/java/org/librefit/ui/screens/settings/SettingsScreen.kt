/*
 * Copyright (c) 2024 LibreFit
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.librefit.R
import org.librefit.data.DataStoreManager
import org.librefit.enums.Language
import org.librefit.enums.ThemeMode
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.HeadlineText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit
) {
    val context = LocalContext.current

    val userPreferences = DataStoreManager(context)

    val viewModel: SettingsScreenViewModel = viewModel()

    viewModel.initPreferences(userPreferences)

    var selectedLanguage by remember {
        mutableStateOf(
            AppCompatDelegate.getApplicationLocales().toLanguageTags()
        )
    }

    val selectedTheme by userPreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

    val keepWorkoutScreenOn by userPreferences.workoutScreenOn.collectAsState(initial = true)

    val materialModeOn by userPreferences.materialMode.collectAsState(initial = true)


    var openPreferenceDialog by remember { mutableStateOf(false) }



    if (openPreferenceDialog) {
        AlertDialog(
            title = { Text(stringResource(id = R.string.label_language)) },
            onDismissRequest = { openPreferenceDialog = false },
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

    CustomScaffold(
        title = stringResource(id = R.string.label_settings),
        navigateBack = navigateBack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            item { HeadlineText(text = stringResource(id = R.string.label_appearance)) }

            item {
                Column {
                    Row {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_dark_mode),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.label_theme),
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
                                        viewModel.savePreference(
                                            key = userPreferences.themeModeKey,
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
                            .height(70.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_material),
                                contentDescription = "",
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                            )
                            Column(verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = stringResource(id = R.string.label_material_you),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(
                                        id = if (materialModeOn) R.string.label_dynamic_color_enabled else R.string.label_dynamic_color_disabled
                                    ),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Switch(
                            checked = materialModeOn,
                            onCheckedChange = {
                                viewModel.savePreference(
                                    key = userPreferences.materialModeKey,
                                    value = it
                                )
                            }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .height(70.dp)
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
                            contentDescription = "",
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.label_keep_screen_on),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(
                                    id = if (keepWorkoutScreenOn) R.string.label_screen_on_desc else R.string.label_screen_off_desc
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Switch(
                        checked = keepWorkoutScreenOn,
                        onCheckedChange = {
                            viewModel.savePreference(
                                key = userPreferences.keepOnWorkoutScreenKey,
                                value = it
                            )
                        }
                    )
                }
            }



            item { HeadlineText(text = stringResource(id = R.string.label_settings_general)) }

            item {
                Row(
                    modifier = Modifier
                        .height(70.dp)
                        .fillMaxWidth()
                        .clickable { openPreferenceDialog = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_translate),
                        contentDescription = null,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(id = R.string.label_language),
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
        }
    }
}


private fun themeModeToId(themeMode: ThemeMode): Int {
    val id = when (themeMode) {
        ThemeMode.SYSTEM -> R.string.label_follow_system
        ThemeMode.LIGHT -> R.string.label_theme_light
        ThemeMode.DARK -> R.string.label_theme_dark
    }
    return id
}

private fun languageCodeToId(code: String): Int {
    val result = when (code) {
        "en" -> R.string.label_language_english_nt
        "it" -> R.string.label_language_italian_nt
        else -> R.string.label_follow_system
    }

    return result
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen { }
}