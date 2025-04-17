package ru.barinov.file_browser.presentation


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import ru.barinov.file_browser.events.FieObserverEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.events.RemoveSelection
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.file_browser.models.ViewableFileModel
import ru.barinov.file_browser.states.AppbarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified T : FieObserverEvent, reified M: ViewableFileModel> BrowserBlock(
    files: Flow<PagingData<M>>,
    currentFolderName: String,
    isSelectionEnabled: Boolean,
    crossinline onEvent: (T) -> Unit,
    isPageEmpty: Boolean,
    isInRoot: Boolean,
    showLoading: Boolean,
    appbarState: AppbarState,
    additionalInfoEnabled: Boolean = true,
    hasSelected: Boolean = false
) {
    val selectionMode = remember { mutableStateOf(false) }
    val pagedFFiles = files.collectAsLazyPagingItems(Dispatchers.IO)
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(appBarState)
    BackHandler {
        if (selectionMode.value && hasSelected) {
            selectionMode.value = false
            onEvent(RemoveSelection as T)
        } else {
            if(!hasSelected) selectionMode.value = false
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
                appbarState = appbarState,
                onEvent = { onEvent(it as T) },
                showArrow = !isInRoot,
            )
            LazyColumn(
                modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    start = 6.dp,
                    end = 6.dp
                )
            ) {
                items(
                    count = pagedFFiles.itemCount,
                    key = { pagedFFiles[it]?.fileId?.value ?: String() },
                    contentType = {(pagedFFiles[it] as? FileUiModel)?.isDir}
                ) { index ->
                    val fileModel = pagedFFiles[index]
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
        if (pagedFFiles.loadState.refresh is LoadState.Error) {
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