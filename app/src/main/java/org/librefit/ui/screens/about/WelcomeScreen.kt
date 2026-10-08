/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.screens.about

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.librefit.R
import org.librefit.enums.userPreferences.DialogPreference
import org.librefit.enums.userPreferences.ThemeMode
import org.librefit.enums.userPreferences.UnitSystem
import org.librefit.ui.components.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.LibreFitButton
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.morphShape.AnimatedMorphShapes
import org.librefit.ui.screens.shared.SharedViewModel
import org.librefit.ui.theme.LibreFitTheme
import org.librefit.util.Formatter

/**
 * First-launch welcome screen.
 *
 * In addition to introducing the app, it offers a compact, non-blocking personalization step
 * (unit system, theme, language) following Material's Self-Select onboarding model: all choices
 * are optional, apply instantly, and have sensible locale/system defaults.
 *
 * @param onNavigateToTutorialScreen invoked when the user chooses to open the tutorial.
 * @param onNavigateToMainScreen invoked when the user chooses to enter the app.
 * @param doNotShowWelcomeScreenAgain invoked right before navigating away, so the screen is not
 *   shown again on the next launch.
 * @param sharedViewModel shared app-level ViewModel exposing the user preferences shown here.
 */
@Composable
fun WelcomeScreen(
    onNavigateToTutorialScreen: () -> Unit,
    onNavigateToMainScreen: () -> Unit,
    doNotShowWelcomeScreenAgain: () -> Unit,
    sharedViewModel: SharedViewModel,
) {
    val unitSystem by sharedViewModel.unitSystem.collectAsStateWithLifecycle()

    val themeMode by sharedViewModel.themeMode.collectAsStateWithLifecycle()

    WelcomeScreenContent(
        unitSystem = unitSystem,
        themeMode = themeMode,
        onUnitSystemChange = sharedViewModel::saveUnitSystem,
        onThemeModeChange = sharedViewModel::saveThemeMode,
        onNavigateToTutorialScreen = onNavigateToTutorialScreen,
        onNavigateToMainScreen = onNavigateToMainScreen,
        doNotShowWelcomeScreenAgain = doNotShowWelcomeScreenAgain,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WelcomeScreenContent(
    unitSystem: UnitSystem,
    themeMode: ThemeMode,
    onUnitSystemChange: (UnitSystem) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onNavigateToTutorialScreen: () -> Unit,
    onNavigateToMainScreen: () -> Unit,
    doNotShowWelcomeScreenAgain: () -> Unit
) {
    /**
     * Only used to preview animation in Android Studio
     */
    val animatedColor by animateColorAsState(
        targetValue = Color.Unspecified,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        label = "ColorAnimation"
    )
    LibreFitScaffold { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            if (maxHeight > maxWidth) {
                LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier
                        .width(maxWidth)
                        .height(maxHeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    welcomeScreenContent(
                        unitSystem = unitSystem,
                        themeMode = themeMode,
                        onUnitSystemChange = onUnitSystemChange,
                        onThemeModeChange = onThemeModeChange,
                        onNavigateToTutorialScreen = onNavigateToTutorialScreen,
                        onNavigateToMainScreen = onNavigateToMainScreen,
                        doNotShowWelcomeScreenAgain = doNotShowWelcomeScreenAgain
                    )
                }
            } else {
                LazyRow(
                    contentPadding = innerPadding,
                    modifier = Modifier
                        .width(maxWidth)
                        .height(maxHeight),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    welcomeScreenContent(
                        unitSystem = unitSystem,
                        themeMode = themeMode,
                        portraitMode = false,
                        onUnitSystemChange = onUnitSystemChange,
                        onThemeModeChange = onThemeModeChange,
                        onNavigateToTutorialScreen = onNavigateToTutorialScreen,
                        onNavigateToMainScreen = onNavigateToMainScreen,
                        doNotShowWelcomeScreenAgain = doNotShowWelcomeScreenAgain
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.welcomeScreenContent(
    unitSystem: UnitSystem,
    themeMode: ThemeMode,
    portraitMode: Boolean = true,
    onUnitSystemChange: (UnitSystem) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onNavigateToTutorialScreen: () -> Unit,
    onNavigateToMainScreen: () -> Unit,
    doNotShowWelcomeScreenAgain: () -> Unit
) {
    item {
        val polygons = remember {
            listOf(
                MaterialShapes.SoftBurst,
                MaterialShapes.Cookie6Sided,
                MaterialShapes.Pentagon,
                MaterialShapes.Pill,
                MaterialShapes.Diamond,
                MaterialShapes.Slanted,
                MaterialShapes.Gem,
                MaterialShapes.Oval,
            ).shuffled()
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedMorphShapes(
                morphIntervalMillis = 6000,
                globalRotationDurationMillis = 12000,
                colors = listOf(colorResource(R.color.ic_launcher_background)),
                shapeSize = 400.dp,
                roundedPolygons = polygons,
                onColorUpdate = {}
            )
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(220.dp)
            )
        }
    }
    item {
        val haptic = LocalHapticFeedback.current

        var showInitialSetup by rememberSaveable { mutableStateOf(true) }

        ElevatedCard(
            modifier = if (portraitMode) Modifier.padding(start = 12.dp, end = 12.dp) else Modifier,
            shape = MaterialTheme.shapes.extraExtraLarge
        ) {
            AnimatedContent(showInitialSetup) { show ->
                Column(
                    modifier = Modifier.padding((if (show) 24 else 40).dp),
                    verticalArrangement = Arrangement.spacedBy((if (show) 18 else 60).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (show) {
                        Text(
                            text = stringResource(R.string.make_it_yours),
                            style = MaterialTheme.typography.headlineSmallEmphasized,
                            textAlign = TextAlign.Center
                        )


                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.unit_system),
                                style = MaterialTheme.typography.labelLarge
                            )
                            SingleSelectConnectedButtonGroup(
                                options = UnitSystem.entries,
                                isSelected = { it == unitSystem },
                                icons = persistentListOf(
                                    painterResource(R.drawable.ic_weight_kg),
                                    painterResource(R.drawable.ic_weight_lb)
                                ),
                                onSelect = {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    onUnitSystemChange(it)
                                }
                            )
                        }


                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.theme),
                                style = MaterialTheme.typography.labelLarge
                            )
                            SingleSelectConnectedButtonGroup(
                                options = ThemeMode.entries,
                                isSelected = { it == themeMode },
                                icons = persistentListOf(
                                    painterResource(R.drawable.ic_light_mode_auto),
                                    painterResource(R.drawable.ic_light_mode),
                                    painterResource(R.drawable.ic_dark_mode),
                                ),
                                onSelect = {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    onThemeModeChange(it)
                                }
                            )
                        }

                        LibreFitButton(
                            text = stringResource(R.string.done)
                        ) {
                            showInitialSetup = false
                        }

                    } else {
                        Text(
                            text = buildAnnotatedString {
                                append(stringResource(R.string.welcome_to))
                                appendLine()
                                GetAppNameInAnnotatedBuilder(MaterialTheme.typography.displayLargeEmphasized)
                            },
                            style = MaterialTheme.typography.displayMedium,
                            textAlign = TextAlign.Center,
                        )
                        Row(
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val interactionSources =
                                remember { List(2) { MutableInteractionSource() } }

                            ButtonGroup(
                                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                                overflowIndicator = {}
                            ) {
                                customItem(
                                    buttonGroupContent = {
                                        LibreFitButton(
                                            icon = painterResource(R.drawable.ic_help),
                                            modifier = Modifier
                                                .animateWidth(interactionSources[0]),
                                            text = stringResource(R.string.tutorial),
                                            interactionSource = interactionSources[0]
                                        ) {
                                            onNavigateToTutorialScreen()
                                            doNotShowWelcomeScreenAgain()
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType.ContextClick
                                            )
                                        }
                                    },
                                    menuContent = {}
                                )
                                customItem(
                                    buttonGroupContent = {
                                        LibreFitButton(
                                            icon = painterResource(R.drawable.ic_home),
                                            modifier = Modifier
                                                .animateWidth(interactionSources[1]),
                                            text = stringResource(R.string.home),
                                            interactionSource = interactionSources[1],
                                            elevated = false
                                        ) {
                                            onNavigateToMainScreen()
                                            doNotShowWelcomeScreenAgain()
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType.ContextClick
                                            )
                                        }
                                    },
                                    menuContent = {}
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}

/**
 * A single-select connected button group (M3 Expressive), the official replacement for the
 * deprecated segmented button: a [FlowRow] of [ToggleButton]s with connected leading/middle/
 * trailing shapes and [Role.RadioButton] semantics.
 *
 * @param options selectable options, rendered in order.
 * @param isSelected whether an option is currently checked.
 * @param onSelect invoked with the tapped option.
 * @param icons optional icon shown on every button.
 * @param label composable label for an option; defaults to its localized preference name.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
private fun <T : DialogPreference> SingleSelectConnectedButtonGroup(
    options: List<T>,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
    icons: ImmutableList<Painter?> = persistentListOf(),
    label: @Composable (T) -> Unit = {
        Text(text = stringResource(Formatter.preferenceToStringId(it)))
    },
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            ButtonGroupDefaults.ConnectedSpaceBetween,
            Alignment.CenterHorizontally
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, option ->
            OutlinedToggleButton(
                checked = isSelected(option),
                onCheckedChange = { checked -> if (checked) onSelect(option) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                modifier = Modifier.semantics { role = Role.RadioButton }
            ) {
                val icon = icons.getOrNull(index)
                if (icon != null) {
                    Icon(painter = icon, contentDescription = null)
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                }
                label(option)
            }
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    LibreFitTheme(dynamicColor = false, themeMode = ThemeMode.DARK) {
        WelcomeScreenContent(
            unitSystem = UnitSystem.METRIC,
            themeMode = ThemeMode.DARK,
            onUnitSystemChange = {},
            onThemeModeChange = {},
            onNavigateToTutorialScreen = {},
            onNavigateToMainScreen = {},
            doNotShowWelcomeScreenAgain = {}
        )
    }
}