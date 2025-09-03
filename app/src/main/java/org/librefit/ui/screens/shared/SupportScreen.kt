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
 */

package org.librefit.ui.screens.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.librefit.R
import org.librefit.ui.components.LibreFitAppName.GetAppNameInAnnotatedBuilder
import org.librefit.ui.components.LibreFitLazyColumn
import org.librefit.ui.components.LibreFitScaffold
import org.librefit.ui.components.animations.PulsingHeartLottie
import org.librefit.ui.theme.LibreFitTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SupportScreen(
    navHostController: NavHostController
) {

    LibreFitScaffold(
        navigateBack = navHostController::navigateUp
    ) { innerPadding ->
        LibreFitLazyColumn(innerPadding = innerPadding) {
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
                    },
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
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
                    onClick = {},
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
                    onClick = {},
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
        }
    }
}

@Preview
@Composable
private fun SupportScreenPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        SupportScreen(
            navHostController = rememberNavController()
        )
    }
}