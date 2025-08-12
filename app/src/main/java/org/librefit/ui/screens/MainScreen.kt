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

package org.librefit.ui.screens


import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavHostController
import org.librefit.R
import org.librefit.nav.Route
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.screens.home.HomeScreen
import org.librefit.ui.screens.profile.ProfileScreen


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MainScreen(
    navController: NavHostController,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var homeSelected by rememberSaveable { mutableStateOf(true) }

    val fabAction: () -> Unit = {
        navController.navigate(Route.EditWorkoutScreen(0L))
    }


    LibreFitScaffold(
        title = buildAnnotatedString {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(stringResource(id = R.string.app_name).removeRange(5, 8))
            }
            append(stringResource(id = R.string.app_name).removeRange(0, 5))
        },
        actions = listOf(
            { navController.navigate(Route.AboutScreen) },
            { navController.navigate(Route.SettingsScreen) }
        ),
        actionsIcons = listOf(
            ImageVector.vectorResource(R.drawable.ic_info),
            ImageVector.vectorResource(R.drawable.ic_settings)
        ),
        actionsElevated = listOf(false, false),
        fabAction = if (homeSelected) fabAction else null,
        fabIcon = ImageVector.vectorResource(R.drawable.ic_add),
        fabDescription = stringResource(R.string.create_routine),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = homeSelected,
                    onClick = { homeSelected = true },
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_home),
                            contentDescription = stringResource(R.string.home)
                        )
                    },
                    label = { Text(stringResource(R.string.home)) }
                )
                NavigationBarItem(
                    selected = !homeSelected,
                    onClick = { homeSelected = false },
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_person),
                            contentDescription = stringResource(R.string.profile)
                        )
                    },
                    label = { Text(stringResource(R.string.profile)) }
                )
            }
        }
    ) { innerPadding ->
        if (homeSelected)
            HomeScreen(innerPadding, navController, animatedVisibilityScope)
        else ProfileScreen(innerPadding, navController, animatedVisibilityScope)
    }
}