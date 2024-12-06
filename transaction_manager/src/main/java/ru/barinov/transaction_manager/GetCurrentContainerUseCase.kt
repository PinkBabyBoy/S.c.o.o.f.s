package ru.barinov.transaction_manager

import android.content.Context
import ru.barinov.internal_data.ContainerProvider
import ru.barinov.internal_data.IndexesProvider
import java.io.File

class GetCurrentContainerUseCase(
    private val indexesProvider: IndexesProvider,
    private val containerProvider: ContainerProvider,
) {

    operator fun invoke(name: String): ContainerData {
        val container = containerProvider.getContainer(name)
        val index = indexesProvider.getIndex(name)
        return ContainerData(container, index)
    }
}

class ContainerData(
    val container: File,
    val indexes: File
){
    val initialSize: Long = container.length()
    val initialIndexesSize: Long = indexes.length()
}