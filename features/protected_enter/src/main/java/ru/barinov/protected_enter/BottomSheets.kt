package ru.barinov.protected_enter

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp


@Composable
fun UsbInfoBottomSheet(
    onDismissRequested: () -> Unit
) {
    InfoBottomSheet(ru.barinov.ui_ext.R.string.usb_static_text, onDismissRequested)
}

@Composable
fun PermissionInfoBottomSheet(
    onDismissRequested: () -> Unit
) {
    InfoBottomSheet(ru.barinov.ui_ext.R.string.permission_static_text, onDismissRequested)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoBottomSheet(@StringRes text: Int, onDismissRequested: () -> Unit) {
    ModalBottomSheet(
        containerColor = Color.White,
        onDismissRequest = { onDismissRequested() }
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = text), Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 18.dp))
        Spacer(modifier = Modifier.height(16.dp))
        //Illustration
    }
}