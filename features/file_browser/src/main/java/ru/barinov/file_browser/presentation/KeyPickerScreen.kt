package ru.barinov.file_browser.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import ru.barinov.core.Source
import ru.barinov.file_browser.args.KeyLoadBottomSheetArgs
import ru.barinov.file_browser.events.KeySelectorEvent
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.KeySelectorSideEffect
import ru.barinov.file_browser.sideEffects.ShowInfo
import ru.barinov.file_browser.states.KeyPickerUiState
import ru.barinov.core.ui.BottomSheetPolicy
import ru.barinov.core.ui.ScoofButton
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.file_browser.R
import ru.barinov.file_browser.events.OnboardingFinished
import ru.barinov.file_browser.events.SourceChanged
import ru.barinov.onboarding.OnBoarding
import ru.barinov.onboarding.orEmpty
import ru.barinov.onboarding.switchDefault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeySelector(
    state: KeyPickerUiState,
    onEvent: (KeySelectorEvent) -> Unit,
    sideEffects: Flow<KeySelectorSideEffect>,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    onFirstPage: () -> Unit
) {
    val keyLoadBsState = remember { mutableStateOf<BottomSheetPolicy>(BottomSheetPolicy.Collapsed) }
    val localCoroutine = rememberCoroutineScope()
    val context = LocalContext.current
    val isKeystoreCreatorBsVisible = remember { mutableStateOf(false) }

    SingleEventEffect(sideEffects) { sideEffect ->
        when (sideEffect) {
            CanGoBack -> onFirstPage()
            is KeySelectorSideEffect.AskToLoadKey ->
                keyLoadBsState.value = BottomSheetPolicy.Expanded(
                    KeyLoadBottomSheetArgs(
                        filename = sideEffect.name,
                        fileId = sideEffect.fileId
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
        BackHandler {
            onFirstPage()
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
                    contentDescription = null
                )
                Spacer(
                    modifier = Modifier
                        .height(32.dp)
                )
                ScoofButton(
                    onClick = { onEvent(KeySelectorEvent.UnbindKey) },
                    modifier = Modifier.padding(8.dp),
                    buttonText = ru.barinov.core.R.string.unbind
                )
            }
        }
    } else {
        BrowserBlock<KeySelectorEvent>(
            files = state.files,
            currentFolderName = state.currentFolderName,
            isSelectionEnabled = false,
            onEvent = { onEvent(it) },
            isPageEmpty = state.isPageEmpty,
            isInRoot = state.isInRoot,
            actions = buildSet {
                if (state.sourceState.isMsdAttached) {
                    add {
                        val onbData = state.onboardings[OnBoarding.CHANGE_SOURCE].orEmpty()
                        OnBoarding(
                            title = stringResource(ru.barinov.core.R.string.key_creation_title_ond),
                            state = onbData,
                            tooltipText = stringResource(ru.barinov.core.R.string.key_creation_message_ond),
                            onClick = { onEvent(OnboardingFinished(OnBoarding.CHANGE_SOURCE)) },
                            width = 42.dp,
                            hasNext = false
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (state.sourceState.currentSource == Source.INTERNAL)
                                        ru.barinov.core.R.drawable.baseline_sd_storage_24
                                    else ru.barinov.core.R.drawable.mass_storage_device
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .clickable {
                                        onEvent(SourceChanged)
                                    }
                                    .size(26.dp),
                                tint = if (state.sourceState.currentSource == Source.INTERNAL) Color.Black else LocalContentColor.current
                            )
                        }
                    }
                    add { Spacer(modifier = Modifier.width(16.dp)) }
                }
                add {
                    val onbData = state.onboardings[OnBoarding.KEY_CREATION].orEmpty()
                    OnBoarding(
                        title = stringResource(ru.barinov.core.R.string.key_creation_title_ond),
                        state = onbData,
                        tooltipText = stringResource(ru.barinov.core.R.string.key_creation_message_ond),
                        onClick = {onEvent(OnboardingFinished(OnBoarding.KEY_CREATION))},
                        width = 42.dp,
                        hasNext = false
                    ) {
                        Icon(
                            painter = painterResource(id = ru.barinov.core.R.drawable.baseline_key_24),
                            contentDescription = null,
                            modifier = Modifier.clickable { isKeystoreCreatorBsVisible.value = true }
                        )
                    }
                }
                add { Spacer(modifier = Modifier.width(16.dp)) }
            },
            showLoading = false,
            additionalInfoEnabled = false
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
                        fileId = fileId,
                        password = pass.toCharArray()
                    )
                )
            },
            filename = filename
        )
    }
}
