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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.librefit.R
import org.librefit.enums.InfoMode
import org.librefit.ui.components.modalBottomSheets.InfoModalBottomSheet
import org.librefit.ui.theme.LibreFitTheme

/**
 * Displays a headline text with an optional information button.
 *
 * @param text The headline text to display.
 * @param infoMode If non-null, the  information is used by the [IconButton] when clicked
 * to open [InfoModalBottomSheet]
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HeadlineText(
    text: String,
    infoMode: InfoMode? = null
) {
    var localInfoMode by remember { mutableStateOf(InfoMode.DISMISS) }

    InfoModalBottomSheet(localInfoMode) { localInfoMode = InfoMode.DISMISS }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.primary
        )
        if (infoMode != null) {
            Spacer(Modifier.width(10.dp))

            IconButton(
                onClick = { localInfoMode = infoMode }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
private fun HeadlineTextPreview() {
    LibreFitTheme(dynamicColor = false, darkTheme = true) {
        LibreFitScaffold(title = AnnotatedString("HeadlineText showcase")) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    HeadlineText(
                        "Headline",
                        InfoMode.entries.filter { it != InfoMode.DISMISS }.random()
                    )
                }

                items((1..10).map { "Item $it" }) {
                    Card(Modifier.fillMaxWidth()) {
                        Text(it, Modifier.padding(15.dp))
                    }
                }
            }
        }
    }
}