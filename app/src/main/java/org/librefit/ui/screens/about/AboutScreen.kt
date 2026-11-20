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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.librefit.BuildConfig
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.ui.components.AppNameText
import org.librefit.ui.components.HeadlineText
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.dialogs.UrlActionDialog
import org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet
import org.librefit.ui.theme.LibreFitTheme

/**
 * About screen: app info header (logo, name, version, support and warning actions)
 * followed by the data-driven credits defined in [aboutSections].
 *
 * @param onNavigateBack returns to the previous screen.
 * @param onNavigateToSupportScreen opens the support screen.
 * @param onNavigateToTutorialScreen opens the tutorial screen.
 * @param onNavigateToPrivacyScreen opens the privacy policy screen.
 * @param onNavigateToLicenseScreen opens the license screen.
 * @param onNavigateToDependenciesScreen opens the dependencies screen.
 */
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSupportScreen: () -> Unit,
    onNavigateToTutorialScreen: () -> Unit,
    onNavigateToPrivacyScreen: () -> Unit,
    onNavigateToLicenseScreen: () -> Unit,
    onNavigateToDependenciesScreen: () -> Unit,
) {
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

    val versionName = BuildConfig.VERSION_NAME

    val resolveAction: (AboutAction) -> (() -> Unit)? = { action ->
        when (action) {
            AboutAction.None -> null
            AboutAction.Tutorial -> onNavigateToTutorialScreen
            AboutAction.Privacy -> onNavigateToPrivacyScreen
            AboutAction.License -> onNavigateToLicenseScreen
            AboutAction.Dependencies -> onNavigateToDependenciesScreen
            is AboutAction.Url -> ({ url.value = resources.getString(action.urlRes) })
        }
    }

    LibreFitScaffold(
        title = AnnotatedString(stringResource(id = R.string.about)),
        navigateBack = onNavigateBack,
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding = innerPadding) {
            item(key = "header_logo") { AboutLogo() }
            item(key = "header_app_name") { AppNameText(MaterialTheme.typography.displayLargeEmphasized) }

            item(key = "header_version") {
                Text(stringResource(R.string.version_name, versionName))
            }

            item(key = "header_support") { AboutSupportButton(onClick = onNavigateToSupportScreen) }
            item(key = "header_warning") {
                AboutWarningButton(onClick = { infoMode.value = InfoMode.KEEP_ANDROID_OPEN })
            }

            aboutSections.forEach { (sectionTitle, entries) ->
                item(key = "section_$sectionTitle") {
                    HeadlineText(stringResource(sectionTitle))
                }

                items(entries, key = { entry -> "$sectionTitle:${entry.title}" }) { entry ->
                    AboutItem(
                        icon = painterResource(entry.icon),
                        text = stringResource(entry.title),
                        description = resolveAboutDescription(entry.description),
                        onClick = resolveAction(entry.action),
                    )
                }
            }
        }
    }
}

/** App icon shown at the top of the About screen. */
@Composable
private fun AboutLogo() {
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

/** Animated "let's build it together" support button with a pulsing radial-gradient border. */
@Composable
private fun AboutSupportButton(onClick: () -> Unit) {
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
        onClick = onClick,
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
                    outline = (if (isPressed) pressedShape else shape).createOutline(
                        size,
                        layoutDirection,
                        this
                    ),
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

/** Warning entry opening the "keep Android open" info sheet. */
@Composable
private fun AboutWarningButton(onClick: () -> Unit) {
    LibreFitButton(
        text = stringResource(R.string.librefit_is_under_threat),
        elevated = false,
        icon = painterResource(R.drawable.ic_warning),
        iconDescription = stringResource(R.string.librefit_is_under_threat)
    ) {
        onClick()
    }
}

/**
 * Resolves [parts] into one subtitle: the first element is a format pattern,
 * the remaining elements are its positional format arguments.
 *
 * @param parts string resources describing the row subtitle; empty renders as `""`.
 * @return the resolved, formatted subtitle.
 */
@Composable
private fun resolveAboutDescription(parts: List<Int>): String {
    if (parts.isEmpty()) return ""
    val pattern = parts.first()
    val args = parts.drop(1)
    // format only when there are arguments — never run a plain string through String.format,
    // so a literal '%' in any single-part description cannot throw
    if (args.isEmpty()) return stringResource(pattern)
    val formatArgs = args.map { stringResource(it) }.toTypedArray()
    return stringResource(pattern, *formatArgs)
}

/**
 * A single About row: a [Button] when [onClick] is provided, otherwise a non-interactive
 * [Surface] with the same shape, colors and content padding.
 *
 * @param modifier modifier applied to the row container.
 * @param icon drawable shown at the start of the row.
 * @param text row label.
 * @param description optional row subtitle; rendered only when not blank.
 * @param onClick activation handler, or `null` for a non-interactive row.
 */
@Composable
private fun AboutItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    text: String,
    description: String = "",
    onClick: (() -> Unit)? = null
) {
    if (onClick != null) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shapes = ButtonDefaults.shapes(
                shape = MaterialTheme.shapes.extraLarge
            ),
            contentPadding = ButtonDefaults.MediumContentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            AboutItemContent(icon = icon, text = text, description = description)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            AboutItemContent(
                modifier = Modifier.padding(ButtonDefaults.MediumContentPadding),
                icon = icon,
                text = text,
                description = description
            )
        }
    }
}

/** Icon, label and optional description content shared by both [AboutItem] variants. */
@Composable
private fun AboutItemContent(
    modifier: Modifier = Modifier,
    icon: Painter,
    text: String,
    description: String = ""
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
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

@Preview(locale = "en")
@Composable
private fun AboutScreenPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        AboutScreen(
            onNavigateBack = {},
            onNavigateToSupportScreen = {},
            onNavigateToTutorialScreen = {},
            onNavigateToPrivacyScreen = {},
            onNavigateToLicenseScreen = {},
            onNavigateToDependenciesScreen = {},
        )
    }
}