package ru.barinov.file_browser.presentation.dialogs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import ru.barinov.core.R
import ru.barinov.core.hasNoSpecialSymbols
import ru.barinov.core.headerDefault
import ru.barinov.core.ui.PasswordTextField
import ru.barinov.core.ui.ProgressButton
import ru.barinov.core.ui.ScoofButton
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.core.ui.TextEnter
import ru.barinov.core.ui.bsContainerColor
import ru.barinov.file_browser.events.CreateContainerEvents
import ru.barinov.file_browser.events.KeyStoreCreateEvents
import ru.barinov.file_browser.events.LoadKeyStoreEvents
import ru.barinov.file_browser.events.OnDismiss
import ru.barinov.file_browser.sideEffects.BottomSheetSideEffects
import ru.barinov.file_browser.sideEffects.DismissConfirmed
import ru.barinov.file_browser.sideEffects.FilesLoadInitializationSideEffects
import ru.barinov.file_browser.sideEffects.ShowInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContainerBottomSheet(
    navController: NavController,
    sideEffectsFlow: Flow<BottomSheetSideEffects>,
    onEvent: (CreateContainerEvents) -> Unit,
) {
    val bsState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    SingleEventEffect(sideEffectsFlow) { sideEffect ->
        when (sideEffect) {
            DismissConfirmed -> navController.navigateUp()
            is ShowInfo -> TODO()
            FilesLoadInitializationSideEffects.CloseOnLongTransaction -> TODO()
            FilesLoadInitializationSideEffects.CloseOnShortTransaction -> TODO()
        }
    }
    ModalBottomSheet(
        onDismissRequest = {
            onEvent(OnDismiss)
            navController.navigateUp()
        },
        sheetState = bsState,
        containerColor = bsContainerColor
    ) {
        val nameInput = remember { mutableStateOf("") }
        val inputErrors = remember { mutableStateOf(emptySet<InputErrors>()) }
        Text(
            text = "Create container",
            style = headerDefault(),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))
        TextEnter(
            supportText = {
                SupportText(
                    errors = inputErrors.value.filter { it == InputErrors.NAME_EMPTY },
                    hint = R.string.container_name_hint
                )
            },
            onValueChanged = { nameInput.value = it },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        val isInProgress = remember { mutableStateOf(false) }
        Spacer(modifier = Modifier.height(32.dp))
        ProgressButton(
            isEnabled = !isInProgress.value,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(horizontal = 64.dp),
            isProgress = isInProgress,
            buttonText = ru.barinov.core.R.string.create,
        ) {
            when {
                nameInput.value.isEmpty() -> inputErrors.value = setOf(InputErrors.NAME_EMPTY)
                nameInput.value.contains('/') -> inputErrors.value =
                    setOf(InputErrors.HAS_SPECIAL_SYMBOLS)

                else -> {
                    isInProgress.value = true
                    onEvent(CreateContainerEvents.CreateContainer(nameInput.value))
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyStoreLoadBottomSheet(
    fileName: String,
    navController: NavController,
    sideEffectsFlow: Flow<BottomSheetSideEffects>,
    onEvent: (LoadKeyStoreEvents) -> Unit,
) {
    val bsState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    SingleEventEffect(sideEffectsFlow) { sideEffect ->
        when (sideEffect) {
            DismissConfirmed -> navController.navigateUp()
            is ShowInfo -> {
                //TODO SNACKBAR SHOW
            }

            FilesLoadInitializationSideEffects.CloseOnLongTransaction -> TODO()
            FilesLoadInitializationSideEffects.CloseOnShortTransaction -> TODO()
        }

    }
    ModalBottomSheet(
        onDismissRequest = { onEvent(OnDismiss) },
        modifier = Modifier.imePadding(),
        sheetState = bsState,
        containerColor = bsContainerColor
    ) {
        val enteredPassword = remember { mutableStateOf("") }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            style = headerDefault(),
            text = "Load keys from $fileName",
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
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(horizontal = 64.dp),
            onClick = {
                onEvent(LoadKeyStoreEvents.LoadKeyStore(enteredPassword.value.toCharArray()))
            },
            buttonText = ru.barinov.core.R.string.load
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateKeyStoreBottomSheet(
    navController: NavController,
    sideEffectsFlow: Flow<BottomSheetSideEffects>,
    onEvent: (KeyStoreCreateEvents) -> Unit
) {
    SingleEventEffect(sideEffectsFlow) { sideEffect ->
        when (sideEffect) {
            DismissConfirmed -> navController.navigateUp()
            is ShowInfo -> {
                //TODO SNACKBAR SHOW
            }
            FilesLoadInitializationSideEffects.CloseOnLongTransaction -> {
                navController.navigateUp()
            }
            FilesLoadInitializationSideEffects.CloseOnShortTransaction -> {
                navController.navigateUp()
            }
        }

    }
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
        onDismissRequest = { if (!loadStarted.value) onEvent(OnDismiss) },
        sheetState = bsState,
        properties = ModalBottomSheetDefaults.properties(shouldDismissOnBackPress = false),
        containerColor = bsContainerColor
    ) {
        Text(
            style = headerDefault(),
            text = "Create the keystore",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))

        TextEnter(
            supportText = {
                SupportText(
                    errors = inputErrors.value.filter { it == InputErrors.NAME_EMPTY },
                    hint = R.string.keystore_name_hint
                )
            },
            onValueChanged = { enteredName.value = it },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))

        PasswordTextField(
            onValueChanged = { passInput.value = it },
            supportText = {
                SupportText(
                    inputErrors.value.filter { it == InputErrors.PASSWORD_EMPTY },
                    R.string.password_enter_helper_text
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
                    onEvent(
                        KeyStoreCreateEvents.OnConfirmed(
                            enteredName.value,
                            passInput.value.toCharArray(),
                            checkState.value
                        )
                    )
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
