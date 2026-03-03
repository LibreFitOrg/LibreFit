/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.about

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.nav.Route
import org.librefit.ui.components.AppNameText
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.dialogs.UrlActionDialog
import org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet
import org.librefit.ui.theme.LibreFitTheme


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(navController: NavHostController) {

    val context = LocalContext.current

    val resources = LocalResources.current

    val url = rememberSaveable { mutableStateOf<String?>(null) }

    url.value?.let {
        UrlActionDialog(it) { url.value = null }
    }

    val infoMode = rememberSaveable {
        mutableStateOf<InfoMode?>(null)
    }

    infoMode.value?.let {
        InfoModalBottomSheet(
            infoMode = it
        ) {
            infoMode.value = null
        }
    }

    LibreFitScaffold(
        title = AnnotatedString(stringResource(id = R.string.about)),
        navigateBack = navController::navigateUp,
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.ic_launcher_background))
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceContainer),
                            CircleShape
                        )
                )
            }
            item {
                AppNameText(MaterialTheme.typography.displayLargeEmphasized)
            }

            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo?.versionName?.let {
                item {
                    Text(stringResource(R.string.version) + ": $it")
                }
            }

            item {
                // Animated button
                val infiniteTransition = rememberInfiniteTransition()
                val animationProgress by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000),
                        repeatMode = RepeatMode.Restart
                    )
                )

                val color1 = MaterialTheme.colorScheme.primary
                val color2 = MaterialTheme.colorScheme.inversePrimary
                val colors = remember(color1, color2) {
                    listOf(color1, color2, color1)
                }

                val shape = ButtonDefaults.shape
                val pressedShape = ButtonDefaults.pressedShape

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                Button(
                    onClick = {
                        navController.navigate(Route.SupportScreen()) {
                            launchSingleTop = true
                        }
                    },
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = ButtonDefaults.MediumContentPadding,
                    interactionSource = interactionSource,
                    modifier = Modifier.drawWithCache {

                        // Everything inside onDrawWithContent runs in the draw phase so reading 'animationProgress' here will not cause recomposition
                        onDrawWithContent {
                            drawContent() // Draw the button first

                            // Calculate the radius using the exact size of the button
                            val radius = (size.width * animationProgress).coerceAtLeast(0.1f)

                            val brush = Brush.radialGradient(
                                colors = colors,
                                radius = radius,
                                center = center
                            )

                            // Draw the animated border
                            drawOutline(
                                outline = (if(isPressed) pressedShape else shape).createOutline(size, layoutDirection, this),
                                brush = brush,
                                style = Stroke(width = 10f)
                            )
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_favorite),
                            contentDescription = null
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(
                            text = stringResource(R.string.lets_build_it_together),
                            style = MaterialTheme.typography.headlineSmallEmphasized,
                            textAlign = TextAlign.Center
                        )
                    }
                }

            }

            item {
                LibreFitButton(
                    text = stringResource(R.string.librefit_is_under_threat),
                    elevated = false,
                    icon = painterResource(R.drawable.ic_warning),
                    iconDescription = stringResource(R.string.librefit_is_under_threat)
                ) {
                    infoMode.value = InfoMode.KEEP_ANDROID_OPEN
                }
            }

            item {
                HeadlineText(stringResource(R.string.info))
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_help),
                    text = stringResource(R.string.tutorial),
                    description = stringResource(R.string.tutorial_desc),
                    onClick = {
                        navController.navigate(Route.TutorialScreen()) { launchSingleTop = true }
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_policy),
                    text = stringResource(R.string.privacy),
                    description = stringResource(R.string.privacy_policy_desc),
                    onClick = {
                        navController.navigate(Route.PrivacyScreen) { launchSingleTop = true }
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_globe),
                    text = stringResource(R.string.website),
                    onClick = {
                        url.value = resources.getString(R.string.url_website)
                    }
                )
            }


            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_license),
                    text = stringResource(R.string.license),
                    description = stringResource(R.string.license_desc),
                    onClick = {
                        navController.navigate(Route.LicenseScreen) { launchSingleTop = true }
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_github),
                    text = stringResource(R.string.github),
                    description = stringResource(R.string.source_code),
                    onClick = {
                        url.value = resources.getString(R.string.url_source_code)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_contract),
                    text = stringResource(R.string.dependencies),
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
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.url_IamDg).split("/").last(),
                    description = stringResource(R.string.founder),
                    onClick = {
                        url.value = resources.getString(R.string.url_IamDg)
                    }
                )
            }

            item {
                HeadlineText(stringResource(R.string.translators))
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.url_IamDg).split("/").last(),
                    description = stringResource(R.string.contributed_to) + stringResource(R.string.language_italian),
                    onClick = {
                        url.value = resources.getString(R.string.url_IamDg)
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


@Preview(locale = "en")
@Composable
private fun AboutScreenPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        AboutScreen(rememberNavController())
    }
}