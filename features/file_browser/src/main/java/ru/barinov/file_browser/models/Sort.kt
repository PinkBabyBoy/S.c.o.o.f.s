package ru.barinov.file_browser.models

import androidx.annotation.StringRes
import ru.barinov.core.SortType

class Sort(
    @StringRes val text: Int,
    val type: SortType
)
