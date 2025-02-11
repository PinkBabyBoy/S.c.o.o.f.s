package ru.barinov.plain_explorer.interactor

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.barinov.core.FileEntity
import ru.barinov.core.FileId
import ru.barinov.core.Filepath
import ru.barinov.core.InteractableFile
import ru.barinov.core.SortType
import ru.barinov.core.Source
import ru.barinov.plain_explorer.FolderTreeAgentImpl

internal class FolderDataInteractorImpl(
    private val folderTreeAgent: FolderTreeAgentImpl,
) : FolderDataInteractor {

    override suspend fun <T : Any> getFolderFiles(
        source: Source,
        sortType: SortType,
        pageTransformation: suspend Flow<PagingData<FileEntity>>.() -> Flow<PagingData<T>>
    ): Flow<Pair<Flow<PagingData<T>>, Boolean>> {
       return  if (source == Source.MASS_STORAGE)
           getExternalFolderFiles(sortType, pageTransformation)
       else
           getInternalFolderFiles(sortType, pageTransformation)
    }

    private fun <T : Any> getInternalFolderFiles(
        sortType: SortType,
        pageTransformation: suspend Flow<PagingData<FileEntity>>.() -> Flow<PagingData<T>>
    ):  Flow<Pair<Flow<PagingData<T>>, Boolean>> =
        getPagedData(sortType, Source.INTERNAL, pageTransformation)


    private fun <T : Any> getExternalFolderFiles(
        sortType: SortType,
        pageTransformation: suspend Flow<PagingData<FileEntity>>.() -> Flow<PagingData<T>>
    ):   Flow<Pair<Flow<PagingData<T>>, Boolean>> =
        getPagedData(sortType, Source.MASS_STORAGE, pageTransformation)

    override fun openParent(source: Source, onAllowed: suspend () -> Unit) {
        folderTreeAgent.goBack(source, onAllowed)
    }

    override fun getCurrentFolderInfo(currentSource: Source): Pair<Filepath, Boolean> {
        return folderTreeAgent.getCurrentFolderInfo(currentSource)
    }

    override fun update(value: Source) {
        folderTreeAgent.update(value)
    }

    override fun getFileByID(fileId: FileId, value: Source): InteractableFile {
        return folderTreeAgent.getFileByID(fileId, value)
    }

    override fun open(fileId: FileId, value: Source) {
        folderTreeAgent.open(fileId, value)
    }

    override fun close() {
        folderTreeAgent.close()
    }

    override fun getCurrentFolder(value: Source): InteractableFile {
       return folderTreeAgent.getCurrentFolder(value)
    }


    private fun <T: Any> getPagedData(
        sortType: SortType,
        source: Source,
        pageTransformation: suspend Flow<PagingData<FileEntity>>.() -> Flow<PagingData<T>>
    ): Flow<Pair<Flow<PagingData<T>>, Boolean>> {
        val sourceFolders =
            if (source == Source.INTERNAL) folderTreeAgent.innerFiles else folderTreeAgent.massStorageFiles
        return sourceFolders.map {
            it?.values?.sort(sortType)
        }.map { sortedFiles ->
            Pager(
                config = PagingConfig(
                    prefetchDistance = PAGE_SIZE,
                    pageSize = PAGE_SIZE,
                    enablePlaceholders = false,
                    initialLoadSize = PAGE_SIZE
                ),
                pagingSourceFactory = {
                    FilesPagingSource(sortedFiles)
                }
            ).flow.run { pageTransformation() to sortedFiles.isNullOrEmpty()}
        }
    }
}
