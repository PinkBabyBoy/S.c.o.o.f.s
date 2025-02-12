package ru.barinov.file_browser.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import ru.barinov.core.FileId
import ru.barinov.core.FileSize
import ru.barinov.core.FileTypeInfo
import ru.barinov.core.Filepath
import ru.barinov.core.Source
import ru.barinov.file_browser.events.FieObserverEvent
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.core.ui.fileItemColor
import ru.barinov.core.ui.mainGreen

@Composable
inline fun <reified T : FieObserverEvent> FileGridItem(
    file: FileUiModel,
    selectionMode: Boolean,
    crossinline onEvent: (T) -> Unit = {},
    additionalInfoEnabled: Boolean
) {
    val info =
        file.info.collectAsStateWithLifecycle(
            lifecycleOwner = LocalLifecycleOwner.current,
            context = Dispatchers.IO
        ).value
    Card(
        colors = CardDefaults.cardColors(
            containerColor = fileItemColor
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable {
            onEvent(
                OnFileClicked(
                    fileId = file.fileId,
                    selectionMode = selectionMode,
                    fileInfo = file.info.value,
                    isDir = file.isDir
                ) as T
            )
        }
    ) {
        Column {
            Row {
                Image(
                    painter = painterResource(id = file.placeholderRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(86.dp)
                        .padding(16.dp)
                )
                AnimatedVisibility(
                    visible = selectionMode,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Checkbox(
                        colors = CheckboxDefaults.colors().copy(checkedBoxColor = mainGreen),
                        checked = file.isSelected,
                        onCheckedChange = {
                            onEvent(
                                OnFileClicked(
                                    fileId = file.fileId,
                                    selectionMode = selectionMode,
                                    fileInfo = file.info.value,
                                    isDir = file.isDir
                                ) as T
                            )
                        })
                }

            }
            Text(text = file.name, modifier = Modifier.align(Alignment.CenterHorizontally))
            if (additionalInfoEnabled) {
                Spacer(Modifier.height(8.dp))
                Text(text = info.getText(), fontSize = 10.sp, color = Color.Gray, modifier =  Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
inline fun <reified T : FieObserverEvent> FileItem(
    file: FileUiModel,
    selectionMode: Boolean,
    selectionAvailable: Boolean,
    showLoading: Boolean,
    crossinline toggleSelection: () -> Unit = {},
    crossinline onEvent: (T) -> Unit = {},
    additionalInfoEnabled: Boolean
) {
    val interactSource = remember { mutableStateOf(MutableInteractionSource()) }
    val info =
        file.info.collectAsStateWithLifecycle(
            lifecycleOwner = LocalLifecycleOwner.current,
            context = Dispatchers.IO
        ).value
    Card(
        colors = CardDefaults.cardColors(
            containerColor = fileItemColor
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .drawBehind {
                drawLine(
                    color = Color.Gray,
                    start = Offset(32f, size.height),
                    end = Offset(size.width - 32f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .combinedClickable(
                interactionSource = interactSource.value,
                indication = ripple(),
                onLongClick = {
                    if (selectionAvailable) {
                        toggleSelection()
                    }
                },
                onClick = {
                    onEvent(
                        OnFileClicked(
                            fileId = file.fileId,
                            selectionMode = selectionMode,
                            fileInfo = file.info.value,
                            isDir = file.isDir
                        ) as T
                    )
                }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            FilePreview(file, info, showLoading)
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(text = file.name)
                if (additionalInfoEnabled) {
                    Text(text = info.getText(), fontSize = 10.sp, color = Color.Gray)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                AnimatedVisibility(visible = selectionMode) {
                    Checkbox(
                        colors = CheckboxDefaults.colors().copy(checkedBoxColor = mainGreen),
                        checked = file.isSelected,
                        onCheckedChange = {
                            onEvent(
                                OnFileClicked(
                                    fileId = file.fileId,
                                    selectionMode = selectionMode,
                                    fileInfo = file.info.value,
                                    isDir = file.isDir
                                ) as T
                            )
                        })
                }
            }
        }
    }
}

@Composable
fun FilePreview(file: FileUiModel, info: FileTypeInfo, showLoading: Boolean) {
    val context = LocalContext.current


    when (info) {
        is FileTypeInfo.ImageFile -> {
            Image(
                bitmap = info.bitmapPreview.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .padding(16.dp)
                    .border(1.dp, Color.Gray)
            )
        }

        is FileTypeInfo.IndexStorage, is FileTypeInfo.Other, is FileTypeInfo.Dir -> {
            Image(
                painter = painterResource(id = file.placeholderRes),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(16.dp)
            )
        }

        FileTypeInfo.Unconfirmed -> {
            if (showLoading)
                CircularProgressIndicator(
                    color = mainGreen,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(16.dp)
                )
            else
                Image(
                    painter = painterResource(id = file.placeholderRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(16.dp)
                )
        }
    }
}

fun FileTypeInfo.getText(): String =
    when (this) {
        is FileTypeInfo.Dir -> contentText
        is FileTypeInfo.ImageFile -> size
        is FileTypeInfo.IndexStorage -> creationDate
        is FileTypeInfo.Other -> size
        is FileTypeInfo.Unconfirmed -> String()
    }

//Previews
@Composable
@Preview(showBackground = true)
fun FileItemPreview() {
    FileItem<FieObserverEvent>(
        FileUiModel(
            fileId = FileId.byFilePath(Filepath.root("")),
            filePath = "",
            origin = Source.INTERNAL,
            isDir = false,
            isFile = true,
            name = "my_pron.mp4",
            fileSize = FileSize(656565L),
            placeholderRes = ru.barinov.core.R.drawable.file,
            isSelected = true,
            info = MutableStateFlow(FileTypeInfo.Other(false, ""))
        ),
        true, true, false, {}, {}, true
    )
}

@Composable
@Preview(showBackground = true)
fun FileItemGridPreview() {
    FileGridItem<FieObserverEvent>(
        file = FileUiModel(
            fileId = FileId.byFilePath(Filepath.root("")),
            filePath = "",
            origin = Source.INTERNAL,
            isDir = false,
            isFile = true,
            name = "my_pron.mp4",
            fileSize = FileSize(656565L),
            placeholderRes = ru.barinov.core.R.drawable.file,
            isSelected = true,
            info = MutableStateFlow(FileTypeInfo.Other(false, ""))
        ),
        selectionMode = true,
        onEvent = {},
        additionalInfoEnabled = true
    )
}