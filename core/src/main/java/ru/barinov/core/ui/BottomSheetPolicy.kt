package ru.barinov.core.ui

sealed interface BottomSheetPolicy {

    data object Collapsed: BottomSheetPolicy

    data class Expanded<T>(val args: T?): BottomSheetPolicy
}

fun BottomSheetPolicy.shouldShow()  = this is BottomSheetPolicy.Expanded<*>

//UNSAFE!!!
fun <T> BottomSheetPolicy.getArgs() = (this as BottomSheetPolicy.Expanded<*>).args as T
