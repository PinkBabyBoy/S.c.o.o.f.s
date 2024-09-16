package ru.barinov.ui_ext

sealed interface BottomSheetPolicy {

    data object Collapsed: BottomSheetPolicy

    data class Expanded<T>(val args: T?): BottomSheetPolicy
}
