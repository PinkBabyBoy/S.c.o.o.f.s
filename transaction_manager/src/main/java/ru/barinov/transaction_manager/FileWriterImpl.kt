package ru.barinov.transaction_manager

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.barinov.core.Addable
import ru.barinov.core.FileEntity
import ru.barinov.core.launchWithMutex
import ru.barinov.core.truncate
import ru.barinov.write_worker.WriteFileWorker
import java.lang.Exception
import java.util.UUID

private const val SHORT_TRANSACTION_LIMIT = 7

internal class FileWriterImpl(
    private val getCurrentContainerUseCase: GetCurrentContainerUseCase,
    private val writeFieWorker: WriteFileWorker,
    private val appFolderProvider: AppFolderProvider
) : FileWriter {

    private val serviceCoroutine =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, _ -> })


    private val mutex = Mutex()

    private val transactionsRegister: MutableMap<UUID, Transaction> = mutableMapOf()

    private fun handleException(throwable: Throwable) {
        //TODO
        throw throwable
    }

    override fun clearStoredData() {
        serviceCoroutine.launchWithMutex(mutex) {
            val folder = appFolderProvider.provideFolder()
            writeFieWorker.deleteEntries(folder, listOf()) // TODO
        }
    }

    override fun evaluateTransaction(
        containersName: String,
        files: List<FileEntity>,
        onEvaluated: (InitialTransactionData, Boolean) -> Unit
    ) {
        serviceCoroutine.launchWithMutex(mutex) {
            val size = files.sumOf { it.calculateSize() }
            val containerData = getCurrentContainerUseCase(containersName)
            val isLongTransaction = size > SHORT_TRANSACTION_LIMIT
            val transaction = registerTransaction(files, containerData, size)
            onEvaluated(transaction, isLongTransaction)
        }
    }

//    private suspend fun shortTransaction(
//        files: List<FileEntity>,
//        containerData: ContainerData
//    ): Result<Unit> = runCatching {
//        files.forEach { file ->
//            writeFieWorker.putInStorage(
//                0L,
//                targetFile = file,
//                progressCallback = null,
//                indexes = containerData.indexes,
//                container = containerData.container
//            )
//        }
//    }.onFailure {
//        containerData.container.truncate(containerData.initialSize)
//        containerData.indexes.truncate(containerData.initialSize)
//    }

    override suspend fun startTransactionToContainer(
        transactionUUID: UUID,
        progressCallback: (Long) -> Unit
    ) = mutex.withLock {
        val transaction = transactionsRegister[transactionUUID] ?: throw IllegalStateException()
        runCatching {
            var currentFileName = String()
            var currentIndex = 0
            transaction.changeState(Transaction.State.STARTED)
            val containerData = transaction.containerData

            transaction.files.flatMapFiles().forEach { file ->
                Log.d("@@@", "WR FILE $currentIndex")
                currentFileName = file.name.value
                writeFieWorker.putInStorage(
                    targetFile = file,
                    progressCallback = progressCallback,
                    indexes = containerData.indexes,
                    container = containerData.container
                )
                currentIndex++
            }
        }.onFailure {
            val containerData = transaction.containerData
            containerData.container.truncate(containerData.initialSize)
            containerData.indexes.truncate(containerData.initialSize)
        }.onSuccess { transaction.changeState(Transaction.State.FINISHED) }.getOrThrow()
    }

//        fun onTransactionError(t: Throwable) {
//            Log.e("@@@", "${t.stackTraceToString()}")
//            fun mapToReason(t: Throwable): TransactionError.Reason =
//                when (t) {
//                    is IOException -> TransactionError.Reason.CONNECTION_FAIL
//                    is NoSuchAlgorithmException, is InvalidKeyException -> TransactionError.Reason.CIPHER_FAIL
//                    else -> {
//                        error("")
//                    }
//                }
//
//            val containerData = transaction?.containerData ?: throw TransactionError(
//                String(), 0, 0, TransactionError.Reason.TRANSACTION_NOT_FOUND
//            )
//
//            val container = containerData.container
//            container.truncate(containerData.initialSize)
//            throw TransactionError(
//                fileName = currentFileName,
//                resultSuccess = currentIndex,
//                total = transaction.files.size,
//                reason = mapToReason(t)
//            )
//        }
//
//        return serviceCoroutine.launchCatching(
//            block = (::execute),
//            onError = (::onTransactionError),
//            onSuccess = { transaction?.changeState(Transaction.State.FINISHED) }
//        )
//}

    private fun registerTransaction(
        files: List<FileEntity>,
        containerData: ContainerData,
        size: Long
    ): InitialTransactionData {
        val transactionUUID = UUID.randomUUID()

        transactionsRegister[transactionUUID] =
            Transaction(transactionUUID, files, containerData)
        return InitialTransactionData(
            uuid = transactionUUID,
            totalSize = size
        )
    }


    override fun clearByUUID(uuid: UUID) {
        transactionsRegister.remove(uuid)
    }

}

sealed interface FailReason {
    data object InternalTransactionError : FailReason
    data object UnknownInternalException : FailReason
    data object TransactionNotFound : FailReason
}


class TransactionError(fileName: String, resultSuccess: Int, total: Int, reason: Reason) :
    Exception() {

    enum class Reason {
        CONNECTION_FAIL, INDEX_CREATION_FAIL, CIPHER_FAIL, KEY_READ_FAIL, KEY_NOT_LOADED, TRANSACTION_NOT_FOUND
    }
}

private suspend fun Collection<FileEntity>.flatMapFiles(): List<FileEntity> {
    val files = filter { !it.isDir }
    val folders = filter { it.isDir }
    return files + folders.map { (it as Addable).innerFilesAsync().values.flatMapFiles() }.flatten()
}
