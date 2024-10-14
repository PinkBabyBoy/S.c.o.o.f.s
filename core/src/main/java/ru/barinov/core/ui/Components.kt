package ru.barinov.core.ui


import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.barinov.core.R
import ru.barinov.core.headerDefault

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
        ElevatedCard(onClick = onBlockClicked, colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
            Row(Modifier.padding(vertical = 8.dp, horizontal = 16.dp).fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable { onIconClicked() },
                    tint = resolveColor(type)
                )
                Text(text = text, style = headerDefault(), maxLines = 1)
            }
        }
    }
}

private fun resolveColor(type: InformationalBlockType) = when(type) {
    InformationalBlockType.INFO -> info
    InformationalBlockType.WARNING -> warning
    InformationalBlockType.ERROR -> error
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
fun TextEnter(
    onValueChanged: (String) -> Unit,
    supportText: @Composable () -> Unit,
    isError: Boolean = false,
    modifier: Modifier
) {
    val enteredText = remember { mutableStateOf(String()) }
    OutlinedTextField(
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors().copy(
            cursorColor = darkGreen,
            unfocusedIndicatorColor = darkGreen,
            focusedIndicatorColor = darkGreen
        ),
        supportingText = { supportText() },
        value = enteredText.value,
        onValueChange = {
            enteredText.value = it
            onValueChanged(it)
        },
        modifier = modifier
    )
}

@Composable
fun PasswordTextField(
    onValueChanged: (String) -> Unit,
    supportText: @Composable () -> Unit,
    isError: Boolean = false,
    modifier: Modifier
) {
    val enteredPass = remember {
        mutableStateOf("")
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors().copy(
            cursorColor = darkGreen,
            unfocusedIndicatorColor = darkGreen,
            focusedIndicatorColor = darkGreen
        ),
        isError = isError,
        value = enteredPass.value,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() }),
        onValueChange = {
            enteredPass.value = it
            onValueChanged(enteredPass.value)
        }, modifier = modifier,
        supportingText = { supportText() }
    )
}

@Composable
fun ProgressButton(
    isEnabled: Boolean = true,
    modifier: Modifier,
    isProgress: State<Boolean>,
    @StringRes buttonText: Int,
    onClick: () -> Unit
) {
    ElevatedButton(
        colors = ButtonDefaults.buttonColors().copy(containerColor = mainGreen),
        onClick = { onClick() },
        modifier = modifier,
        enabled = isEnabled
    ) {
        if (!isProgress.value) {
            Text(text = stringResource(id = buttonText), fontSize = 22.sp)
        } else {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ScoofButton(
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    @StringRes buttonText: Int,
    onClick: () -> Unit
) {
    ElevatedButton(
        colors = ButtonDefaults.buttonColors().copy(containerColor = mainGreen),
        onClick = { onClick() },
        modifier = modifier,
        enabled = isEnabled
    ) {
        Text(text = stringResource(id = buttonText), fontSize = 18.sp)
    }
}

@Composable
fun ScoofAlertDialog(
    title: String,
    message: String,
    onConfirmed: () -> Unit,
    onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = title)
        },
        text = {
            Text(text  = message)
        },
        dismissButton = {
            ScoofButton(
                buttonText = android.R.string.cancel,
                onClick = onDismissRequest
            )
        },
        confirmButton = {
            ScoofButton(
                buttonText = android.R.string.ok,
                onClick = onConfirmed
            )
        }
    )
}