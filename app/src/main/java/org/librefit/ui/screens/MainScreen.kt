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


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.nav.Destination
import org.librefit.ui.components.CustomScaffold
import org.librefit.ui.screens.home.HomeScreen
import org.librefit.ui.screens.profile.ProfileScreen
import org.librefit.ui.screens.shared.SharedViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    var homeSelected by rememberSaveable { mutableStateOf(true) }


    CustomScaffold(
        title = buildAnnotatedString {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(stringResource(id = R.string.app_name).removeRange(5, 8))
            }
            append(stringResource(id = R.string.app_name).removeRange(0, 5))
        },
        actions = listOf { navController.navigate(Destination.SettingsScreen) },
        actionsIcons = listOf(Icons.Default.Settings),
        actionsElevated = listOf(false),
        fabAction = {
            sharedViewModel.updateWorkoutId(0)
            navController.navigate(Destination.EditWorkoutScreen)
        },
        fabIcon = Icons.Default.Add,
        fabDescription = stringResource(R.string.create_routine),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = homeSelected,
                    onClick = { homeSelected = true },
                    icon = {
                        Icon(
                            imageVector = if (homeSelected) Icons.Default.Home else Icons.Outlined.Home,
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
                            imageVector = if (!homeSelected) Icons.Default.Person else Icons.Outlined.Person,
                            contentDescription = stringResource(R.string.profile)
                        )
                    },
                    label = { Text(stringResource(R.string.profile)) }
                )
            }
        }
    ) { innerPadding ->
        if (homeSelected)
            HomeScreen(innerPadding, navController, sharedViewModel)
        else ProfileScreen(innerPadding, navController, sharedViewModel)
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen(rememberNavController(), viewModel())
}

