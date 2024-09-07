package ru.barinov.file_browser

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

@Immutable
class TopLevelScreen(
    val rout: FileBrowserRout,
    val label: String,
    @DrawableRes val iconImgDrawable: Int
)
