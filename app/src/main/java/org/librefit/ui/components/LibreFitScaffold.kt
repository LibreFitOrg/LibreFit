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

package org.librefit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import org.librefit.R


/**
 * A reusable [Scaffold] component designed for multiple screens to minimize boilerplate code
 * and maintain a consistent design across the application.
 *
 * @param title The title displayed in the [TopAppBar]. By default the title isn't shown
 * @param navigateBack A callback function invoked when the navigation icon in the [TopAppBar] is clicked.
 * This should typically trigger back navigation. By default the back navigation icon is not displayed
 * @param actions A list of callback functions executed when the relative action button in the [TopAppBar] is clicked.
 * It must be passed in order to show the action button.
 * @param actionsEnabled A list of booleans that controls whether the relative action button is enabled or disabled.
 * The default value is `true`.
 * @param actionsDescription A list of strings that provides a description of the relative action.
 * It should not be passed along with an icon from [actionsIcons].
 * @param actionsIcons A list of icons to be displayed inside the relative action button.
 * It should not be passed along with a description from [actionsDescription].
 * @param actionsElevated A list of booleans that controls the color elevation of the relative action button.
 * The default value is `true`.
 * @param fabAction A callback function executed when the [FloatingActionButton] is clicked. It
 * must be passed in order to show the FAB.
 * @param fabIcon A [Painter] representing the icon displayed in the [FloatingActionButton]. It
 * must be passed in order to show the FAB.
 * @param fabDescription An optional string that provides a description of the [fabIcon] and [fabAction]
 * for accessibility purposes. Read mode at [Icon] and [FloatingActionButton]
 * @param bottomBar The bottom bar of the scaffold. By default there's no bottom bar.
 * @param content A composable lambda that defines the main content of the screen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibreFitScaffold(
    title: AnnotatedString? = null,
    navigateBack: (() -> Unit)? = null,
    actions: List<() -> Unit> = listOf(),
    actionsEnabled: List<Boolean> = listOf(),
    actionsDescription: List<String?> = listOf(),
    actionsIcons: List<Painter> = listOf(),
    actionsElevated: List<Boolean> = listOf(),
    fabAction: (() -> Unit)? = null,
    fabIcon: Painter? = null,
    fabDescription: String? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable ((PaddingValues) -> Unit),
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            if (title != null || navigateBack != null || actions.isNotEmpty()) {
                TopAppBar(
                    title = {
                        if (title != null) {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        if (navigateBack != null) {
                            IconButton(
                                onClick = navigateBack,
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_back),
                                    contentDescription = stringResource(id = R.string.navigate_back)
                                )
                            }
                        }
                    },
                    actions = {
                        val haptic = LocalHapticFeedback.current

                        val interactionSources =
                            remember { List(actions.size) { MutableInteractionSource() } }
                        ButtonGroup(
                            overflowIndicator = {},
                        ) {
                            actions.forEachIndexed { index, action ->
                                val description = actionsDescription.getOrNull(index)

                                val icon = actionsIcons.getOrNull(index)

                                val enabled = actionsEnabled.getOrNull(index) != false

                                val elevated = actionsElevated.getOrNull(index) != false

                                customItem(
                                    buttonGroupContent = {
                                        if (icon != null) {
                                            IconButton(
                                                modifier = Modifier.animateWidth(interactionSources[index]),
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                                    action()
                                                },
                                                shapes = IconButtonDefaults.shapes(),
                                                interactionSource = interactionSources[index],
                                                enabled = enabled,
                                                colors = if (elevated)
                                                    IconButtonDefaults.filledIconButtonColors() else
                                                    IconButtonDefaults.iconButtonColors()
                                            ) {
                                                Icon(
                                                    painter = icon,
                                                    contentDescription = description
                                                )
                                            }
                                        }
                                        if (description != null) {
                                            Button(
                                                modifier = Modifier.animateWidth(interactionSources[index]),
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                                    action()
                                                },
                                                shapes = ButtonDefaults.shapes(),
                                                interactionSource = interactionSources[index],
                                                enabled = enabled,
                                                colors = if (elevated)
                                                    ButtonDefaults.buttonColors() else
                                                    ButtonDefaults.textButtonColors()
                                            ) {
                                                Text(text = description)

                                            }
                                        }
                                    },
                                    menuContent = {}
                                )

                            }
                        }

                    },
                )
            }
        },
        floatingActionButton = {
            if (fabIcon != null) {
                AnimatedVisibility(
                    visible = fabAction != null,
                    enter = slideInHorizontally(initialOffsetX = { it }),
                    exit = slideOutHorizontally(targetOffsetX = { it })
                ) {
                    FloatingActionButton(
                        onClick = { fabAction?.invoke() }
                    ) {
                        Icon(
                            painter = fabIcon,
                            contentDescription = fabDescription
                        )
                    }
                }
            }
        },
        bottomBar = { bottomBar?.invoke() }
    ) {
        content(it)
    }
}