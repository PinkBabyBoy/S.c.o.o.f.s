package ru.barinov.plain_explorer.interactor

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Filepath
import ru.barinov.core.InteractableFile
import ru.barinov.core.SortType
import ru.barinov.core.Source

interface FolderDataInteractor {

    suspend fun <T : Any> getFolderFiles(
        source: Source,
        sortType: SortType = SortType.AS_IS,
        pageTransformation: suspend Flow<PagingData<FileEntity>>.() -> Flow<PagingData<T>>
    ):   Flow<Pair<Flow<PagingData<T>>, Boolean>>

    fun openParent(source: Source, onAllowed: suspend () -> Unit)

    fun getCurrentFolderInfo(currentSource: Source): Pair<Filepath, Boolean>

    fun update(value: Source)
    fun getFileByID(fileId: FileId, value: Source): InteractableFile
    fun open(fileId: FileId, value: Source)
    fun close()
    fun getCurrentFolder(value: Source): InteractableFile

}