package ru.barinov.transaction_manager

import java.io.File

fun interface AppFolderProvider {

    fun provideFolder(): File
}
