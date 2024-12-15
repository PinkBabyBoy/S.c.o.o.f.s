package ru.barinov.file_browser.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.barinov.file_browser.viewModels.ContainersViewModel
import ru.barinov.file_browser.viewModels.FileObserverViewModel
import ru.barinov.file_browser.viewModels.KeySelectorViewModel

private const val PAGE_COUNT = 3

@Composable
fun HostPager(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    scaffoldPaddings: PaddingValues
){
    val state = rememberPagerState { PAGE_COUNT }
    HorizontalPager(state) { pageIndex ->
        when(pageIndex){
            0 -> {
                val vm: ContainersViewModel = koinViewModel()
                Containers(
                    state = vm.uiState.collectAsState().value,
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    sideEffects = vm.sideEffects,
                    onEvent = vm::handleEvent
                )
            }
            1 -> {
                val vm: FileObserverViewModel = koinViewModel()
                FileBrowserScreen(
                    state = vm.uiState.collectAsState().value,
                    scaffoldPaddingValues = scaffoldPaddings,
                    sideEffects = vm.sideEffects,
                    navController = navController,
                    onEvent = vm::onNewEvent,
                    snackbarHostState = snackbarHostState
                )
            }
            2 -> {
                val vm: KeySelectorViewModel = koinViewModel()
                KeySelector(
                    state = vm.uiState.collectAsState().value,
                    scaffoldPaddings = scaffoldPaddings,
                    onEvent = vm::handleEvent,
                    sideEffects = vm.sideEffects,
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}
