package ru.barinov.crypto_container_explorer

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.barinov.core.FileIndex

interface CryptoRepository {

   fun openContainer(name: String) : Flow<PagingData<FileIndex>>

}