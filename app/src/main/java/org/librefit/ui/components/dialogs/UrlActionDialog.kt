/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components.dialogs

import android.content.ClipData
import android.content.Intent
import android.util.Patterns
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.enums.userPreferences.ThemeMode
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

    val clipboardManager = LocalClipboard.current

    val coroutine = rememberCoroutineScope()

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
                    onDismiss()
                }) {
                Text(stringResource(R.string.open))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    coroutine.launch {
                        val clipData = ClipData.newPlainText("Copied Url", url)
                        clipboardManager.setClipEntry(
                            ClipEntry(clipData)
                        )
                        onDismiss()
                    }
                }) {
                Text(stringResource(R.string.copy))
            }
        },
        text = { Text(text = url) })

}

@Preview
@Composable
private fun UrlActionDialogPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        UrlActionDialog("https://example.com") {}
    }
}