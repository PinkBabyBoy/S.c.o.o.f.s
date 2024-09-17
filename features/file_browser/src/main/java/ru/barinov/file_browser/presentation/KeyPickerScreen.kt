package ru.barinov.file_browser.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.barinov.file_browser.R
import ru.barinov.file_browser.args.KeyLoadBottomSheetArgs
import ru.barinov.file_browser.events.KeySelectorEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.KeySelectorSideEffect
import ru.barinov.file_browser.sideEffects.ShowInfo
import ru.barinov.file_browser.states.KeyPickerUiState
import ru.barinov.ui_ext.BottomSheetPolicy
import ru.barinov.ui_ext.SingleEventEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeySelector(
    state: KeyPickerUiState,
    scaffoldPaddings: PaddingValues,
    onEvent: (KeySelectorEvent) -> Unit,
    sideEffects: Flow<KeySelectorSideEffect>,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    BackHandler {
        onEvent(OnBackPressed)
    }
    val keyLoadBsState = remember { mutableStateOf<BottomSheetPolicy>(BottomSheetPolicy.Collapsed) }
    val localCoroutine = rememberCoroutineScope()
    val context = LocalContext.current
    val isKeystoreCreatorBsVisible = remember { mutableStateOf(false) }

    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            CanGoBack -> navController.navigateUp()
            is KeySelectorSideEffect.AskToLoadKey ->
                keyLoadBsState.value = BottomSheetPolicy.Expanded(
                    KeyLoadBottomSheetArgs(
                        filename = sideEffect.name,
                        uuid = sideEffect.uuid
                    )
                )

            is ShowInfo -> {
                isKeystoreCreatorBsVisible.value = false
                localCoroutine.launch {
                    snackbarHostState.showSnackbar(context.getString(sideEffect.text))
                }
            }
        }
    }
    if (state.isKeyLoaded) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(text = "Key is binded")
                Image(
                    painter = painterResource(id = ru.barinov.core.R.drawable.baseline_key_24),
                    contentDescription = null
                )
                Spacer(
                    modifier = Modifier
                        .height(32.dp)
                )
                Button(
                    onClick = { onEvent(KeySelectorEvent.UnbindKey) }, modifier = Modifier
                        .padding(8.dp)
                ) {
                    Text(text = "Unbind")
                }
            }
        }
    } else {
        BrowserBlock<KeySelectorEvent>(
            files = state.files,
            currentFolderName = state.currentFolderName,
            paddingBottom = scaffoldPaddings.calculateBottomPadding(),
            isSelectionEnabled = false,
            onEvent = { onEvent(it) },
            isPageEmpty = state.isPageEmpty,
            actions = setOf {
                Image(
                    painter = painterResource(id = ru.barinov.core.R.drawable.baseline_key_24),
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        isKeystoreCreatorBsVisible.value = true
                    }
                )
            }
        )
    }
    if (isKeystoreCreatorBsVisible.value) {
        CreateKeyStoreBottomSheet(
            onDismissRequested = { isKeystoreCreatorBsVisible.value = false },
            onConfirmed = { name, pass, load ->
                onEvent(
                    KeySelectorEvent.CreateKeyStoreConfirmed(
                        password = pass,
                        name = name,
                        loadInstantly = load
                    )
                )
            }
        )
    }
    ((keyLoadBsState.value as? BottomSheetPolicy.Expanded<*>)?.args as? KeyLoadBottomSheetArgs)?.apply {
        KeyStoreLoadBottomSheet(
            onDismissRequested = {
                keyLoadBsState.value = BottomSheetPolicy.Collapsed
            },
            onConfirmed = { pass ->
                onEvent(
                    KeySelectorEvent.KeyLoadConfirmed(
                        uuid = uuid,
                        password = pass.toCharArray()
                    )
                )
            },
            filename = filename
        )
    }
}