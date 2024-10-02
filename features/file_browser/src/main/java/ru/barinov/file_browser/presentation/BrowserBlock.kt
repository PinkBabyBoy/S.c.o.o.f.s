package ru.barinov.file_browser.presentation

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import ru.barinov.file_browser.events.FieObserverEvent
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.ui_ext.ColorPair
import ru.barinov.ui_ext.DecorStyle
import ru.barinov.ui_ext.bottomNavGreen
import ru.barinov.ui_ext.fileBrowserBackground
import ru.barinov.ui_ext.mainGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified T : FieObserverEvent> BrowserBlock(
    files: Flow<PagingData<FileUiModel>>,
    currentFolderName: String,
    paddingBottom: Dp,
    isSelectionEnabled: Boolean,
    crossinline onEvent: (T) -> Unit,
    isPageEmpty: Boolean,
    isInRoot: Boolean,
    showLoading: Boolean,
    actions: Set<@Composable (RowScope) -> Unit> = emptySet()
) {
    val selectionMode = remember { mutableStateOf(false) }
    val folderFiles = files.collectAsLazyPagingItems(Dispatchers.IO)
    BackHandler {
        if (selectionMode.value) {
            selectionMode.value = false
        } else onEvent(OnBackPressed as T)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFCFFFD))
        ) {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            FileBrowserAppBar(
                titleString = currentFolderName,
                topAppBarScrollBehavior = scrollBehavior,
                onNavigateUpClicked = {
                    if (selectionMode.value) {
                        selectionMode.value = false
                    } else onEvent(OnBackPressed as T)
                },
                actions = actions,
                showArrow = !isInRoot
            )
            LazyColumn(
                modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    bottom = paddingBottom + 8.dp,
                    start = 6.dp,
                    end = 6.dp
                )
            ) {
                items(
                    count = folderFiles.itemCount,
                    key = { folderFiles[it]?.fileId?.path ?: String() }
                ) { index ->
                    val fileModel = folderFiles[index]
                    if (fileModel != null) {
                        FileItem<T>(
                            file = fileModel,
                            selectionMode = selectionMode.value && isSelectionEnabled,
                            toggleSelection = { selectionMode.value = !selectionMode.value },
                            selectionAvailable = isSelectionEnabled,
                            onEvent = { onEvent(it) },
                            showLoading = showLoading
                        )
                    } else LoaderPlaceholder()
                }
            }
        }
        if (isPageEmpty) {
            Text(text = "Folder is empty", modifier = Modifier.align(Alignment.Center))
        }
        if (folderFiles.loadState.refresh is LoadState.Error) {
            Text(
                text = "Error",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun LoaderPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    }
}