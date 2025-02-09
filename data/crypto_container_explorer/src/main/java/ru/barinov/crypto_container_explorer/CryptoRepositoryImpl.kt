package ru.barinov.crypto_container_explorer

import androidx.paging.PagingData
import ru.barinov.core.FileIndex
import ru.barinov.internal_data.IndexesProvider
import ru.barinov.read_worker.ReadFileWorker

internal class CryptoRepositoryImpl(
    private val indexesProvider: IndexesProvider,
    private val readFileWorker: ReadFileWorker
): CryptoRepository {


    override fun openContainer(): PagingData<FileIndex> {
        TODO("Not yet implemented")
    }
}