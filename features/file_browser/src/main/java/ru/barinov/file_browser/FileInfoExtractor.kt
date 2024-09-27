package ru.barinov.file_browser

import android.content.Context
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.core.bytesToMbSting
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileInfoExtractor(private val appContext: Context) {

    private val extractorCoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    operator fun invoke(state: MutableState<String>, file: FileEntity) {
        extractorCoroutineScope.launch {
            val textInfo = when (file) {
                is FileEntity.InternalFile
                -> if (file.isDir)
                    appContext.getString(
                        ru.barinov.ui_ext.R.string.contains_files_info,
                        file.attachedOrigin.list()?.size ?: 0
                    )
                else file.size.value.bytesToMbSting()

                is FileEntity.MassStorageFile
                -> if (file.isDir)
                    appContext.getString(
                        ru.barinov.ui_ext.R.string.contains_files_info,
                        file.attachedOrigin.list()?.size ?: 0
                    )
                else file.size.value.bytesToMbSting()

                is FileEntity.Index
                -> SimpleDateFormat(
                    "dd-MM-yyyy",
                    Locale.getDefault()
                ).format(Date(file.attachedOrigin.lastModified()))

            }
            state.value = textInfo
        }
    }

}
