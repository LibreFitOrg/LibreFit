package org.librefit.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.librefit.R

@Composable
fun ConfirmExitDialog(
    text : String,
    onExit : () -> Unit,
    onDismiss : () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismiss ,
        confirmButton = {
            TextButton(
                onClick = onExit
            ){
                Text(text = stringResource(id = R.string.label_exit_dialog))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss ){
                Text(text = stringResource(id = R.string.label_cancel_dialog))
            }
        },
        text = { Text(text = text ) }
    )
}