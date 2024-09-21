package ru.barinov.file_browser.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
class TopLevelScreen(
    val rout: FileBrowserRout,
    @StringRes val label: Int,
    @DrawableRes val iconImgDrawable: Int
)
