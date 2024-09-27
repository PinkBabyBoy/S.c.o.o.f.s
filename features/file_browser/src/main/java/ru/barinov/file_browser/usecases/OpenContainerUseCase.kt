package ru.barinov.file_browser.usecases

import ru.barinov.core.FileIndex
import ru.barinov.internal_data.IndexesProvider
import ru.barinov.read_worker.ReadFileWorker

class OpenContainerUseCase(
    private val indexesProvider: IndexesProvider,
    private val readFileWorker: ReadFileWorker
) {

    suspend operator fun invoke(name: String): Result<List<FileIndex>> {
        val container = indexesProvider.getIndex(name)
        return readFileWorker.readIndexes(container)
    }
}
