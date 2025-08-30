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

package org.librefit.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.librefit.R
import org.librefit.enums.userPreferences.DialogPreference
import org.librefit.enums.userPreferences.Language
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.util.Formatter

@Composable
fun PreferenceDialog(
    currentPreference: DialogPreference?,
    preferences: List<DialogPreference>,
    updatePreference: (DialogPreference) -> Unit,
    onDismiss: () -> Unit,
) {
    require(preferences.isNotEmpty()) { "Preferences must be not empty" }

    AlertDialog(
        title = {
            Text(
                text = stringResource(
                    when (preferences.first()) {
                        is Language -> R.string.language
                        is ThemeMode -> R.string.theme
                    }
                )
            )
        },
        icon = {
            Icon(
                painter = painterResource(
                    when (preferences.first()) {
                        is Language -> R.drawable.ic_translate
                        is ThemeMode -> R.drawable.ic_dark_mode
                    }
                ),
                contentDescription = null
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = { /*The user doesn't need to confirm*/ },
        text = {
            LazyColumn(Modifier.heightIn(max = 300.dp)) {
                items(preferences) { preference ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .padding(5.dp)
                            .clickable {
                                updatePreference(preference)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = preference == currentPreference,
                            onClick = {
                                updatePreference(preference)
                            }
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(Formatter.preferenceToStringId(preference)))
                    }
                }
            }
        }
    )
}