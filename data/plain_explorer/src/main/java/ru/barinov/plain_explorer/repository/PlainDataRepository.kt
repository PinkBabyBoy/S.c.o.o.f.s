package ru.barinov.plain_explorer.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.barinov.core.FileEntity
import ru.barinov.core.SortType

interface PlainDataRepository {

    suspend fun getInternalFolderFiles(sortType: SortType): Flow<Flow<PagingData<FileEntity>>>

    suspend fun getExternalFolderFiles(sortType: SortType): Flow<Flow<PagingData<FileEntity>>>

}