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

package org.librefit.ui.screens.about

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.nav.Destination
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.HeadlineText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavHostController) {

    val context = LocalContext.current

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var showDialog by remember { mutableStateOf(false) }

    var url by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(url)
                        }
                        context.startActivity(intent)
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.label_open))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val clip = ClipData.newPlainText("Copied Url", url)
                        clipboardManager.setPrimaryClip(clip)
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.label_copy))
                }
            },
            text = { Text(text = url) }
        )
    }

    CustomScaffold(
        title = stringResource(id = R.string.label_about),
        navigateBack = { navController.popBackStack() },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(start = 15.dp, end = 15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val logoSize = 170.dp
            item {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(logoSize)
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceContainer),
                            RoundedCornerShape(logoSize)
                        )
                )
            }
            item {
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append(stringResource(id = R.string.app_name).removeRange(5, 8))
                        }
                        append(stringResource(id = R.string.app_name).removeRange(0, 5))
                    },
                    style = MaterialTheme.typography.displayMedium
                )
            }

            var version = ""

            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                if (pInfo != null) {
                    version = pInfo.versionName.toString()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            item {
                if (version != "") {
                    Text(version)
                }
            }

            item {
                HeadlineText(text = stringResource(R.string.label_support_project))
            }


            item {
                AboutItem(
                    Icons.Default.Favorite,
                    text = stringResource(R.string.label_donate),
                    description = stringResource(R.string.label_donate_desc),
                    onClick = {},
                    enabled = false
                )
            }

            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_handshake),
                    text = stringResource(R.string.label_contribute),
                    description = stringResource(R.string.label_contribute_desc),
                    onClick = {},
                    enabled = false
                )
            }

            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_translate),
                    stringResource(R.string.label_translate),
                    description = stringResource(R.string.label_translate_desc),
                    onClick = {},
                    enabled = false
                )
            }

            item {
                HeadlineText(stringResource(R.string.label_info))
            }

            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_policy),
                    stringResource(R.string.label_privacy_policy),
                    description = stringResource(R.string.label_privacy_policy_desc),
                    onClick = {
                        url = context.getString(R.string.url_privacy)
                        showDialog = true
                    }

                )
            }

            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_globe),
                    stringResource(R.string.label_website),
                    onClick = {
                        url = context.getString(R.string.url_website)
                        showDialog = true
                    }
                )
            }


            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_license),
                    stringResource(R.string.label_license),
                    description = stringResource(R.string.label_license_desc),
                    onClick = {
                        navController.navigate(Destination.LicenseScreen)
                    }
                )
            }

            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_github),
                    stringResource(R.string.label_github),
                    stringResource(R.string.label_source_code),
                    onClick = {
                        url = context.getString(R.string.url_source_code)
                        showDialog = true
                    }
                )
            }

            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_codeberg),
                    stringResource(R.string.label_codeberg),
                    stringResource(R.string.label_source_code),
                    onClick = {
                        url = context.getString(R.string.url_codeberg)
                        showDialog = true
                    }
                )
            }


            item {
                HeadlineText(stringResource(R.string.label_contributors))
            }

            item {
                AboutItem(
                    Icons.Default.Person,
                    stringResource(R.string.url_IamDg).split("/").last(),
                    stringResource(R.string.label_founder),
                    onClick = {
                        url = context.getString(R.string.url_IamDg)
                        showDialog = true
                    }
                )
            }

            item {
                HeadlineText(stringResource(R.string.label_translators))
            }

            item {
                AboutItem(
                    Icons.Default.Person,
                    stringResource(R.string.url_IamDg).split("/").last(),
                    stringResource(R.string.label_contributed_to) + stringResource(R.string.label_language_italian),
                    onClick = {
                        url = context.getString(R.string.url_IamDg)
                        showDialog = true
                    }
                )
            }
        }
    }
}

@Composable
private fun AboutItem(
    imageVector: ImageVector,
    text: String,
    description: String = "",
    onClick: () -> Unit,
    enabled: Boolean = true
) {

    OutlinedCard(
        enabled = enabled,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = "$text icon",
                modifier = Modifier.padding(end = 20.dp)
            )

            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@Preview
@Composable
private fun AboutScreenPreview() {
    AboutScreen(rememberNavController())
}