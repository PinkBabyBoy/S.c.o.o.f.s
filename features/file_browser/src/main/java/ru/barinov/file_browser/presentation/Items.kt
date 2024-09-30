package ru.barinov.file_browser.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.barinov.core.FileId
import ru.barinov.core.FileSize
import ru.barinov.core.Filepath
import ru.barinov.core.Source
import ru.barinov.file_browser.events.FieObserverEvent
import ru.barinov.file_browser.events.OnFileClicked
import ru.barinov.file_browser.models.FileInfo
import ru.barinov.file_browser.models.FileUiModel
import ru.barinov.ui_ext.fileItemColor
import ru.barinov.ui_ext.mainGreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
inline fun <reified T : FieObserverEvent> FileItem(
    file: FileUiModel,
    selectionMode: Boolean,
    selectionAvailable: Boolean,
    crossinline toggleSelection: () -> Unit = {},
    crossinline onEvent: (T) -> Unit
) {
    val interactSource = remember { mutableStateOf(MutableInteractionSource()) }
    val type = file.info.value
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
                indication = rememberRipple(),
                onLongClick = {
                    if (selectionAvailable) {
                        toggleSelection()
                    }
                },
                onClick = { onEvent(OnFileClicked(file.fileId, selectionMode, type) as T) }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (type !is FileInfo.ImageFile)
                Image(
                    painter = painterResource(id = file.placeholderRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(16.dp)
                )
            else
                Image(
                    bitmap = type.bitmapPreview.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .padding(16.dp)
                        .border(1.dp, Color.Gray)
                )
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(text = file.name)
                Text(text = type.getText(), fontSize = 10.sp, color = Color.Gray)
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
                        checked =  file.isSelected,
                        onCheckedChange = { onEvent(OnFileClicked(file.fileId, selectionMode, type) as T) })
                }
            }
        }
    }
}

fun FileInfo.getText(): String =
    when(this){
        is FileInfo.Dir -> contentText
        is FileInfo.ImageFile -> size
        is FileInfo.Index -> creationDate
        is FileInfo.Other -> size
        is FileInfo.Unconfirmed -> String()
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
            size = FileSize(656565L),
            placeholderRes = ru.barinov.core.R.drawable.file,
            isSelected = true,
            info = remember {
                mutableStateOf(FileInfo.Other(false, ""))
            }
        ),
        true, true, {}, {},
    )
}