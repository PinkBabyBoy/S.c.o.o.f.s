package ru.barinov.file_browser.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import ru.barinov.file_browser.presentation.FileBrowserRout

@Immutable
class TopLevelScreen(
    val rout: FileBrowserRout,
    @StringRes val label: Int,
    @DrawableRes val iconImgDrawable: Int
)
