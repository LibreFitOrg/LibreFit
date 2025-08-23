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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import org.librefit.R
import org.librefit.ui.theme.LibreFitTheme

/**
 * This dialog allow the user to either open the [url] in a browser or copy it to the clipboard.
 *
 * @param url It holds the URL string. It must match [Patterns.WEB_URL].
 * @param onDismiss A lambda called when the user dismiss this dialog.
 *
 * @throws IllegalArgumentException when [url] value is not a valid URL according to [Patterns.WEB_URL]
 */
@Composable
fun UrlActionDialog(
    url: String,
    onDismiss: () -> Unit
) {
    require(Patterns.WEB_URL.matcher(url).matches()) {
        "Invalid URL: $url"
    }

    val context = LocalContext.current

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.url_dialog)) },
        confirmButton = {
            TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = url.toUri()
                    }
                    context.startActivity(intent)
                    onDismiss
                }) {
                Text(stringResource(R.string.open))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    val clip = ClipData.newPlainText("Copied Url", url)
                    clipboardManager.setPrimaryClip(clip)
                    onDismiss
                }) {
                Text(stringResource(R.string.copy))
            }
        },
        text = { Text(text = url) })

}

@Preview
@Composable
private fun UrlActionDialogPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        UrlActionDialog("https://example.com") {}
    }
}