package ru.barinov.protected_enter

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.barinov.permission_manager.Permission
import ru.barinov.permission_manager.PermissionRequestManager
import ru.barinov.core.ui.InformationalBlock
import ru.barinov.core.ui.InformationalBlockType
import ru.barinov.core.ui.Keyboard
import ru.barinov.core.ui.PasswordTextField
import ru.barinov.core.ui.ProgressButton
import ru.barinov.core.ui.RegisterLifecycleCallbacks
import ru.barinov.core.ui.ScoofAlertDialog
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.core.ui.darkGreen
import ru.barinov.core.ui.enterScreenBackground
import ru.barinov.core.ui.getActivity
import ru.barinov.core.ui.keyboardAsState

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
                    if(state.isBioAuthAvailable()) {
                        enterScreenEvent(EnterScreenEvent.RequestFingerprintAuth(context.getActivity()!!))
                    }
                }
            }
        )
    }
    RegisterLifecycleCallbacks(
        onDestroy = { permissionManager.close() },
        onCreate = {
            if(state.hasPermission && state.isBioAuthAvailable())
                enterScreenEvent(EnterScreenEvent.RequestFingerprintAuth(context.getActivity()!!))
        }
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
        if(!state.isBioAuthAvailable()) {
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
                        hint = ru.barinov.core.R.string.password_enter_helper_text
                    )
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 84.dp)
            )
        }
        if (state.stage == Stage.Enter && !state.isBioAuthAvailable()) {
            val alertDialogVisible = remember {
                mutableStateOf(false)
            }
            Spacer(modifier = Modifier.height(64.dp))
            InformationalBlock(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 64.dp),
                type = InformationalBlockType.INFO,
                text = "Reset password",
                onIconClicked = { alertDialogVisible.value = true },
                onBlockClicked = { alertDialogVisible.value = true }

            )
            if (alertDialogVisible.value) {
                ScoofAlertDialog(
                    title = " Reset the password?",
                    message =  "All stored data will be removed",
                    onConfirmed = {
                        enterScreenEvent(EnterScreenEvent.ResetConfirmed)
                        alertDialogVisible.value = false
                    },
                    onDismissRequest = { alertDialogVisible.value = false }
                )
            }
        }
        if (state.stage == Stage.Create) {
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
            if(!state.isBioAuthAvailable()) {
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
            }
        } else {
            Spacer(modifier = Modifier.height(84.dp))
            InformationalBlock(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .align(Alignment.CenterHorizontally),
                type = InformationalBlockType.WARNING,
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
        ErrorType.READ_HASH_ERROR -> ru.barinov.core.R.string.empty_password_text
        ErrorType.WRONG_PASSWORD -> R.string.wrong_password_err_text
        ErrorType.CREATE_NOT_EQUALS -> R.string.not_equals_passwords_err_text
        ErrorType.PASSWORD_EMPTY -> ru.barinov.core.R.string.empty_password_text
        ErrorType.CHECK_EMPTY -> R.string.empty_check_password_text
    }.let { stringResource(id = it) }
    Text(text = text, color = MaterialTheme.colorScheme.error)
}

@Composable
@Preview(showBackground = true)
internal fun EnterScreenPreview() {
    EnterScreen(EnterScreenUiState.empty(), emptyFlow(), {}, {})
}