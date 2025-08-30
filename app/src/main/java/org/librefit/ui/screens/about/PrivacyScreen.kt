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

package org.librefit.ui.screens.about

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import org.librefit.R
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.MarkdownText
import org.librefit.ui.components.dialogs.UrlActionDialog
import org.librefit.ui.theme.LibreFitTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PrivacyScreen(
    navigateBack: (() -> Unit)? = null
) {

    var privacyPolicyText by rememberSaveable { mutableStateOf("") }

    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        privacyPolicyText = resources.openRawResource(R.raw.privacy_policy)
            .bufferedReader()
            .use { it.readText() }
    }


    val url = remember { mutableStateOf<String?>(null) }

    url.value?.let {
        UrlActionDialog(it) { url.value = null }
    }

    LibreFitScaffold(
        title = AnnotatedString(stringResource(id = R.string.privacy)),
        navigateBack = navigateBack,
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding) {
            item { HeadlineText(stringResource(R.string.privacy_notice)) }

            item {
                MarkdownText(
                    stringResource(R.string.privacy_notice_summary)
                            + "\n\n## " + stringResource(R.string.privacy_notice_optional_perm)
                            + "\n\n" + stringResource(R.string.privacy_notice_notification_perm)
                            + "\n\n## " + stringResource(R.string.privacy_notice_default_perm)
                            + "\n\n" + stringResource(R.string.privacy_notice_foreground_perm)
                )
            }

            item { HeadlineText(stringResource(R.string.privacy_policy)) }

            item {
                LibreFitButton(
                    text = stringResource(R.string.view_online_version),
                    icon = painterResource(R.drawable.ic_exit_to_app),
                    onClick = {
                        url.value = resources.getString(R.string.url_privacy)
                    }
                )
            }

            item {
                MarkdownText(privacyPolicyText)
            }
        }
    }

}

@Preview
@Composable
private fun PrivacyScreenPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        PrivacyScreen()
    }
}