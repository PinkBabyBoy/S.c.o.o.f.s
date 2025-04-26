package ru.barinov.file_browser.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.barinov.core.ui.RegisterLifecycleCallbacks
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.file_browser.events.OpenedContainerEvent
import ru.barinov.file_browser.models.EncryptedFileIndexUiModel
import ru.barinov.file_browser.presentation.BrowserBlock
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.OpenImageFile
import ru.barinov.file_browser.sideEffects.OpenedContainerSideEffect
import ru.barinov.file_browser.states.AppbarState
import ru.barinov.file_browser.viewModels.ContainerContentViewState

@Composable
fun ContainerContent(
    scaffoldPaddings: PaddingValues,
    uiState: State<ContainerContentViewState>,
    eventReceiver: (OpenedContainerEvent) -> Unit,
    sideEffects: Flow<OpenedContainerSideEffect>,
    navController: NavController,
    bottomNavBarVisibility: (Boolean) -> Unit,
) {

    bottomNavBarVisibility(false)

    RegisterLifecycleCallbacks(
        onDispose = { bottomNavBarVisibility(true) }
    )

    SingleEventEffect(sideEffects) { sideEffect ->
        when(sideEffect){
            CanGoBack -> navController.navigateUp()
            is OpenImageFile -> TODO()
        }

    }
    Box(Modifier.background(Color.Black).padding(bottom = scaffoldPaddings.calculateBottomPadding())) {
        when (val state = uiState.value) {
            is ContainerContentViewState.ContainerLoaded -> Ready(state, eventReceiver,)
            ContainerContentViewState.Error -> Error()
            ContainerContentViewState.Loading -> Loading()
        }
    }
}

@Composable
private fun Loading(){}

@Composable
private fun Error(){}

@Composable
private fun Ready(
    uiState: ContainerContentViewState.ContainerLoaded,
    eventReceiver: (OpenedContainerEvent) -> Unit
){
    BrowserBlock<OpenedContainerEvent, EncryptedFileIndexUiModel>(
        files = uiState.pageDataFlow,
        currentFolderName = uiState.containerName,
        isSelectionEnabled = true,
        onEvent = { event -> eventReceiver(event) },
        isPageEmpty = false, //Add to state
        isInRoot = true,
        showLoading = true,
        appbarState = AppbarState.None
    )
}