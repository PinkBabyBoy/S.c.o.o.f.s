package ru.barinov.file_browser.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.barinov.core.ui.getActivity
import ru.barinov.file_browser.presentation.screens.Containers
import ru.barinov.file_browser.presentation.screens.FileBrowserScreen
import ru.barinov.file_browser.presentation.screens.KeySelector
import ru.barinov.file_browser.viewModels.ContainersViewModel
import ru.barinov.file_browser.viewModels.FileObserverViewModel
import ru.barinov.file_browser.viewModels.KeySelectorViewModel

@Composable
fun HostPager(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    scaffoldPaddings: PaddingValues
) {
    val state = rememberPagerState { Pages.entries.size }
    val localCoroutine = rememberCoroutineScope()
    val context = LocalContext.current

    BackHandler {
        context.getActivity()?.finish()
    }
    val openPage: (Int) -> Unit = { pageIndex ->
        localCoroutine.launch {
            state.scrollToPage(pageIndex)
        }
    }
    val pageState = remember { mutableIntStateOf(state.currentPage) }
    LaunchedEffect(state) {
        // Collect from the pager state a snapshotFlow reading the currentPage
        snapshotFlow { state.currentPage }.collect { page ->
            pageState.intValue = page
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White)) {
        Column {
            HorizontalPager(
                state,
                modifier = Modifier.padding(bottom = scaffoldPaddings.calculateBottomPadding() + 12.dp)
            ) { pageIndex ->
                when (Pages.entries[pageIndex]) {
                    Pages.CONTAINERS -> {
                        val vm: ContainersViewModel = koinViewModel()
                        Containers(
                            state = vm.uiState.collectAsState().value,
                            navController = navController,
                            snackbarHostState = snackbarHostState,
                            sideEffects = vm.sideEffects,
                            onEvent = vm::handleEvent,
                            pageState = pageState,
                            openPage = openPage
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
                            snackbarHostState = snackbarHostState,
                            openPage = openPage,
                            pageState = pageState
                        )
                    }

                    Pages.KEY_PICKER -> {
                        val vm: KeySelectorViewModel = koinViewModel()
                        KeySelector(
                            state = vm.uiState.collectAsState().value,
                            onEvent = vm::handleEvent,
                            sideEffects = vm.sideEffects,
                            navController = navController,
                            snackbarHostState = snackbarHostState,
                            openPage = openPage,
                            pageState = pageState
                        )
                    }
                }
            }
        }
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = scaffoldPaddings.calculateBottomPadding() + 6.dp)
        ) {
            Pages.entries.forEachIndexed { index, page ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (page.ordinal == state.currentPage)
                                Color.Gray.copy(alpha = 0.5f)
                            else Color.LightGray.copy(alpha = 0.5f)
                        )

                ) {
                    val iconPainter = when (page) {
                        Pages.CONTAINERS -> ru.barinov.core.R.drawable.outline_archive_24
                        Pages.FILE_BROWSER -> ru.barinov.core.R.drawable.baseline_storage_24
                        Pages.KEY_PICKER -> ru.barinov.core.R.drawable.baseline_key_24
                    }.let { painterResource(it) }
                    Icon(
                        iconPainter, "", modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.Center)
                    )
                }
                if (index != Pages.entries.lastIndex)
                    Spacer(Modifier.width(42.dp))
            }
        }
    }
}

internal enum class Pages{
    CONTAINERS, FILE_BROWSER, KEY_PICKER
}
