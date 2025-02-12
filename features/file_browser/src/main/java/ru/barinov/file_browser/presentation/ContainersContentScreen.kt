package ru.barinov.file_browser.presentation

import android.graphics.pdf.PdfDocument.Page
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import ru.barinov.core.FileIndex
import ru.barinov.file_browser.events.OnFileClicked

@Composable
fun ContainerContent(
    indexes: Flow<PagingData<FileIndex>>
) {
    val encryptedFiles = indexes.collectAsLazyPagingItems(Dispatchers.IO)
    Box(Modifier.fillMaxSize()){
        LazyColumn(
            contentPadding = PaddingValues(
                start = 6.dp,
                end = 6.dp
            )
        ) {
//            items(
//                count = folderFiles.itemCount,
//                key = { folderFiles[it]?.startPoint ?: 0 },
//            ) { index ->
//                val fileModel = folderFiles[index]
//                if (fileModel != null) {
//                  Text(fileModel.fileName)
//                } else LoaderPlaceholder()
//            }
        }
    }
}