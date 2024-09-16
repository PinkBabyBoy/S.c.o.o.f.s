package ru.barinov.protected_enter

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun PasswordResetAlertDialog(dismiss: () -> Unit, onConfirmed: () -> Unit) {
    AlertDialog(
        title = {
            Text(text = "Reset the password?")
        },
        text = {
            Text(text  = "All stored data will be removed")
        },
        onDismissRequest = { dismiss() },
        dismissButton = {
            Button(onClick = dismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            Button(onClick = onConfirmed) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        }
    )
}