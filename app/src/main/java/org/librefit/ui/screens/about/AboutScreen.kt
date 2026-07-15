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
                    icon = painterResource(R.drawable.ic_codeberg),
                    text = stringResource(R.string.codeberg),
                    description = stringResource(R.string.source_code),
                    onClick = {
                        url.value = resources.getString(R.string.url_source_code_codeberg)
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
                HeadlineText(stringResource(R.string.donators))
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.FlashyGhost)
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.jakedevs)
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.debianite65),
                    onClick = {
                        url.value = resources.getString(R.string.url_debianite65)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.the_blue_blurr)
                )
            }

            item {
                HeadlineText(stringResource(R.string.contributors))
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.dpusceddu),
                    onClick = {
                        url.value = resources.getString(R.string.url_dpusceddu)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.b3nj5m1n),
                    onClick = {
                        url.value = resources.getString(R.string.url_b3nj5m1n)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.ByYeah),
                    onClick = {
                        url.value = resources.getString(R.string.url_ByYeah)
                    }
                )
            }

            item {
                HeadlineText(stringResource(R.string.translators))
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.doen1el),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_german),
                    onClick = {
                        url.value = resources.getString(R.string.url_doen1el)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.kid1412621),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_chinese_simplified),
                    onClick = {
                        url.value = resources.getString(R.string.url_kid1412621)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.mwesten),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_dutch),
                    onClick = {
                        url.value = resources.getString(R.string.url_mwesten)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.Odweta),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_czech)
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.VA5H_One),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_spanish),
                    onClick = {
                        url.value = resources.getString(R.string.url_VA5H_One)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.AhmedAwad7),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_arabic),
                    onClick = {
                        url.value = resources.getString(R.string.url_AhmedAwad7)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.b0saleh),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_arabic),
                    onClick = {
                        url.value = resources.getString(R.string.url_b0saleh)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.miguelsoaresouza8_droid),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_brazilian_portuguese),
                    onClick = {
                        url.value = resources.getString(R.string.url_miguelsoaresouza8_droid)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.DoubleCheddarBurger),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_catalan),
                    onClick = {
                        url.value = resources.getString(R.string.url_DoubleCheddarBurger)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.Spartang_117),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_french),
                    onClick = {
                        url.value = resources.getString(R.string.url_Spartang_117)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.josé_m),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_spanish),
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.SilentCoderHere),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_hindi),
                    onClick = {
                        url.value = resources.getString(R.string.url_SilentCoderHere)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.raihankr),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_indonesian),
                    onClick = {
                        url.value = resources.getString(R.string.url_raihankr)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.SelwynDO),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_spanish),
                    onClick = {
                        url.value = resources.getString(R.string.url_SelwynDO)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.UnknownLi),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_ukrainian),
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.xorodev),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_spanish),
                    onClick = {
                        url.value = resources.getString(R.string.url_xorodev)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.johncorea580_crypto),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_spanish),
                    onClick = {
                        url.value = resources.getString(R.string.url_johncorea580_crypto)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.theswordsgame),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_polish)
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.ylconion),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_chinese_simplified),
                    onClick = {
                        url.value = resources.getString(R.string.url_ylconion)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.zei_dan),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_chinese_simplified),
                    onClick = {
                        url.value = resources.getString(R.string.url_zei_dan)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.Robin_Schanbacher),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_german),
                    onClick = {
                        url.value = resources.getString(R.string.url_Robin_Schanbacher)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.sprivaq),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_german),
                    onClick = {
                        url.value = resources.getString(R.string.url_sprivaq)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.Safi_Ullah),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_urdu),
                    onClick = {
                        url.value = resources.getString(R.string.url_Safi_Ullah)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.dzjulis),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_russian),
                    onClick = {
                        url.value = resources.getString(R.string.url_dzjulis)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.Jae_Hyuk_Lee),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_korean)
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.Semprista),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_french),
                    onClick = {
                        url.value = resources.getString(R.string.url_Semprista)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.mister_bum),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_german),
                    onClick = {
                        url.value = resources.getString(R.string.url_mister_bum)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.BigP0tato),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_french),
                    onClick = {
                        url.value = resources.getString(R.string.url_BigP0tato)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.T_Silverspoon),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_swedish),
                    onClick = {
                        url.value = resources.getString(R.string.url_T_Silverspoon)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.tomel51733),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_spanish),
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.Ahmedbd23),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_chinese_simplified),
                    onClick = {
                        url.value = resources.getString(R.string.url_Ahmedbd23)
                    }
                )
            }

            item {
                AboutItem(
                    icon = painterResource(R.drawable.ic_person),
                    text = stringResource(R.string.Jent1357),
                    description = stringResource(R.string.contributed_to) + " " + stringResource(R.string.language_german),
                    onClick = {
                        url.value = resources.getString(R.string.url_Jent1357)
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
    onClick: () -> Unit = {}
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