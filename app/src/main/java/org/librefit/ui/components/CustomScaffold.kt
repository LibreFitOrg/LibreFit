/*
 * Copyright (c) 2024. LibreFit
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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.librefit.R


/**
 * A reusable [Scaffold] component designed for multiple screens to minimize boilerplate code
 * and maintain a consistent design across the application.
 *
 * @param title The title displayed in the [TopAppBar].
 * @param navigateBack A callback function invoked when the navigation icon in the [TopAppBar] is clicked.
 *                     This should typically handle back navigation.
 * @param action A callback function executed when the action button in the [TopAppBar] is clicked.
 * @param actionEnabled A Boolean that controls whether the action button is enabled or disabled.
 * @param actionIcon An optional [ImageVector] representing the icon displayed in the action button.
 * @param elevatedActionIcon A Boolean that, when true, applies elevated colors to the [actionIcon].
 * @param actionDescription An optional string that provides a description of the action for accessibility purposes.
 * @param fabAction A callback function executed when the [FloatingActionButton] is clicked.
 * @param fabIcon An optional [ImageVector] representing the icon displayed in the [FloatingActionButton].
 * @param fabDescription An optional string that provides a description of the [fabIcon] and [fabAction]
 * for accessibility purposes. Read mode at [Icon] and [FloatingActionButton]
 * @param content A composable lambda that defines the main content of the screen. It receives a
 * [PaddingValues] parameter that should be applied to the content root using [Modifier.padding]
 * and [Modifier.consumeWindowInsets] to properly offset the top and bottom bars.
 * If using [Modifier.verticalScroll], ensure this modifier is applied to the child of the
 * scrollable content, not the scrollable container itself.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomScaffold(
    title: String,
    navigateBack: () -> Unit,
    action: () -> Unit = {},
    actionEnabled: Boolean = true,
    actionIcon: ImageVector? = null,
    elevatedActionIcon: Boolean = false,
    actionDescription: String? = null,
    fabAction: () -> Unit = {},
    fabIcon: ImageVector? = null,
    fabDescription: String? = null,
    content: @Composable ((PaddingValues) -> Unit),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = title)
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    if (actionIcon != null) {
                        IconButton(
                            onClick = action,
                            enabled = actionEnabled,
                            colors = if (elevatedActionIcon) IconButtonDefaults.filledIconButtonColors() else IconButtonDefaults.iconButtonColors()
                        ) {
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = actionDescription
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (fabIcon != null) {
                FloatingActionButton(
                    onClick = fabAction
                ) {
                    Icon(
                        imageVector = fabIcon,
                        contentDescription = fabDescription
                    )
                }
            }
        }
    ) {
        content(it)
        Spacer(Modifier.height(100.dp))
    }
}