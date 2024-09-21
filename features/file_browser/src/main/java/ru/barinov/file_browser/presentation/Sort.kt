package ru.barinov.file_browser.presentation

import androidx.annotation.StringRes

class Sort(
    @StringRes val text: Int,
    val type: Type
) {
    enum class Type {
        AS_IS, NEW_FIRST, OLD_FIRST, BIG_FIRST, SMALL_FIRST
    }
}
