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

package org.librefit.ui.screens.about

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import org.librefit.R
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.MarkdownText
import org.librefit.ui.components.dialogs.UrlActionDialog
import org.librefit.ui.theme.LibreFitTheme

@Composable
fun LicenseScreen(navigateBack: () -> Unit) {
    val licenseText = rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        licenseText.value = context.resources.openRawResource(R.raw.license)
            .bufferedReader()
            .use { it.readText() }
    }


    val url = remember { mutableStateOf("") }

    UrlActionDialog(url)

    LibreFitScaffold(
        title = AnnotatedString(stringResource(id = R.string.license)),
        navigateBack = navigateBack,
    ) { innerPadding ->
        // This box is used to constrain width in landscape mode
        LibreFitLazyColumn(innerPadding) {
            item {
                LibreFitButton(
                    text = stringResource(R.string.view_online_version),
                    icon = Icons.AutoMirrored.Default.ExitToApp,
                    onClick = {
                        url.value = context.getString(R.string.url_gpl3)
                    }
                )
            }

            item {
                MarkdownText(licenseText.value)
            }
        }
    }
}

@Preview
@Composable
private fun LicenseScreenPreview() {
    LibreFitTheme(false, true) {
        LicenseScreen { }
    }
}