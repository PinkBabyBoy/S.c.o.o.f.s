package ru.barinov.file_browser

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.barinov.core.getBytes
import ru.barinov.core.toContainerFileEntity
import ru.barinov.internal_data.ContainerProvider
import ru.barinov.internal_data.IndexesProvider
import java.io.File

internal class ContainersManagerImpl(
    private val containerProvider: ContainerProvider,
    private val indexesProvider: IndexesProvider,
) : ContainersManager {

    private val _indexes =
        MutableStateFlow(indexesProvider.provideIndexesRoot().listFiles().orEmpty().map {
            it.toContainerFileEntity() }
        )
    override val indexes = _indexes.asStateFlow()

    override suspend fun addContainer(name: String, keysHash: ByteArray) {
        File(containerProvider.provideContainersRoot(), name).also {
            if (it.exists()) error("Already Exist")
            it.createNewFile()
        }
        File(indexesProvider.provideIndexesRoot(), name).also {
            if (it.exists()) error("Already Exist")
            it.createNewFile()
            it.outputStream().use { `is`->
                `is`.write(keysHash.size.getBytes() + keysHash)
            }
        }
        _indexes.value = indexesProvider.provideIndexesRoot().listFiles().orEmpty().map {
            it.toContainerFileEntity()
        }
    }

    override fun removeContainer(name: String) {
        containerProvider.provideContainersRoot().listFiles()?.find {
            it.name == name
        }?.delete()
        indexesProvider.provideIndexesRoot().listFiles()?.find {
            it.name == name
        }?.delete()
        _indexes.value = indexesProvider.provideIndexesRoot().listFiles().orEmpty().map {
            it.toContainerFileEntity()
        }
    }

}