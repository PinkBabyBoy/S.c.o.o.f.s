package ru.barinov.file_browser.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.barinov.core.Filename
import ru.barinov.file_browser.R
import ru.barinov.file_browser.events.KeySelectorEvent
import ru.barinov.ui_ext.BottomSheetPolicy
import ru.barinov.ui_ext.PasswordTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyStoreLoadBottomSheet(
    onDismissRequested: () -> Unit,
    onConfirmed: (String) -> Unit,
    filename: Filename
) {
    ModalBottomSheet(
        onDismissRequest = { onDismissRequested() }
    ) {
        val enteredPassword = remember { mutableStateOf("") }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            fontSize = 14.sp,
            text = "To load keys from ${filename.value}, please enter store's password",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = enteredPassword.value,
            onValueChange = { enteredPassword.value = it },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            onClick = {
                onConfirmed(enteredPassword.value)
                onDismissRequested()
            }
        ) {
            Text(text = "Load", modifier = Modifier.padding(horizontal = 32.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateKeyStoreBottomSheet(
    onDismissRequested: () -> Unit,
    onConfirmed: (String, CharArray, Boolean) -> Unit
) {
    val inputErrors = remember {
        mutableStateOf(emptySet<InputErrors>())
    }
    val enteredName = remember {
        mutableStateOf("")
    }
    val checkState = remember {
        mutableStateOf(true)
    }
    val passInput = remember { mutableStateOf(String()) }
    val loadStarted = remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = { if(!loadStarted.value) onDismissRequested() }) {
        Text(
            text = "Create the keystore", modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))
        //имя
        OutlinedTextField(
            supportingText = { SupportText(errors = inputErrors.value.filter { it == InputErrors.NAME_EMPTY }, hint = ru.barinov.ui_ext.R.string.keystore_name_hint)},
            value = enteredName.value, onValueChange = { enteredName.value = it },
            modifier =  Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))
        //пароль
        PasswordTextField(
            onValueChanged = { passInput.value = it },
            supportText = { SupportText(inputErrors.value.filter { it == InputErrors.PASSWORD_EMPTY }, ru.barinov.ui_ext.R.string.password_enter_helper_text) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedCard(Modifier.align(Alignment.CenterHorizontally)) {
            Row(Modifier.padding(12.dp)) {
                Text(text = "Load after create", Modifier.align(Alignment.CenterVertically))
                Spacer(modifier = Modifier.width(32.dp))
                Switch(
                    checked = checkState.value,
                    onCheckedChange = { checkState.value = it },
                    Modifier.align(Alignment.CenterVertically)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            enabled = !loadStarted.value,
            onClick = {
                if(enteredName.value.isNotEmpty() && passInput.value.isNotEmpty()) {
                    loadStarted.value = true
                    onConfirmed(enteredName.value, passInput.value.toCharArray(), checkState.value)
                } else {
                    inputErrors.value = buildSet{
                        if(enteredName.value.isEmpty()) add(InputErrors.NAME_EMPTY)
                        if(passInput.value.isEmpty()) add(InputErrors.PASSWORD_EMPTY)
                    }
                }
            },
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            if (!loadStarted.value)
                Text(text = "Create")
            else CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

    }
}

@Composable
private fun SupportText(errors: List<InputErrors>, @StringRes hint: Int) {
    if (errors.isEmpty()) {
        Text(text = stringResource(id = hint))
        return
    }
    val text = when (errors.first()) {
        InputErrors.NAME_EMPTY -> ru.barinov.ui_ext.R.string.empty_keystore_name
        InputErrors.PASSWORD_EMPTY -> ru.barinov.ui_ext.R.string.empty_password_text
    }.let { stringResource(id = it) }
    Text(text = text, color = MaterialTheme.colorScheme.error)
}

private enum class InputErrors {
    NAME_EMPTY, PASSWORD_EMPTY
}
