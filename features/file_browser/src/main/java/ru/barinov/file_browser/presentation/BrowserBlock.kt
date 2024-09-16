package ru.barinov.file_browser.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import ru.barinov.file_browser.events.FieObserverEvent
import ru.barinov.file_browser.events.FileBrowserEvent
import ru.barinov.file_browser.events.OnBackPressed
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.ui_ext.fileBrowserBackground

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : FieObserverEvent> BrowserBlock(
    files: Flow<PagingData<FileUiModel>>,
    currentFolderName: String,
    paddingBottom: Dp,
    isSelectionEnabled: Boolean,
    onEvent: (T) -> Unit,
    isPageEmpty: Boolean,
    actions: Set<@Composable (RowScope) -> Unit> = emptySet()
) {
    val selectionMode = remember { mutableStateOf(false) }
    val folderFiles = files.collectAsLazyPagingItems()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fileBrowserBackground)
        ) {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            FileBrowserAppBar(
                folderName = currentFolderName,
                topAppBarScrollBehavior = scrollBehavior,
                onNavigateUpClicked = { onEvent(OnBackPressed as T) },
                actions = actions
            )
            LazyColumn(
                modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    bottom = paddingBottom + 8.dp,
                    start = 6.dp,
                    end = 6.dp
                )
            ) {
                items(folderFiles.itemCount) { index ->
                    val fileModel = folderFiles[index]
                    if(fileModel != null) {
                        FileItem<FileBrowserEvent>(
                            file = fileModel,
                            selectionMode = selectionMode.value && isSelectionEnabled,
                            toggleSelection = { selectionMode.value = !selectionMode.value },
                            onEvent = { onEvent(it as T) })
                    } else LoaderPlaceholder()
                }
            }
        }
        if (isPageEmpty) {
            Text(text = "Folder is empty", modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun LoaderPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(48.dp)) {
        CircularProgressIndicator(modifier= Modifier.fillMaxSize())
    }
}