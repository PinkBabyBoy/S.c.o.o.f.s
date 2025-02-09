package ru.barinov.crypto_container_explorer

import androidx.paging.PagingData
import ru.barinov.core.FileIndex

interface CryptoRepository {

    fun openContainer() : PagingData<FileIndex>

}