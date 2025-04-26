package ru.barinov.file_browser

import kotlinx.coroutines.flow.MutableStateFlow
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.file_browser.models.EncryptedFileIndexUiModel
import java.util.UUID

class SelectedCache {

    private val selectedCache = mutableMapOf<FileId, FileEntity>()
    val cacheFlow = MutableStateFlow<HashSet<FileId>>(hashSetOf())

    fun add(fileId: FileId, file: FileEntity){
        selectedCache[fileId] = file
        cacheFlow.value = selectedCache.values.mapTo(HashSet()) { it.fileId }
    }

    fun remove(fileId: FileId){
        selectedCache.remove(fileId)
        cacheFlow.value = selectedCache.values.mapTo(HashSet()) { it.fileId }
    }

    fun removeAll() {
        selectedCache.clear()
        cacheFlow.value = hashSetOf()
    }

    operator fun get(fileId: FileId) = selectedCache[fileId]

    fun getCache(): Map<FileId, FileEntity>{
        return selectedCache.toMap()
    }

    fun getSelected(): HashSet<FileId> = selectedCache.values.mapTo(HashSet()) { it.fileId }

    fun hasSelected(fileId: FileId) = selectedCache.hasKey(fileId)
}

class IndexSelectedCache(){
    private val selectedCache = mutableMapOf<FileId, EncryptedFileIndexUiModel>()
    val cacheFlow = MutableStateFlow<HashSet<FileId>>(hashSetOf())

    fun add(fileId: FileId, file: EncryptedFileIndexUiModel){
        selectedCache[fileId] = file
        cacheFlow.value = selectedCache.values.mapTo(HashSet()) { it.fileId }
    }

    fun remove(fileId: FileId){
        selectedCache.remove(fileId)
        cacheFlow.value = selectedCache.values.mapTo(HashSet()) { it.fileId }
    }

    fun removeAll() {
        selectedCache.clear()
        cacheFlow.value = hashSetOf()
    }

    operator fun get(fileId: FileId) = selectedCache[fileId]

    fun getCache(): Map<FileId, EncryptedFileIndexUiModel>{
        return selectedCache.toMap()
    }

    fun getSelected(): HashSet<FileId> = selectedCache.values.mapTo(HashSet()) { it.fileId }

    fun hasSelected(fileId: FileId) = selectedCache.hasKey(fileId)
}

private fun <K>MutableMap<K, *>.hasKey(key: K): Boolean = keys.contains(key)
