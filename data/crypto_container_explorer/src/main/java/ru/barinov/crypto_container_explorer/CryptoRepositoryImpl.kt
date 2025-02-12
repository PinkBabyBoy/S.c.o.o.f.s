package ru.barinov.crypto_container_explorer

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.barinov.core.FileIndex
import ru.barinov.internal_data.IndexesProvider
import ru.barinov.read_worker.ReadFileWorker

internal class CryptoRepositoryImpl(
    private val indexesProvider: IndexesProvider,
    private val readFileWorker: ReadFileWorker
): CryptoRepository {


    override fun openContainer(name: String): Flow<PagingData<FileIndex>> {
        val indexFile = indexesProvider.getIndex(name)
        return Pager(
            config = PagingConfig(
                prefetchDistance = PAGE_SIZE,
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSize = PAGE_SIZE
            ),
            pagingSourceFactory = {
                val loader: suspend (Long, Int) -> List<FileIndex> =  { offsetPointer, limit ->
                    readFileWorker.getIndexesByOffsetAndLimit(indexFile, offsetPointer, limit)
                }
                IndexesPagingSource(loader)
            }
        ).flow
    }
}
