package ru.barinov.transaction_manager

import android.content.Context
import java.io.File

class GetCurrentContainerUseCase(private val appContext: Context) {

    operator fun invoke(): ContainerData {
        error("")
    }
}

class ContainerData(
    val container: File,
    val indexes: File,
    val initialSize: Long = container.length(),
    val initialIndexesSize: Long = indexes.length()
)