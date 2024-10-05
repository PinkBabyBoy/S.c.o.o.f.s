package ru.barinov.file_browser.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Filepath
import ru.barinov.core.Addable
import ru.barinov.core.Source
import ru.barinov.external_data.MassStorageEventBus
import ru.barinov.file_browser.RootNameProvider
import ru.barinov.file_browser.RootProvider
import java.io.Closeable
import java.util.Stack

//TODO interface
class FileTreeProvider(
    private val rootProvider: RootProvider,
    private val massStorageEventBus: MassStorageEventBus,
    private val rootNameProvider: RootNameProvider
) : Closeable, FileProvider {

    private val localCoroutine = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val internalRoot = rootProvider.getRootFile(Source.INTERNAL)
    private val msdRoot = rootProvider.getRootFile(Source.MASS_STORAGE)

    private val innerFolderBackStack = Stack<Addable>()
    private val massStorageFolderBackStack = Stack<Addable>()

    private val _innerFiles: MutableStateFlow<Map<FileId, FileEntity>?> =
        MutableStateFlow(internalRoot?.innerFiles())

    val innerFiles = _innerFiles.asStateFlow()

    private val _massStorageFiles: MutableStateFlow<Map<FileId, FileEntity>?> =
        MutableStateFlow(msdRoot?.innerFiles())

    val massStorageFiles = _massStorageFiles.asStateFlow()

    fun getCurrentFolderInfo(source: Source): Pair<Filepath, Boolean> {
        val stack =
            if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
        val path = if (stack.isNotEmpty()) stack.peek().path else Filepath.root("Root")
        return path to !stack.hasBackFolder()
    }

    fun getCurrentFolder(source: Source): Addable {
        val stack =
            if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
        return if (stack.isNotEmpty()) stack.peek() as Addable else rootProvider.getRootFile(source)!!
    }

    fun getCurrentList(source: Source): Collection<FileEntity>? =
        if (source == Source.INTERNAL) innerFiles.value?.values else massStorageFiles.value?.values

    override fun getFileByID(fileId: FileId, source: Source): Addable =
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
        localCoroutine.launch {
            when (source) {
                Source.INTERNAL -> _innerFiles.value = getCurrentFolder(source).innerFilesAsync()
                Source.MASS_STORAGE -> _massStorageFiles.value = getCurrentFolder(source).innerFilesAsync()
            }
        }
    }

    private suspend fun onBack(source: Source, folder: Addable) {
        when (source) {
            Source.INTERNAL -> _innerFiles.value = folder.parent?.innerFilesAsync()
            Source.MASS_STORAGE -> _massStorageFiles.value = folder.parent?.innerFilesAsync()
        }
    }

    private fun openMassStorageFolder(fileUUID: FileId) {
        localCoroutine.launch {
            val folder = massStorageFiles.getFileByID(fileUUID)
            if (folder == null || !folder.isDir) error("Can't open file")
            massStorageFolderBackStack.add(folder)
            _massStorageFiles.emit(folder.innerFilesAsync())
        }
    }

    private fun openInternalFolder(fileUUID: FileId) {
        localCoroutine.launch {
            val folder = innerFiles.getFileByID(fileUUID)
            if (folder == null || !folder.isDir) error("Can't open file")
            innerFolderBackStack.add(folder)
            _innerFiles.emit(folder.innerFilesAsync())
        }
    }

    fun goBack(source: Source, onEmptyStack: suspend () -> Unit) {
        localCoroutine.launch {
            val stack =
                if (source == Source.INTERNAL) innerFolderBackStack else massStorageFolderBackStack
            if (stack.hasBackFolder()) {
                val last = stack.pop()
                onBack(source, last!!)
            } else {
                onEmptyStack()
            }
        }
    }

    override fun close() {
        localCoroutine.cancel()
        innerFolderBackStack.clear()
        massStorageFolderBackStack.clear()
    }
}

fun StateFlow<Map<FileId, FileEntity>?>.getFileByID(fileId: FileId): Addable? {
    val currentFiles = value
    if (currentFiles.isNullOrEmpty()) return null
    return currentFiles[fileId] as? Addable
}

fun Stack<Addable>.hasBackFolder(): Boolean =
    isNotEmpty() && peek()?.parent != null
