package ru.barinov.file_browser.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.barinov.file_browser.args.KeyLoadBottomSheetArgs
import ru.barinov.file_browser.events.KeySelectorEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.KeySelectorSideEffect
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

    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            CanGoBack -> navController.navigateUp()
            is KeySelectorSideEffect.AskToLoadKey ->
                keyLoadBsState.value = BottomSheetPolicy.Expanded(
                    KeyLoadBottomSheetArgs(
                        sideEffect.name,
                        sideEffect.uuid
                    )
                )

            KeySelectorSideEffect.KeyLoadFail ->
                localCoroutine.launch {
                    snackbarHostState.showSnackbar("Keys load failed")
                }
        }
    }

    BrowserBlock<KeySelectorEvent>(
        files = state.files,
        currentFolderName = state.currentFolderName,
        paddingBottom = scaffoldPaddings.calculateBottomPadding(),
        isSelectionEnabled = false,
        onEvent = { onEvent(it) },
        isPageEmpty = state.isPageEmpty
    )
    ((keyLoadBsState.value as? BottomSheetPolicy.Expanded<*>)?.args as? KeyLoadBottomSheetArgs)?.apply {
        ModalBottomSheet(
            onDismissRequest = { keyLoadBsState.value = BottomSheetPolicy.Collapsed }
        ) {
            val enteredPassword = remember { mutableStateOf("") }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                fontSize = 14.sp,
                text = "To load keys from ${filename.value}, please enter store's password",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp))
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
                    keyLoadBsState.value = BottomSheetPolicy.Collapsed
                    onEvent(KeySelectorEvent.KeyLoadConfirmed(uuid, enteredPassword.value.toCharArray()))
                }
            ) {
                Text(text = "Load", modifier = Modifier.padding(horizontal = 32.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}