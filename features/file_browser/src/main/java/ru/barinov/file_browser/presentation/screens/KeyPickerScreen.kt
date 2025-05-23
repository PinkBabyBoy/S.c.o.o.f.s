package ru.barinov.file_browser.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.barinov.file_browser.events.KeySelectorEvent
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.KeySelectorSideEffect
import ru.barinov.file_browser.sideEffects.ShowInfo
import ru.barinov.file_browser.states.KeySelectorUiState
import ru.barinov.core.ui.ScoofButton
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.file_browser.CreateKeyStore
import ru.barinov.file_browser.NoArgsRouts
import ru.barinov.file_browser.loadKeyStore
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.presentation.BrowserBlock
import ru.barinov.file_browser.presentation.Pages

@Composable
fun KeySelector(
    state: KeySelectorUiState,
    onEvent: (KeySelectorEvent) -> Unit,
    sideEffects: Flow<KeySelectorSideEffect>,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    openPage: (Int) -> Unit,
    pageState: MutableIntState
) {
    val isPageOnScreen = remember {
        derivedStateOf {
            pageState.intValue == Pages.KEY_PICKER.ordinal
        }
    }
    if(!isPageOnScreen.value) return
    val localCoroutine = rememberCoroutineScope()
    val context = LocalContext.current


    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            CanGoBack -> openPage(Pages.CONTAINERS.ordinal)
            is KeySelectorSideEffect.AskToLoadKey -> navController.navigate(loadKeyStore(sideEffect.source, sideEffect.fileName))

            is ShowInfo -> {
                localCoroutine.launch {
                    snackbarHostState.showSnackbar(context.getString(sideEffect.text))
                }
            }

           is KeySelectorSideEffect.ShowKeyCreationDialog -> navController.navigate(CreateKeyStore(sideEffect.source))
        }
    }
    if (state.isKeyLoaded) {
        BackHandler(enabled = isPageOnScreen.value) {
            openPage(Pages.CONTAINERS.ordinal)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(text = stringResource(id = ru.barinov.core.R.string.key_loaded))
                Image(
                    painter = painterResource(id = ru.barinov.core.R.drawable.baseline_key_24),
                    modifier = Modifier
                        .size(54.dp)
                        .padding(top = 24.dp),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.height(32.dp))
                ScoofButton(
                    onClick = { onEvent(KeySelectorEvent.UnbindKey) },
                    modifier = Modifier.padding(8.dp),
                    buttonText = ru.barinov.core.R.string.unbind
                )
            }
        }
    } else {
        BrowserBlock<KeySelectorEvent, FileUiModel>(
            files = state.files,
            currentFolderName = state.currentFolderName,
            isSelectionEnabled = false,
            onEvent = { onEvent(it) },
            isPageEmpty = state.isPageEmpty,
            isInRoot = state.isInRoot,
            showLoading = false,
            additionalInfoEnabled = false,
            appbarState = state.appBarState
        )
    }
}
