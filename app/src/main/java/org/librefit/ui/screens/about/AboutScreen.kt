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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.nav.Destination
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.bottomMargin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavHostController) {

    val context = LocalContext.current

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var showUrlDialog by remember { mutableStateOf(false) }

    var url by remember { mutableStateOf("") }

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
                        val clip = ClipData.newPlainText("Website url", url)
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
        title = AnnotatedString(stringResource(id = R.string.about)),
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

            item {
                val logoSize = 170.dp
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo),
                    contentDescription = stringResource(R.string.app_name),
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

            item {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val version = pInfo?.versionName.toString()
                Text(stringResource(R.string.version) + ": " + version)
            }

            item {
                HeadlineText(text = stringResource(R.string.support_project))
            }


            item {
                AboutItem(
                    Icons.Default.Favorite,
                    text = stringResource(R.string.donate),
                    description = stringResource(R.string.donate_desc),
                    onClick = {},
                    enabled = false
                )
            }

            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_handshake),
                    text = stringResource(R.string.contribute),
                    description = stringResource(R.string.contribute_desc),
                    onClick = {},
                    enabled = false
                )
            }

            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_translate),
                    stringResource(R.string.translate),
                    description = stringResource(R.string.translate_desc),
                    onClick = {},
                    enabled = false
                )
            }

            item {
                HeadlineText(stringResource(R.string.info))
            }

            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_policy),
                    stringResource(R.string.privacy_policy),
                    description = stringResource(R.string.privacy_policy_desc),
                    onClick = {
                        url = context.getString(R.string.url_privacy)
                        showUrlDialog = true
                    }

                )
            }

            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_globe),
                    stringResource(R.string.website),
                    onClick = {
                        url = context.getString(R.string.url_website)
                        showUrlDialog = true
                    }
                )
            }


            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_license),
                    stringResource(R.string.license),
                    description = stringResource(R.string.license_desc),
                    onClick = {
                        navController.navigate(Destination.LicenseScreen)
                    }
                )
            }

            item {
                AboutItem(
                    ImageVector.vectorResource(R.drawable.ic_github),
                    stringResource(R.string.github),
                    stringResource(R.string.source_code),
                    onClick = {
                        url = context.getString(R.string.url_source_code)
                        showUrlDialog = true
                    }
                )
            }


            item {
                HeadlineText(stringResource(R.string.contributors))
            }

            item {
                AboutItem(
                    Icons.Default.Person,
                    stringResource(R.string.url_IamDg).split("/").last(),
                    stringResource(R.string.founder),
                    onClick = {
                        url = context.getString(R.string.url_IamDg)
                        showUrlDialog = true
                    }
                )
            }

            item {
                HeadlineText(stringResource(R.string.translators))
            }

            item {
                AboutItem(
                    Icons.Default.Person,
                    stringResource(R.string.url_IamDg).split("/").last(),
                    stringResource(R.string.contributed_to) + stringResource(R.string.language_italian),
                    onClick = {
                        url = context.getString(R.string.url_IamDg)
                        showUrlDialog = true
                    }
                )
            }
            bottomMargin()
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
                contentDescription = text,
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