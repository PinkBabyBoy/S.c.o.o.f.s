package ru.barinov.file_browser.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.navigation.NavController
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.file_browser.events.OpenedContainerEvent
import ru.barinov.file_browser.models.EncryptedFileIndexUiModel
import ru.barinov.file_browser.sideEffects.CanGoBack
import ru.barinov.file_browser.sideEffects.OpenImageFile
import ru.barinov.file_browser.sideEffects.OpenedContainerSideEffect
import ru.barinov.file_browser.viewModels.ContainerContentViewState

@Composable
fun ContainerContent(
    uiState: State<ContainerContentViewState>,
    eventReceiver: (OpenedContainerEvent) -> Unit,
    sideEffects: Flow<OpenedContainerSideEffect>,
    navController: NavController,
) {

    SingleEventEffect(sideEffects) { sideEffect ->
        when(sideEffect){
            CanGoBack -> navController.navigateUp()
            is OpenImageFile -> TODO()
        }

    }

    when(val state = uiState.value){
        is ContainerContentViewState.ContainerLoaded -> Ready(state.pageDataFlow, eventReceiver)
        ContainerContentViewState.Error -> Error()
        ContainerContentViewState.Loading -> Loading()
    }
}

@Composable
private fun Loading(){

}

@Composable
private fun Error(){}

@Composable
private fun Ready(
    pageDataFlow: Flow<PagingData<EncryptedFileIndexUiModel>>,
    eventReceiver: (OpenedContainerEvent) -> Unit
){
    BrowserBlock<OpenedContainerEvent, EncryptedFileIndexUiModel>(
        files = pageDataFlow,
        currentFolderName = String(),
        isSelectionEnabled = true,
        onEvent = { event -> eventReceiver(event) },
        actions = setOf(), //TODO fill with actions of topbar
        isPageEmpty = false, //Add to state
        isInRoot = true,
        showLoading = true
    )
}