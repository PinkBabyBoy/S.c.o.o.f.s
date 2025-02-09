package ru.barinov.plain_explorer.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.barinov.core.FileEntity
import ru.barinov.core.SortType
import ru.barinov.core.Source
import ru.barinov.plain_explorer.FileTreeProvider

internal class PlainRepositoryImpl(
    private val fileTreeProvider: FileTreeProvider,
) : PlainDataRepository {

    override suspend fun getInternalFolderFiles(sortType: SortType): Flow<Flow<PagingData<FileEntity>>> =
        getPagedData(sortType, Source.INTERNAL)


    override suspend fun getExternalFolderFiles(sortType: SortType): Flow<Flow<PagingData<FileEntity>>> =
        getPagedData(sortType, Source.MASS_STORAGE)


    private fun getPagedData(
        sortType: SortType,
        source: Source
    ): Flow<Flow<PagingData<FileEntity>>> {
        val sourceFolders =
            if (source == Source.INTERNAL) fileTreeProvider.innerFiles else fileTreeProvider.massStorageFiles
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
            ).flow
        }
    }
}
