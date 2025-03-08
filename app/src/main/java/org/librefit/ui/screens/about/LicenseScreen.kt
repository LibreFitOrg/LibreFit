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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import org.librefit.R
import org.librefit.ui.components.CustomButton
import org.librefit.ui.components.CustomScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(navigateBack: () -> Unit) {
    val licenseText = rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        licenseText.value = loadLicenseText(context)
    }


    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var showUrlDialog by remember { mutableStateOf(false) }

    var url = stringResource(R.string.url_gpl3)

    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text(stringResource(R.string.link)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = url.toUri()
                        }
                        context.startActivity(intent)
                        showUrlDialog = false
                    }
                ) {
                    Text(stringResource(R.string.open))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val clip = ClipData.newPlainText("Copied Url", url)
                        clipboardManager.setPrimaryClip(clip)
                        showUrlDialog = false
                    }
                ) {
                    Text(stringResource(R.string.copy))
                }
            },
            text = { Text(text = url) }
        )
    }

    CustomScaffold(
        title = AnnotatedString(stringResource(id = R.string.license)),
        navigateBack = navigateBack,
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CustomButton(
                text = stringResource(R.string.view_online_version),
                icon = Icons.AutoMirrored.Default.ExitToApp,
                onClick = { showUrlDialog = true }
            )
            HorizontalDivider()
            Box(
                modifier = Modifier
                    .padding(start = 15.dp, end = 15.dp)
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                    .fillMaxSize(),
            ) {
                Text(licenseText.value)
            }
        }
    }
}

private fun loadLicenseText(context: Context): String {
    val inputStream = context.resources.openRawResource(R.raw.license)
    return inputStream.bufferedReader().use { it.readText() }
}