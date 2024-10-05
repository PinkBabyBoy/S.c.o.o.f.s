package ru.barinov.file_browser.events

sealed interface ImageDetailsEvent {
    data object RotateLeft: ImageDetailsEvent
    data object RotateRight: ImageDetailsEvent
    data object SaveToContainer: ImageDetailsEvent
}