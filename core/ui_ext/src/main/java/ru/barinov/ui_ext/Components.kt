package ru.barinov.ui_ext

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InformationalBlock(
    type: InformationalBlockType,
    text: String,
    modifier: Modifier = Modifier,
    onBlockClicked: () -> Unit,
    onIconClicked: () -> Unit
) {
    val iconRes = when (type) {
        InformationalBlockType.INFO -> R.drawable.info
        InformationalBlockType.WARNING -> R.drawable.warning
        InformationalBlockType.ERROR -> R.drawable.warning
    }
    Box(modifier) {
        ElevatedCard(onClick = onBlockClicked) {
            Row(Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable { onIconClicked() },
                    tint = info
                )
                Text(text = text, fontSize = 14.sp, maxLines = 1)
            }
        }
    }
}


enum class InformationalBlockType {
    INFO, WARNING, ERROR
}

//Preview region

@Composable
@Preview(showBackground = true)
private fun InformationalBlockPreview() {
    MaterialTheme {
        InformationalBlock(
            type = InformationalBlockType.INFO,
            text = "Application requires permission for work",
            modifier = Modifier,
            onBlockClicked = {},
            onIconClicked = {}
        )
    }
}

@Composable
fun PasswordTextField(
    onValueChanged: (String) -> Unit,
    supportText: @Composable () -> Unit,
    modifier: Modifier
) {
    val enteredPass = remember {
        mutableStateOf("")
    }
    OutlinedTextField(
        value = enteredPass.value,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        onValueChange = {
            enteredPass.value = it
            onValueChanged(enteredPass.value)
        }, modifier = modifier,
        supportingText = { supportText() }
    )
}