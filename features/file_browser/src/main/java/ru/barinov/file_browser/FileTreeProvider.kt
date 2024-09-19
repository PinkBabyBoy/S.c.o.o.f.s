package ru.barinov.file_browser

import android.util.Log
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
    private val internalRoot = rootProvider.getRootFile(Source.INTERNAL)
    private val msdRoot = rootProvider.getRootFile(Source.MASS_STORAGE)

    private val innerFolderBackStack = Stack<Map<FileEntity, List<FileEntity>>>()
    private val massStorageFolderBackStack = Stack<Map<FileEntity, List<FileEntity>>>()

    private val _innerFiles: MutableStateFlow<List<FileEntity>?> =
        MutableStateFlow(internalRoot?.innerFiles())

    val innerFiles = _innerFiles.asStateFlow()

    private val _massStorageFiles: MutableStateFlow<List<FileEntity>?> =
        MutableStateFlow(msdRoot?.innerFiles())

    val massStorageFiles = _massStorageFiles.asStateFlow()

    fun getCurrentFolderInfo(source: Source): Pair<Filepath, Boolean> {
        val stack =
            if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
        val path = if(stack.isNotEmpty()) stack.peek().keys.first().path else Filepath.root("Root")
        return path to !stack.hasBackFolder()
    }

    fun getCurrentFolder(source: Source): FileEntity {
        val stack =
            if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
        return if(stack.isNotEmpty()) stack.peek().keys.first() else rootProvider.getRootFile(source)!!
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
        //FIXME(update is not correct)
        when(source){
            Source.INTERNAL -> _innerFiles.value = getCurrentFolder(source).innerFiles()
            Source.MASS_STORAGE -> _massStorageFiles.value = getCurrentFolder(source).innerFiles()
        }

    }

    private fun onBack(source: Source, folder: FileEntity, folderFiles: List<FileEntity>) {
        when (source) {
            Source.INTERNAL -> _innerFiles.value = folderFiles
            Source.MASS_STORAGE -> _massStorageFiles.value = folderFiles
        }
    }

    private fun openMassStorageFolder(fileUUID: UUID) {
        val folder = massStorageFiles.getFileByUUID(fileUUID)
        if (folder == null || !folder.isDir) error("Can't open file")
        massStorageFolderBackStack.add(mapOf(folder to massStorageFiles.value!!))
        localCoroutine.launch {
            _massStorageFiles.emit(folder.innerFiles())
        }

    }

    private fun openInternalFolder(fileUUID: UUID) {
        val folder = innerFiles.getFileByUUID(fileUUID)
        if (folder == null || !folder.isDir) error("Can't open file")
        innerFolderBackStack.add(mapOf(folder to innerFiles.value!!))
        localCoroutine.launch {
            _innerFiles.emit(folder.innerFiles())
        }
    }

    fun goBack(source: Source, onEmptyStack: suspend () -> Unit) {
        val stack =
            if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
        if (stack.hasBackFolder()) {
            val last = stack.pop()
            val lastFolder = last.keys.first()
            onBack(source, lastFolder.parent!!, last[lastFolder]!!)
        } else {
            localCoroutine.launch { onEmptyStack() }
        }
    }

    override fun close() {
        localCoroutine.cancel()
        innerFolderBackStack.clear()
        massStorageFolderBackStack.clear()
    }


}

fun StateFlow<List<FileEntity>?>.getFileByUUID(fileUUID: UUID): FileEntity? {
    val currentFiles = value
    if (currentFiles.isNullOrEmpty()) return null
    return currentFiles.find { it.uuid == fileUUID } //hashset
}

fun Stack<Map<FileEntity, List<FileEntity>>>.hasBackFolder(): Boolean =
    isNotEmpty() && peek()?.keys?.first()?.parent != null
