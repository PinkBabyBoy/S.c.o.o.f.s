package ru.barinov.file_browser.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.barinov.core.FileIndex
import ru.barinov.crypto_container_explorer.CryptoRepository

class OpenContainerUseCase(
    private val cryptoRepository: CryptoRepository
) {

    operator fun invoke(name: String): Flow<List<FileIndex>> = flow {
//        val container = indexesProvider.getIndex(name)
//        readFileWorker.readIndexes(container).fold(
//            onFailure = {
//                Log.d("@@@", it.stackTraceToString())
//                it.printStackTrace()
//                emit(emptyList())
//            },
//            onSuccess = { emit(it) }
//        )
    }

}
