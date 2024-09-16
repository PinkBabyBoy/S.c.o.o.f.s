package ru.barinov.protected_enter

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.barinov.permission_manager.Permission
import ru.barinov.permission_manager.PermissionRequestManager
import ru.barinov.routes.EnterScreenRoute
import ru.barinov.ui_ext.InformationalBlock
import ru.barinov.ui_ext.InformationalBlockType
import ru.barinov.ui_ext.RegisterLifecycleCallbacks
import ru.barinov.ui_ext.SingleEventEffect
import ru.barinov.ui_ext.enterScreenBackground

@Composable
internal fun EnterScreen(
    state: EnterScreenUiState,
    sideEffects: Flow<SideEffects>,
    enterScreenEvent: (EnterScreenEvent) -> Unit,
    rebase: () -> Unit
) {
    val context = LocalContext.current
    val pass: MutableState<CharSequence> = remember { mutableStateOf("") }
    val confirm: MutableState<CharSequence> = remember { mutableStateOf("") }
    val permissionManager = remember { PermissionRequestManager() }.apply {
        register(
            Permission.MANAGE_FILES,
            PermissionRequestManager.PermissionLauncherBuilder(context).build(
                Permission.MANAGE_FILES
            ) { granted ->
                if (granted) {
                    enterScreenEvent(EnterScreenEvent.PermissionGranted)
                }
            }
        )
    }
    RegisterLifecycleCallbacks(
        onDestroy = { permissionManager.close() }
    )
    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            SideEffects.AskPermission -> permissionManager.launch(Permission.MANAGE_FILES)
            SideEffects.EnterGranted -> rebase()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(enterScreenBackground)
    ) {
        Box {
            AnimatedLogo(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 128.dp)
            )
        }

        OutlinedTextField(
            value = pass.value.toString(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            onValueChange = {
                pass.value = it
                enterScreenEvent(EnterScreenEvent.NewInput(InputType.PASSWORD, it))
            }, modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 84.dp),
            supportingText = {
                SupportText(
                    errors = state.errors.filter { it != ErrorType.CHECK_EMPTY && it != ErrorType.CREATE_NOT_EQUALS },
                    hint = R.string.password_enter_helper_text
                )
            }
        )
        if (state.type == Stage.Enter) {
            val alertDialogVisible = remember {
                mutableStateOf(false)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Forgot your password",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { alertDialogVisible.value = true }
            )
            if (alertDialogVisible.value) {
                PasswordResetAlertDialog(
                    onConfirmed = {
                        enterScreenEvent(EnterScreenEvent.ResetConfirmed)
                        alertDialogVisible.value = false
                    },
                    dismiss = { alertDialogVisible.value = false }
                )
            }
        }
        if (state.type == Stage.Create) {
            OutlinedTextField(
                value = confirm.value.toString(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                onValueChange = {
                    confirm.value = it
                    enterScreenEvent(EnterScreenEvent.NewInput(InputType.CONFIRMATION, it))
                }, modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 64.dp),
                supportingText = {
                    SupportText(
                        errors = state.errors.filter { it == ErrorType.CHECK_EMPTY || it == ErrorType.CREATE_NOT_EQUALS },
                        hint = R.string.check_password_helper_text
                    )
                }
            )
        } else {
            Spacer(modifier = Modifier.height(128.dp))
        }

        if (state.hasPermission) {
            ElevatedButton(
                onClick = { enterScreenEvent(EnterScreenEvent.SubmitClicked) },
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 84.dp)
                    .defaultMinSize(minWidth = 228.dp)
            ) {
                Text(text = stringResource(id = R.string.enter_text), fontSize = 22.sp)
            }
        } else {
            InformationalBlock(
                modifier = Modifier
                    .padding(top = 84.dp)
                    .align(Alignment.CenterHorizontally),
                type = InformationalBlockType.INFO,
                text = stringResource(id = R.string.permission_warning_title)
            ) {
                permissionManager.launch(Permission.MANAGE_FILES)
            }
        }

    }


}

@Composable
private fun SupportText(errors: List<ErrorType>, @StringRes hint: Int) {
    if (errors.isEmpty()) {
        Text(text = stringResource(id = hint))
        return
    }
    val text = when (errors.first()) {
        ErrorType.READ_HASH_ERROR -> R.string.empty_password_text
        ErrorType.WRONG_PASSWORD -> R.string.wrong_password_err_text
        ErrorType.CREATE_NOT_EQUALS -> R.string.not_equals_passwords_err_text
        ErrorType.PASSWORD_EMPTY -> R.string.empty_password_text
        ErrorType.CHECK_EMPTY -> R.string.empty_check_password_text
    }.let { stringResource(id = it) }
    Text(text = text, color = MaterialTheme.colorScheme.error)
}

@Composable
@Preview(showBackground = true)
internal fun EnterScreenPreview() {
    EnterScreen(EnterScreenUiState.empty(), emptyFlow(), {}, {})
}