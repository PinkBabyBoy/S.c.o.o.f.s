package ru.barinov.protected_enter

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import ru.barinov.ui_ext.Keyboard
import ru.barinov.ui_ext.PasswordTextField
import ru.barinov.ui_ext.ProgressButton
import ru.barinov.ui_ext.RegisterLifecycleCallbacks
import ru.barinov.ui_ext.SingleEventEffect
import ru.barinov.ui_ext.bottomNavGreen
import ru.barinov.ui_ext.darkGreen
import ru.barinov.ui_ext.enterScreenBackground
import ru.barinov.ui_ext.keyboardAsState
import ru.barinov.ui_ext.lightGreen
import ru.barinov.ui_ext.mainGreen

@Composable
internal fun EnterScreen(
    state: EnterScreenUiState,
    sideEffects: Flow<SideEffects>,
    enterScreenEvent: (EnterScreenEvent) -> Unit,
    rebase: () -> Unit
) {
    val context = LocalContext.current
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

    val progress = remember { mutableStateOf(false) }

    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            SideEffects.AskPermission -> permissionManager.launch(Permission.MANAGE_FILES)
            SideEffects.EnterGranted -> rebase()
            SideEffects.ProgressStop -> progress.value = false
        }
    }

    val isPermissionInfoBsVisible = remember { mutableStateOf(false) }
    val kbState = keyboardAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(enterScreenBackground)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        AnimatedVisibility(visible = kbState.value == Keyboard.Closed) {
            Box {
                AnimatedLogo(
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = 128.dp)
                )
            }
        }

        if (kbState.value == Keyboard.Opened) {
            Spacer(modifier = Modifier.height(64.dp))
        }
        val errors =
            state.errors.filter { it != ErrorType.CHECK_EMPTY && it != ErrorType.CREATE_NOT_EQUALS }
        PasswordTextField(
            onValueChanged = {
                enterScreenEvent(
                    EnterScreenEvent.NewInput(
                        type = InputType.PASSWORD,
                        input = it
                    )
                )
            },
            isError = errors.isNotEmpty(),
            supportText = {
                SupportText(
                    errors = errors,
                    hint = ru.barinov.ui_ext.R.string.password_enter_helper_text
                )
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 84.dp)
        )
        if (state.type == Stage.Enter) {
            val alertDialogVisible = remember {
                mutableStateOf(false)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                color = darkGreen,
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
            val confirmErrors = state.errors.filter { it == ErrorType.CHECK_EMPTY || it == ErrorType.CREATE_NOT_EQUALS }
            PasswordTextField(
                onValueChanged = {
                    enterScreenEvent(
                        EnterScreenEvent.NewInput(
                            InputType.CONFIRMATION,
                            it
                        )
                    )
                },
                isError = confirmErrors.isNotEmpty(),
                supportText = {
                    SupportText(
                        errors = confirmErrors,
                        hint = R.string.check_password_helper_text
                    )
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 64.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(68.dp))
        }

        if (state.hasPermission) {
            Spacer(modifier = Modifier.height(if (kbState.value == Keyboard.Closed) 84.dp else 42.dp))
            val keyboardController = LocalSoftwareKeyboardController.current
            ProgressButton(
                isEnabled = !progress.value,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .defaultMinSize(minWidth = 228.dp),
                buttonText = R.string.enter_text,
                isProgress = progress
            ) {
                keyboardController?.hide()
                progress.value = true
                enterScreenEvent(EnterScreenEvent.SubmitClicked)
            }
        } else {
            InformationalBlock(
                modifier = Modifier
                    .padding(top = 84.dp)
                    .align(Alignment.CenterHorizontally),
                type = InformationalBlockType.INFO,
                text = stringResource(id = R.string.permission_warning_title),
                onBlockClicked = { permissionManager.launch(Permission.MANAGE_FILES) },
                onIconClicked = { isPermissionInfoBsVisible.value = true }
            )
            if (isPermissionInfoBsVisible.value) {
                PermissionInfoBottomSheet {
                    isPermissionInfoBsVisible.value = false
                }
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
        ErrorType.READ_HASH_ERROR -> ru.barinov.ui_ext.R.string.empty_password_text
        ErrorType.WRONG_PASSWORD -> R.string.wrong_password_err_text
        ErrorType.CREATE_NOT_EQUALS -> R.string.not_equals_passwords_err_text
        ErrorType.PASSWORD_EMPTY -> ru.barinov.ui_ext.R.string.empty_password_text
        ErrorType.CHECK_EMPTY -> R.string.empty_check_password_text
    }.let { stringResource(id = it) }
    Text(text = text, color = MaterialTheme.colorScheme.error)
}

@Composable
@Preview(showBackground = true)
internal fun EnterScreenPreview() {
    EnterScreen(EnterScreenUiState.empty(), emptyFlow(), {}, {})
}