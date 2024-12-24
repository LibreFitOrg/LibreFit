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

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.librefit.R

@Composable
fun ConfirmDialog(
    title: String,
    text : String,
    onConfirm: () -> Unit,
    onDismiss : () -> Unit
){
    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = text) },
        onDismissRequest = onDismiss ,
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ){
                Text(text = stringResource(R.string.ok_dialog))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss ){
                Text(text = stringResource(id = R.string.cancel_dialog))
            }
        }
    )
}