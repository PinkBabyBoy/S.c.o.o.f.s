package ru.barinov.file_browser.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Column(Modifier.padding(bottom = scaffoldPaddings.calculateBottomPadding())) {
        HorizontalPager(state) { pageIndex ->
            when (Pages.entries[pageIndex]) {
                Pages.CONTAINERS -> {
                    val vm: ContainersViewModel = koinViewModel()
                    Containers(
                        state = vm.uiState.collectAsState().value,
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        sideEffects = vm.sideEffects,
                        onEvent = vm::handleEvent
                    )
                }

                Pages.FILE_BROWSER -> {
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

                Pages.KEY_PICKER -> {
                    val vm: KeySelectorViewModel = koinViewModel()
                    KeySelector(
                        state = vm.uiState.collectAsState().value,
                        onEvent = vm::handleEvent,
                        sideEffects = vm.sideEffects,
                        navController = navController,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
        ScrollableTabRow(
            selectedTabIndex =  state.currentPage,
        ) {
            Pages.entries.forEach {
                LeadingIconTab(
                    selected = state.currentPage == it.ordinal,
                    text = {Text("00")},
                    icon = {},
                    onClick = {}
                )
            }
        }
        Spacer(Modifier.height(64.dp))
    }
}

private enum class Pages{
    CONTAINERS, FILE_BROWSER, KEY_PICKER
}
