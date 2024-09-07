package ru.barinov.transaction_manager

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.barinov.core.FileEntity
import ru.barinov.core.launchCatching
import ru.barinov.core.launchWithMutex
import ru.barinov.core.truncate
import ru.barinov.cryptography.KeyMemoryCache
import ru.barinov.filework.ReadFileWorker
import ru.barinov.filework.WriteFileWorker
import java.io.IOException
import java.lang.Exception
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.UUID
import kotlin.coroutines.CoroutineContext

private const val SHORT_TRANSACTION_LIMIT = 2000

internal class TransactionManagerImpl(
    private val readFileWorker: ReadFileWorker,
    private val getCurrentContainerUseCase: GetCurrentContainerUseCase,
    private val writeFieWorker: WriteFileWorker,
    private val keyCache: KeyMemoryCache,
    private val appFolderProvider: AppFolderProvider
) : TransactionManager {

    private val serviceCoroutine =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler(::handleException))

    private val _events = MutableSharedFlow<FailReason>()
    val events = _events.asSharedFlow()

    override val isKeyLoaded: StateFlow<Boolean> = keyCache.isLoaded

    private val mutex = Mutex()

    private val transactionsRegister: MutableMap<UUID, Transaction> = mutableMapOf()

    private fun handleException(coroutineContext: CoroutineContext, throwable: Throwable) {

        fun toFailReason(t: Throwable): FailReason =
            when (t) {
                else -> FailReason.UnknownInternalException
            }

        serviceCoroutine.launch {
            when (throwable) {
                is TransactionError -> _events.emit(toFailReason(throwable))
                else -> _events.emit(FailReason.UnknownInternalException)
            }
        }
    }

    override fun loadKey(
        keyFile: FileEntity,
        password: CharArray,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        serviceCoroutine.launchCatching(
            block = {
                mutex.withLock {
                    readFileWorker.readRawKey(keyFile)
                }
            },
            onSuccess = {
                keyCache.initKeyStore(it.inputStream(), password)
                onSuccess()
            },
            onError = onError
        )
    }

    override fun unbindKey() {
        keyCache.unbind()
    }

    override fun clearStoredData() {
        serviceCoroutine.launch {
            val folder = appFolderProvider.provideFolder()
            writeFieWorker.deleteEntries(folder, listOf()) // TODO
        }
    }

    override fun startTransaction(
        files: List<FileEntity>,
        onShortTransaction: (Result<Unit>) -> Unit,
        onLongTransaction: (InitialTransactionData) -> Unit
    ) {
        serviceCoroutine.launchWithMutex(mutex) {
            val size = files.sumOf { it.size.value }
            val containerData = getCurrentContainerUseCase()
            if (size > SHORT_TRANSACTION_LIMIT) {
                val result = shortTransaction(files, containerData)
                onShortTransaction(result)
            } else {
                val transaction = registerLongTransaction(files, containerData, size)
                onLongTransaction(transaction)
            }
        }
    }

    private fun shortTransaction(
        files: List<FileEntity>,
        containerData: ContainerData
    ): Result<Unit> = runCatching {
        files.forEach { file ->
            serviceCoroutine.launch {
                writeFieWorker.putInStorage(
                    targetFile = file,
                    progressFlow = null,
                    indexes = containerData.indexes,
                    container = containerData.container
                )
            }
        }
    }

    override fun startTransactionToContainer(transactionUUID: UUID) {
        val transaction = transactionsRegister[transactionUUID]
        var currentFileName = String()
        var currentIndex = 0
        suspend fun execute() {
            mutex.withLock {
                if (transaction == null) {
                    _events.emit(FailReason.TransactionNotFound)
                    return
                }
                transaction.changeState(Transaction.State.STARTED)
                val containerData = transaction.containerData

                transaction.files.forEach { file ->
                    currentIndex++
                    currentFileName = file.name.value
                    writeFieWorker.putInStorage(
                        targetFile = file,
                        progressFlow = transaction.progressFlow,
                        indexes = containerData.indexes,
                        container = containerData.container
                    )
                }
            }
        }

        fun onTransactionError(t: Throwable) {

            fun mapToReason(t: Throwable): TransactionError.Reason =
                when (t) {
                    is IOException -> TransactionError.Reason.CONNECTION_FAIL
                    is NoSuchAlgorithmException, is InvalidKeyException -> TransactionError.Reason.CIPHER_FAIL
                    else -> { error("") }
                }

            val containerData = transaction?.containerData ?: throw TransactionError(
                String(), 0, 0, TransactionError.Reason.TRANSACTION_NOT_FOUND
            )

            val container = containerData.container
            container.truncate(containerData.initialSize)
            throw TransactionError(
                fileName = currentFileName,
                resultSuccess = currentIndex,
                total = transaction.files.size,
                reason = mapToReason(t)
            )
        }

        serviceCoroutine.launchCatching(
            block = (::execute),
            onError = (::onTransactionError),
            onSuccess = { transaction?.changeState(Transaction.State.FINISHED) }
        )
    }

    private fun registerLongTransaction(
        files: List<FileEntity>,
        containerData: ContainerData,
        size: Long
    ): InitialTransactionData {
        val transactionUUID = UUID.randomUUID()

        transactionsRegister[transactionUUID] =
            Transaction(transactionUUID, MutableSharedFlow(), files, containerData)
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
