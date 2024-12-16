package ru.barinov.file_browser.presentation


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import ru.barinov.file_browser.events.FieObserverEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.models.FileUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified T : FieObserverEvent> BrowserBlock(
    files: Flow<PagingData<FileUiModel>>,
    currentFolderName: String,
    isSelectionEnabled: Boolean,
    crossinline onEvent: (T) -> Unit,
    isPageEmpty: Boolean,
    isInRoot: Boolean,
    showLoading: Boolean,
    additionalInfoEnabled: Boolean = true,
    actions: Set<@Composable (RowScope) -> Unit> = emptySet()
) {
    val selectionMode = remember { mutableStateOf(false) }
    val folderFiles = files.collectAsLazyPagingItems(Dispatchers.IO)
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(appBarState)
    BackHandler {
        if (selectionMode.value) {
            selectionMode.value = false
        } else {
            onEvent(OnBackPressed as T)
            appBarState.heightOffset = 0f
            appBarState.contentOffset = 0f
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFCFFFD))
        ) {
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
                    start = 6.dp,
                    end = 6.dp
                )
            ) {
                items(
                    count = folderFiles.itemCount,
                    key = { folderFiles[it]?.fileId?.value ?: String() },
                    contentType = {folderFiles[it]?.isDir}
                ) { index ->
                    val fileModel = folderFiles[index]
                    if (fileModel != null) {
                        FileItem<T>(
                            file = fileModel,
                            selectionMode = selectionMode.value && isSelectionEnabled,
                            toggleSelection = { selectionMode.value = !selectionMode.value },
                            selectionAvailable = isSelectionEnabled,
                            onEvent = {
                                if(it is OnFileClicked && it.isDir){
                                    appBarState.heightOffset = 0f
                                    appBarState.contentOffset = 0f
                                }
                                onEvent(it) },
                            showLoading = showLoading,
                            additionalInfoEnabled = additionalInfoEnabled
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