package ru.barinov.file_browser.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.barinov.core.headerBig
import ru.barinov.file_browser.events.FieObserverEvent
import ru.barinov.file_browser.events.FileLoadInitializationEvent
import ru.barinov.file_browser.viewModels.FilesLoadInitializationViewModel
import ru.barinov.file_browser.viewModels.InitializationParams
import ru.barinov.core.ui.InformationalBlock
import ru.barinov.core.ui.InformationalBlockType
import ru.barinov.core.ui.ScoofButton
import ru.barinov.core.ui.SingleEventEffect
import ru.barinov.core.ui.bsContainerColor
import ru.barinov.file_browser.sideEffects.FilesLoadInitializationSideEffects

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FilesLoadInitialization(
    initializationMode: InitializationParams,
    onDismissRequested: () -> Unit
) {
    val vm: FilesLoadInitializationViewModel = koinViewModel(parameters = { parametersOf(initializationMode)}, key = initializationMode.hashCode().toString())
    val state = vm.uiState.collectAsStateWithLifecycle(context = Dispatchers.IO, minActiveState = Lifecycle.State.RESUMED)
    val bsState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    SingleEventEffect(vm.sideEffects) { sideEffect ->
        when (sideEffect) {
            FilesLoadInitializationSideEffects.CloseOnLongTransaction
            -> onDismissRequested()
            FilesLoadInitializationSideEffects.CloseOnShortTransaction
            -> onDismissRequested()
        }
    }
    ModalBottomSheet(onDismissRequest = onDismissRequested, sheetState = bsState, containerColor = bsContainerColor) {
        Column {
            if (state.value.containers.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                InformationalBlock(
                    modifier = Modifier.padding(horizontal = 32.dp).align(Alignment.CenterHorizontally),
                    type = InformationalBlockType.WARNING,
                    text = "No container associated with key",
                    onBlockClicked = { onDismissRequested() }) {
                }
                Spacer(modifier = Modifier.height(48.dp))
            } else {
                Text(text = "Check files and select container", modifier = Modifier.align(Alignment.CenterHorizontally), style = headerBig )
                Spacer(modifier = Modifier.height(32.dp))
                //Files (not selectable)
                LazyColumn(
                    modifier = Modifier.heightIn(0.dp, 240.dp)
                ) {
                    items(state.value.selectedFiles.size){
                        FileItem<FieObserverEvent>(
                            file = state.value.selectedFiles[it],
                            selectionMode = false,
                            selectionAvailable = false,
                            showLoading = false,
                            additionalInfoEnabled = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Select container for save", style = headerBig,  modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(32.dp))

                //Containers (selectable)
                LazyRow(Modifier.padding(horizontal = 16.dp)) {
                    items(state.value.containers.size){
                        FileGridItem<FileLoadInitializationEvent>(
                            file = state.value.containers[it],
                            selectionMode = true,
                            onEvent = { vm.onEvent(it) },
                            additionalInfoEnabled = false
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                ScoofButton(buttonText = ru.barinov.core.R.string.start, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    vm.onEvent(FileLoadInitializationEvent.StartProcess)
                }
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}
