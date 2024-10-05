package ru.barinov.file_browser.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.barinov.core.Source
import ru.barinov.file_browser.viewModels.FilesLoadInitializationViewModel
import ru.barinov.file_browser.viewModels.InitializationMode

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FilesLoadInitialization(initializationMode: InitializationMode, onDismissRequested: () -> Unit) {
    val vm: FilesLoadInitializationViewModel = koinViewModel(parameters = { parametersOf(initializationMode)})
    val state = vm.uiState.collectAsState()
    ModalBottomSheet(onDismissRequest = onDismissRequested) {
        Column {
            if (state.value.containers.isEmpty()) {
                Text(text = "No container associated with current key, create container first")
            } else {
                //Files (not selectable)
                LazyColumn {

                }

                //Containers (selectable)
                LazyColumn {
                }


            }

        }
    }
}
