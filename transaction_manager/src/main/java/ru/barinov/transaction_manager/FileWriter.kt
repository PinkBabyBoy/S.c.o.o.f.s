package ru.barinov.transaction_manager

import ru.barinov.core.FileEntity
import java.util.UUID

interface FileWriter: Cleaner {

    fun startTransaction(
        files: List<FileEntity>,
        onShortTransaction: (Result<Unit>) -> Unit,
        onLongTransaction: (InitialTransactionData) -> Unit
    )

    fun startTransactionToContainer(transactionUUID: UUID)

    fun clearByUUID(uuid: UUID)
}