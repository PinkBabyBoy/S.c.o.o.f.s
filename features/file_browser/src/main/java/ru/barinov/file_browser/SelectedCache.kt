package ru.barinov.file_browser

import kotlinx.coroutines.flow.MutableStateFlow
import ru.barinov.core.FileEntity
import java.util.UUID

class SelectedCache {

    private val selectedCache = mutableMapOf<UUID, FileEntity>()
    val cacheFlow = MutableStateFlow<HashSet<UUID>>(hashSetOf())

    fun add(uuid: UUID, file: FileEntity){
        selectedCache[uuid] = file
        cacheFlow.value = selectedCache.values.mapTo(HashSet()) { it.uuid }
    }

    fun remove(uuid: UUID){
        selectedCache.remove(uuid)
        cacheFlow.value = selectedCache.values.mapTo(HashSet()) { it.uuid }
    }

    fun removeAll(){
        selectedCache.clear()
        cacheFlow.value = hashSetOf()
    }

    operator fun get(uuid: UUID) = selectedCache[uuid]

    fun getCache(): Map<UUID, FileEntity>{
        return selectedCache.toMap()
    }

    fun getSelected(): HashSet<UUID> = selectedCache.values.mapTo(HashSet()) { it.uuid }

    fun hasSelected(uuid: UUID) = selectedCache.hasKey(uuid)
}

private fun <K>MutableMap<K, *>.hasKey(key: K): Boolean = keys.contains(key)
