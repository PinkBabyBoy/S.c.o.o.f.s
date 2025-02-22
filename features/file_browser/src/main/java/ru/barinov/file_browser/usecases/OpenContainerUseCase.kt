package ru.barinov.file_browser.usecases

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.barinov.core.FileIndex
import ru.barinov.crypto_container_explorer.CryptoRepository

class OpenContainerUseCase(
    private val cryptoRepository: CryptoRepository
) {

    operator fun invoke(name: String): Flow<PagingData<FileIndex>> {
       return cryptoRepository.openContainer(name)
    }

}
