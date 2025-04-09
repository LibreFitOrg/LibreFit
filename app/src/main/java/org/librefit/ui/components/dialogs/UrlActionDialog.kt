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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Patterns
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import org.librefit.R
import org.librefit.ui.theme.LibreFitTheme

/**
 * If the [url] state is not empty, this dialog is shown, allowing the user
 * to either open the URL in a browser or copy it to the clipboard.
 *
 * @param url A [MutableState] holding the URL string. The state is reset to an empty string after an action.
 *
 * @throws IllegalArgumentException when [url] value is not a valid URL according to [Patterns.WEB_URL]
 */
@Composable
fun UrlActionDialog(
    url: MutableState<String>
) {
    val context = LocalContext.current

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    if (url.value != "") {

        require(Patterns.WEB_URL.matcher(url.value).matches()) {
            "Invalid URL"
        }

        AlertDialog(
            onDismissRequest = { url.value = "" },
            title = { Text(stringResource(R.string.url)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = url.value.toUri()
                        }
                        context.startActivity(intent)
                        url.value = ""
                    }) {
                    Text(stringResource(R.string.open))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val clip = ClipData.newPlainText("Copied Url", url.value)
                        clipboardManager.setPrimaryClip(clip)
                        url.value = ""
                    }) {
                    Text(stringResource(R.string.copy))
                }
            },
            text = { Text(text = url.value) })
    }
}

@Preview
@Composable
private fun UrlActionDialogPreview() {
    LibreFitTheme(false, true) {
        UrlActionDialog(remember { mutableStateOf("https://example.com") })
    }
}