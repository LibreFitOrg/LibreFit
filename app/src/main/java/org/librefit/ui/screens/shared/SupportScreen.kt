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
 *
 * LibreFit is subject to additional terms covering author attribution and
 * trademark usage, as found in the accompanying ADDITIONAL_TERMS.md file.
 */

package org.librefit.ui.screens.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.enums.supporter.SupporterVerificationResult
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitAppName.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.PulsingHeartLottie
import org.librefit.ui.components.dialogs.UrlActionDialog
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.SupporterVerifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SupportScreen(
    navHostController: NavHostController,
    supporterInfo: Boolean = false,
    isSupporter: Boolean = false,
    updateIsSupporter: (Boolean) -> Unit = {}
) {

    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (supporterInfo) 6 else 0
    )

    val resources = LocalResources.current

    var url by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    url?.let {
        UrlActionDialog(it) { url = null }
    }

    LibreFitScaffold(
        navigateBack = navHostController::navigateUp
    ) { innerPadding ->
        LibreFitLazyColumn(
            innerPadding = innerPadding, lazyListState = lazyListState
        ) {
            item {
                PulsingHeartLottie(
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
            }
            item {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.lets_build))
                        append(" ")

                        GetAppNameInAnnotatedBuilder()
                        append(" ")
                        append(stringResource(R.string.together))
                    }, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center
                )
            }
            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.motivation_1)
                        )
                        Text(
                            text = stringResource(R.string.motivation_2)
                        )
                        Text(
                            text = stringResource(R.string.motivation_3),
                            style = MaterialTheme.typography.bodyLargeEmphasized
                        )
                    }
                }
            }
            item {
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        url = resources.getString(R.string.url_donate)
                    },
                    contentPadding = ButtonDefaults.MediumContentPadding
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 15.dp),
                            painter = painterResource(R.drawable.ic_favorite),
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.donate),
                            style = MaterialTheme.typography.headlineSmallEmphasized
                        )
                    }
                }
            }
            item {
                OutlinedButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        url = resources.getString(R.string.url_contribute)
                    },
                    contentPadding = ButtonDefaults.MediumContentPadding
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 15.dp),
                            painter = painterResource(R.drawable.ic_handshake),
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.contribute),
                            style = MaterialTheme.typography.headlineSmallEmphasized
                        )
                    }
                }
            }
            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.motivation_4),
                            style = MaterialTheme.typography.bodyLargeEmphasized
                        )
                    }
                }
            }
            item {
                HeadlineText(text = stringResource(R.string.supporter_version))
            }
            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.supporter_version_desc_1),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.supporter_version_desc_2),
                            textAlign = TextAlign.Center
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            SupporterVersionItem(
                                icon = painterResource(id = R.drawable.ic_material),
                                text = stringResource(R.string.material_you)
                            )
                            SupporterVersionItem(
                                icon = painterResource(id = R.drawable.ic_edit),
                                text = stringResource(R.string.create_exercises),
                                enabled = false
                            )
                        }
                    }
                }
            }



            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    AnimatedContent(
                        targetState = isSupporter
                    ) { isSupporterState ->
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isSupporterState) {
                                Text(
                                    text = stringResource(R.string.thank_supporter),
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center
                                )
                            } else {
//                                Text(
//                                    text = stringResource(R.string.verify_companion_or_code),
//                                    style = MaterialTheme.typography.titleLarge,
//                                    textAlign = TextAlign.Center
//                                )
//
//                                HorizontalDivider()
//                                var verificationAppStatus by rememberSaveable {
//                                    mutableStateOf<SupporterVerificationResult?>(
//                                        null
//                                    )
//                                }
//
//                                Text(
//                                    text = stringResource(R.string.companion_app_desc),
//                                    textAlign = TextAlign.Center
//                                )
//
//
//                                val context = LocalContext.current
//                                LibreFitButton(
//                                    text = stringResource(R.string.verify_companion_app),
//                                    onClick = {
//                                        SupporterVerifier.verifyCompanionApp(context = context)
//                                            .let {
//                                                verificationAppStatus = it
//                                                updateIsSupporter(
//                                                    it == SupporterVerificationResult.VALID_COMPANION_APP_SIGNATURE
//                                                )
//                                            }
//                                    }
//                                )
//                                Text(
//                                    text = buildAnnotatedString {
//                                        append(stringResource(R.string.status))
//                                        append(": ")
//                                        withStyle(
//                                            style = MaterialTheme.typography.titleMedium.copy(
//                                                color = verificationAppStatus?.let {
//                                                    if (it == SupporterVerificationResult.VALID_COMPANION_APP_SIGNATURE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
//                                                } ?: Color.Unspecified).toSpanStyle()) {
//                                            append(
//                                                stringResource(
//                                                    supporterVerificationResultToStringId(
//                                                        verificationAppStatus
//                                                    )
//                                                )
//                                            )
//                                        }
//                                    },
//                                    textAlign = TextAlign.Center,
//                                    style = MaterialTheme.typography.bodyLarge
//                                )
//
//                                HorizontalDivider()

                                Text(
                                    text = stringResource(R.string.code_desc),
                                    textAlign = TextAlign.Center
                                )

                                var code by rememberSaveable { mutableStateOf("") }

                                var verificationCodeStatus by rememberSaveable {
                                    mutableStateOf<SupporterVerificationResult?>(
                                        null
                                    )
                                }

                                val clipboardManager = LocalClipboard.current

                                OutlinedTextField(
                                    value = code,
                                    label = {
                                        Text("Insert code")
                                    },
                                    onValueChange = {
                                        verificationCodeStatus = null
                                        code = it
                                    },
                                    shape = MaterialTheme.shapes.large,
                                    modifier = Modifier.fillMaxWidth(),
                                    supportingText = verificationCodeStatus?.let {
                                        {
                                            Text(
                                                stringResource(
                                                    supporterVerificationResultToStringId(
                                                        verificationCodeStatus
                                                    )
                                                )
                                            )
                                        }
                                    },
                                    isError = verificationCodeStatus?.let {
                                        it != SupporterVerificationResult.VALID_CODE
                                    } ?: false,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                verificationCodeStatus = null
                                                code = if (code.isNotBlank()) {
                                                    ""
                                                } else {
                                                    clipboardManager.nativeClipboard.primaryClip?.getItemAt(
                                                        0
                                                    )?.text?.toString() ?: ""
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(if (code.isNotBlank()) R.drawable.ic_cancel else R.drawable.ic_paste),
                                                contentDescription = stringResource(
                                                    if (code.isNotBlank()) R.string.delete else R.string.paste
                                                )
                                            )
                                        }
                                    }
                                )
                                LibreFitButton(
                                    text = stringResource(R.string.verify_code),
                                    enabled = code.isNotBlank(),
                                    onClick = {
                                        SupporterVerifier.verifyCode(code = code).let {
                                            verificationCodeStatus = it
                                            updateIsSupporter(
                                                it == SupporterVerificationResult.VALID_CODE
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FlowRowScope.SupporterVersionItem(
    icon: Painter, text: String, enabled: Boolean = true
) {
    val shape = remember {
        listOf(
            MaterialShapes.Pentagon,
            MaterialShapes.Arch,
            MaterialShapes.Gem,
            MaterialShapes.Slanted,
            MaterialShapes.Cookie7Sided,
            MaterialShapes.Pill,
        ).random()
    }
    ElevatedCard(
        modifier = Modifier
            .size(150.dp)
            .weight(1f),
        shape = shape.toShape(),
        enabled = enabled,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainerHigh)
        ),
        onClick = {}) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.fillMaxWidth(0.3f), painter = icon, contentDescription = null
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = text, textAlign = TextAlign.Center
            )
        }
    }
}

private fun supporterVerificationResultToStringId(supporterVerificationResult: SupporterVerificationResult?): Int {
    return when (supporterVerificationResult) {
        SupporterVerificationResult.VALID_COMPANION_APP_SIGNATURE -> R.string.valid_companion_app_signature
        SupporterVerificationResult.INVALID_COMPANION_APP_SIGNATURE -> R.string.invalid_companion_app_signature
        SupporterVerificationResult.LIBREFIT_APP_NOT_FOUND -> R.string.librefit_app_not_found
        SupporterVerificationResult.LIBREFIT_APP_SIGNATURE_ERROR -> R.string.librefit_signature_error
        SupporterVerificationResult.COMPANION_APP_NOT_FOUND -> R.string.companion_app_not_found
        SupporterVerificationResult.COMPANION_APP_SIGNATURE_ERROR -> R.string.companion_signature_error
        SupporterVerificationResult.VALID_CODE -> R.string.valid_code
        SupporterVerificationResult.INVALID_CODE -> R.string.invalid_code
        SupporterVerificationResult.PUBLIC_KEY_NOT_INITIALIZED_PROPERLY -> R.string.public_key_not_initialized_properly
        SupporterVerificationResult.INVALID_PUBLIC_KEY -> R.string.invalid_public_key
        SupporterVerificationResult.INAPPROPRIATE_PUBLIC_KEY_STRING -> R.string.inappropriate_public_key_string
        SupporterVerificationResult.ALGORITHM_NOT_AVAILABLE -> R.string.algorithm_not_available
        SupporterVerificationResult.INVALID_SIGNATURE_ENCODING -> R.string.invalid_signature_encoding
        SupporterVerificationResult.MALFORMED_CODE -> R.string.malformed_code
        SupporterVerificationResult.MISSING_DOT_SEPARATOR -> R.string.missing_dot_separator
        SupporterVerificationResult.INVALID_PUBLIC_KEY_ENCODING -> R.string.invalid_public_key_encoding
        SupporterVerificationResult.UNKNOWN_ERROR -> R.string.unknown_error
        null -> R.string.to_verify
    }
}

@Preview
@Composable
private fun SupportScreenPreview() {
    var isSupporter by remember { mutableStateOf(false) }

    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SupportScreen(
            navHostController = rememberNavController(),
            supporterInfo = true,
            isSupporter = isSupporter,
            updateIsSupporter = { isSupporter = true })
    }
}