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

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.nav.Route
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitAppName
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.dialogs.UrlActionDialog
import org.librefit.ui.theme.LibreFitTheme


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(navController: NavHostController) {

    val context = LocalContext.current

    val url = remember { mutableStateOf<String?>(null) }

    url.value?.let {
        UrlActionDialog(it) { url.value = null }
    }

    LibreFitScaffold(
        title = AnnotatedString(stringResource(id = R.string.about)),
        navigateBack = navController::navigateUp,
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding) {
            item {
                val logoSize = 170.dp
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
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
                LibreFitAppName.AppNameText(MaterialTheme.typography.displayLargeEmphasized)
            }

            item {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val version = pInfo?.versionName.toString()
                Text(stringResource(R.string.version) + ": $version")
            }

            item {
                BoxWithConstraints {
                    val spaceMaxWidth = with(LocalDensity.current) { maxWidth.toPx() }

                    val infiniteTransition = rememberInfiniteTransition()
                    val animationProgress = infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = spaceMaxWidth * 5,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000),
                            repeatMode = RepeatMode.Restart
                        )
                    )

                    val a = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.inversePrimary,
                            MaterialTheme.colorScheme.primary
                        ),
                        radius = animationProgress.value
                    )

                    Button(
                        onClick = {
                            navController.navigate(Route.SupportScreen) {
                                launchSingleTop = true
                            }
                        },
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = ButtonDefaults.MediumContentPadding,
                        border = BorderStroke(
                            width = 4.dp,
                            brush = a
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_favorite),
                                contentDescription = null
                            )
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 10.dp),
                                text = stringResource(R.string.lets_build_it_together),
                                style = MaterialTheme.typography.headlineSmallEmphasized,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item {
                HeadlineText(stringResource(R.string.info))
            }

            item {
                AboutItem(
                    painterResource(R.drawable.ic_policy),
                    stringResource(R.string.privacy),
                    description = stringResource(R.string.privacy_policy_desc),
                    onClick = {
                        navController.navigate(Route.PrivacyScreen) { launchSingleTop = true }
                    }

                )
            }

            item {
                AboutItem(
                    painterResource(R.drawable.ic_globe),
                    stringResource(R.string.website),
                    onClick = {
                        url.value = context.getString(R.string.url_website)
                    }
                )
            }


            item {
                AboutItem(
                    painterResource(R.drawable.ic_license),
                    stringResource(R.string.license),
                    description = stringResource(R.string.license_desc),
                    onClick = {
                        navController.navigate(Route.LicenseScreen) { launchSingleTop = true }
                    }
                )
            }

            item {
                AboutItem(
                    painterResource(R.drawable.ic_github),
                    stringResource(R.string.github),
                    stringResource(R.string.source_code),
                    onClick = {
                        url.value = context.getString(R.string.url_source_code)
                    }
                )
            }

            item {
                AboutItem(
                    painterResource(R.drawable.ic_library),
                    stringResource(R.string.libraries),
                    onClick = {
                        navController.navigate(Route.LibrariesScreen) { launchSingleTop = true }
                    }
                )
            }


            item {
                HeadlineText(stringResource(R.string.contributors))
            }

            item {
                AboutItem(
                    painterResource(R.drawable.ic_person),
                    stringResource(R.string.url_IamDg).split("/").last(),
                    stringResource(R.string.founder),
                    onClick = {
                        url.value = context.getString(R.string.url_IamDg)
                    }
                )
            }

            item {
                HeadlineText(stringResource(R.string.translators))
            }

            item {
                AboutItem(
                    painterResource(R.drawable.ic_person),
                    stringResource(R.string.url_IamDg).split("/").last(),
                    stringResource(R.string.contributed_to) + stringResource(R.string.language_italian),
                    onClick = {
                        url.value = context.getString(R.string.url_IamDg)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AboutItem(
    icon: Painter,
    text: String,
    description: String = "",
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(
            shape = MaterialTheme.shapes.extraLarge
        ),
        contentPadding = ButtonDefaults.MediumContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = icon,
                contentDescription = text,
                modifier = Modifier.padding(end = 20.dp)
            )

            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium
                )
                if (description.isNotBlank()) {
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
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        AboutScreen(rememberNavController())
    }
}