package ru.barinov.file_browser

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.core.Filepath
import ru.barinov.core.Source
import ru.barinov.core.mutableStateIn
import ru.barinov.core.toFileEntity
import ru.barinov.external_data.MassStorageEventBus
import ru.barinov.external_data.MassStorageState
import java.io.Closeable
import java.util.Stack
import java.util.UUID

//TODO interface
class FileTreeProvider(
    private val rootProvider: RootProvider,
    private val massStorageEventBus: MassStorageEventBus,
    private val rootNameProvider: RootNameProvider
) : Closeable {

    private val localCoroutine = CoroutineScope(Job() + Dispatchers.IO)

    private val innerFolderBackStack = Stack<FileEntity>()
    private val massStorageFolderBackStack = Stack<FileEntity>()

    private val _innerFiles: MutableStateFlow<List<FileEntity>?> =
        MutableStateFlow(rootProvider.getRootFile(Source.INTERNAL)?.innerFiles())

    val innerFiles = _innerFiles.asStateFlow()

    //   private val _massStorageFiles: MutableStateFlow<List<FileEntity>?> =
//        MutableStateFlow(rootProvider.getRootFile(Source.MASS_STORAGE)?.innerFiles())
    private val _massStorageFiles: MutableStateFlow<List<FileEntity>?> =
        massStorageEventBus.massStorageState.map {
            when (it) {
                MassStorageState.Detached -> null
                is MassStorageState.Ready -> it.msdFileSystem.rootDirectory.toFileEntity()
                    .innerFiles()
            }
        }.mutableStateIn(localCoroutine, null)

    val massStorageFiles = _massStorageFiles.asStateFlow()

    fun getCurrentFolderInfo(source: Source): Pair<Filepath, Boolean> {
        val stack =
            if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
        val path = if(stack.isNotEmpty()) stack.peek().path else Filepath.root("Root")
        return path to !stack.hasBackFolder()
    }

    fun getCurrentFolder(source: Source): FileEntity {
        val stack =
            if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
        return if(stack.isNotEmpty()) stack.peek() else rootProvider.getRootFile(source)!!
    }

    fun getCurrentList(source: Source): List<FileEntity>? =
        if (source == Source.INTERNAL) innerFiles.value else massStorageFiles.value

    fun getFileByUUID(uuid: UUID, source: Source): FileEntity =
        when (source) {
            Source.INTERNAL -> innerFiles.getFileByUUID(uuid)
            Source.MASS_STORAGE -> massStorageFiles.getFileByUUID(uuid)
        } ?: error("File is not found")

    fun open(uuid: UUID, source: Source) {
        when (source) {
            Source.INTERNAL -> openInternalFolder(uuid)
            Source.MASS_STORAGE -> openMassStorageFolder(uuid)
        }
    }

    fun update(source: Source) {
        when(source){
            Source.INTERNAL -> _innerFiles.value = getCurrentFolder(source).innerFiles()
            Source.MASS_STORAGE -> _massStorageFiles.value = getCurrentFolder(source).innerFiles()
        }

    }

    private fun onBack(source: Source, folder: FileEntity) {
        when (source) {
            Source.INTERNAL -> _innerFiles.value = folder.innerFiles()
            Source.MASS_STORAGE -> _massStorageFiles.value = folder.innerFiles()
        }
    }

    private fun openMassStorageFolder(fileUUID: UUID) {
        val file = massStorageFiles.getFileByUUID(fileUUID)
        if (file == null || !file.isDir) error("Can't open file")
        innerFolderBackStack.add(file)
        localCoroutine.launch {
            _massStorageFiles.emit(file.innerFiles())
        }

    }

    private fun openInternalFolder(fileUUID: UUID) {
        val folder = innerFiles.getFileByUUID(fileUUID)
        if (folder == null || !folder.isDir) error("Can't open file")
        innerFolderBackStack.add(folder)
        localCoroutine.launch {
            _innerFiles.emit(folder.innerFiles())
        }
    }

    fun exit(source: Source, onEmptyStack: suspend () -> Unit) {
        val stack =
            if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
        if (stack.hasBackFolder()) {
            val lastFolder = stack.pop()
            onBack(source, lastFolder.parent!!)
        } else {
            localCoroutine.launch {
                onEmptyStack()
            }
        }
    }

    override fun close() {
        localCoroutine.cancel()
    }


}

fun StateFlow<List<FileEntity>?>.getFileByUUID(fileUUID: UUID): FileEntity? {
    val currentFiles = value
    if (currentFiles.isNullOrEmpty()) return null
    return currentFiles.find { it.uuid == fileUUID }
}

fun Stack<FileEntity>.hasBackFolder(): Boolean = isNotEmpty() && peek()?.parent != null
