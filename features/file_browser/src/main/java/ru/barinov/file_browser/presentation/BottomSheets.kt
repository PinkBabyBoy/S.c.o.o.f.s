package ru.barinov.file_browser.presentation

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.barinov.core.Filename
import ru.barinov.core.hasNoSpecialSymbols
import ru.barinov.core.ui.PasswordTextField
import ru.barinov.core.ui.ProgressButton
import ru.barinov.core.ui.ScoofButton
import ru.barinov.core.ui.TextEnter
import ru.barinov.core.ui.bsContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContainerBottomSheet(
    onDismissRequested: () -> Unit,
    onConfirmed: (String) -> Unit
) {
    val bsState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onDismissRequested() },
        sheetState = bsState,
        containerColor = bsContainerColor
    ) {
        val nameInput = remember { mutableStateOf("") }
        val inputErrors = remember { mutableStateOf(emptySet<InputErrors>()) }
        Text(text = "Create container", modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(32.dp))
        TextEnter(
            supportText = {
                SupportText(
                    errors = inputErrors.value.filter { it == InputErrors.NAME_EMPTY },
                    hint = ru.barinov.core.R.string.container_name_hint
                )
            },
            onValueChanged = { nameInput.value = it },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        val isInProgress = remember { mutableStateOf(false) }
        ProgressButton(
            isEnabled = !isInProgress.value,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally),
            isProgress = isInProgress,
            buttonText = ru.barinov.core.R.string.create,
        ) {
            when {
                nameInput.value.isEmpty() -> inputErrors.value = setOf(InputErrors.NAME_EMPTY)
                nameInput.value.contains('/') -> inputErrors.value =
                    setOf(InputErrors.HAS_SPECIAL_SYMBOLS)
                else -> {
                    isInProgress.value = true
                    onConfirmed(nameInput.value)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyStoreLoadBottomSheet(
    onDismissRequested: () -> Unit,
    onConfirmed: (String) -> Unit,
    filename: Filename
) {
    val bsState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onDismissRequested() },
        modifier = Modifier.imePadding(),
        sheetState = bsState,
        containerColor = bsContainerColor
    ) {
        val enteredPassword = remember { mutableStateOf("") }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            fontSize = 14.sp,
            text = "Load keys from ${filename.value}",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        PasswordTextField(
            onValueChanged = { enteredPassword.value = it },
            supportText = { Text(text = stringResource(id = ru.barinov.core.R.string.password_enter_helper_text)) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))
        ScoofButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp),
            onClick = {
                onConfirmed(enteredPassword.value)
                onDismissRequested()
            },
            buttonText = ru.barinov.core.R.string.load
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
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
    val bsState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { !loadStarted.value }
    )

    ModalBottomSheet(
        onDismissRequest = { if (!loadStarted.value) onDismissRequested() },
        sheetState = bsState,
        properties = ModalBottomSheetDefaults.properties(shouldDismissOnBackPress = false),
        containerColor = bsContainerColor
    ) {
        Text(text = "Create the keystore", modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(32.dp))
        //имя
        TextEnter(
            supportText = {
                SupportText(
                    errors = inputErrors.value.filter { it == InputErrors.NAME_EMPTY },
                    hint = ru.barinov.core.R.string.keystore_name_hint
                )
            },
            onValueChanged = { enteredName.value = it },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))
        //пароль
        PasswordTextField(
            onValueChanged = { passInput.value = it },
            supportText = {
                SupportText(
                    inputErrors.value.filter { it == InputErrors.PASSWORD_EMPTY },
                    ru.barinov.core.R.string.password_enter_helper_text
                )
            },
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
        ProgressButton(
            isEnabled = !loadStarted.value,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally),
            isProgress = loadStarted,
            buttonText = ru.barinov.core.R.string.create,
        ) {
            if (enteredName.value.isNotEmpty() && passInput.value.isNotEmpty()) {
                if (enteredName.value.hasNoSpecialSymbols()) {
                    loadStarted.value = true
                    onConfirmed(enteredName.value, passInput.value.toCharArray(), checkState.value)
                } else inputErrors.value = setOf(InputErrors.HAS_SPECIAL_SYMBOLS)
            } else {
                inputErrors.value = buildSet {
                    if (enteredName.value.isEmpty()) add(InputErrors.NAME_EMPTY)
                    if (passInput.value.isEmpty()) add(InputErrors.PASSWORD_EMPTY)
                }
            }
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
        InputErrors.NAME_EMPTY -> ru.barinov.core.R.string.empty_keystore_name
        InputErrors.PASSWORD_EMPTY -> ru.barinov.core.R.string.empty_password_text
        InputErrors.HAS_SPECIAL_SYMBOLS -> TODO()
    }.let { stringResource(id = it) }
    Text(text = text, color = MaterialTheme.colorScheme.error)
}

private enum class InputErrors {
    NAME_EMPTY, PASSWORD_EMPTY, HAS_SPECIAL_SYMBOLS
}
