package ru.barinov.file_browser

import android.graphics.Path.Op
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Filepath
import ru.barinov.core.Openable
import ru.barinov.core.Source
import ru.barinov.external_data.MassStorageEventBus
import java.io.Closeable
import java.util.Stack

//TODO interface
class FileTreeProvider(
    private val rootProvider: RootProvider,
    private val massStorageEventBus: MassStorageEventBus,
    private val rootNameProvider: RootNameProvider
) : Closeable {

    private val localCoroutine = CoroutineScope(Job() + Dispatchers.IO)
    private val internalRoot = rootProvider.getRootFile(Source.INTERNAL)
    private val msdRoot = rootProvider.getRootFile(Source.MASS_STORAGE)

    private val innerFolderBackStack = Stack<Openable>()
    private val massStorageFolderBackStack = Stack<Openable>()

    private val _innerFiles: MutableStateFlow<Map<FileId, FileEntity>?> =
        MutableStateFlow(internalRoot?.innerFiles())

    val innerFiles = _innerFiles.asStateFlow()

    private val _massStorageFiles: MutableStateFlow<Map<FileId, FileEntity>?> =
        MutableStateFlow(msdRoot?.innerFiles())

    val massStorageFiles = _massStorageFiles.asStateFlow()

    fun getCurrentFolderInfo(source: Source): Pair<Filepath, Boolean> {
        val stack =
            if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
        val path = if(stack.isNotEmpty()) stack.peek().path else Filepath.root("Root")
        return path to !stack.hasBackFolder()
    }

    fun getCurrentFolder(source: Source): Openable {
        val stack =
            if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
        return if(stack.isNotEmpty()) stack.peek() as Openable else rootProvider.getRootFile(source)!!
    }

    fun getCurrentList(source: Source): Collection<FileEntity>? =
        if (source == Source.INTERNAL) innerFiles.value?.values else massStorageFiles.value?.values

    fun getFileByID(fileId: FileId, source: Source): Openable =
        when (source) {
            Source.INTERNAL -> innerFiles.getFileByID(fileId)
            Source.MASS_STORAGE -> massStorageFiles.getFileByID(fileId)
        } ?: error("File is not found")

    fun open(fileId: FileId, source: Source) {
        when (source) {
            Source.INTERNAL -> openInternalFolder(fileId)
            Source.MASS_STORAGE -> openMassStorageFolder(fileId)
        }
    }

    fun update(source: Source) {
        when(source){
            Source.INTERNAL -> _innerFiles.value = getCurrentFolder(source).innerFiles()
            Source.MASS_STORAGE -> _massStorageFiles.value = getCurrentFolder(source).innerFiles()
        }

    }

    private fun onBack(source: Source, folder: Openable) {
        when (source) {
            Source.INTERNAL -> _innerFiles.value = folder.parent?.innerFiles()
            Source.MASS_STORAGE -> _massStorageFiles.value = folder.parent?.innerFiles()
        }
    }

    private fun openMassStorageFolder(fileUUID: FileId) {
        val folder = massStorageFiles.getFileByID(fileUUID)
        if (folder == null || !folder.isDir) error("Can't open file")
        massStorageFolderBackStack.add(folder)
        localCoroutine.launch {
            _massStorageFiles.emit(folder.innerFiles())
        }

    }

    private fun openInternalFolder(fileUUID: FileId) {
        val folder = innerFiles.getFileByID(fileUUID)
        if (folder == null || !folder.isDir) error("Can't open file")
        innerFolderBackStack.add(folder)
        localCoroutine.launch {
            _innerFiles.emit(folder.innerFiles())
        }
    }

    fun goBack(source: Source, onEmptyStack: suspend () -> Unit) {
        val stack =
            if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
        if (stack.hasBackFolder()) {
            val last = stack.pop()
            onBack(source, last!!)
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

fun StateFlow<Map<FileId, FileEntity>?>.getFileByID(fileId: FileId): Openable? {
    val currentFiles = value
    if (currentFiles.isNullOrEmpty()) return null
    return currentFiles[fileId] as? Openable
}

fun Stack<Openable>.hasBackFolder(): Boolean =
    isNotEmpty() && peek()?.parent != null
