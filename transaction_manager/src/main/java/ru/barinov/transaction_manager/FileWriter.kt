package ru.barinov.transaction_manager

import ru.barinov.core.InteractableFile
import java.util.UUID

interface FileWriter: Cleaner {

    fun evaluateTransaction(
        containersName: String,
        files: List<InteractableFile>,
        onEvaluated: suspend (InitialTransactionData, Boolean) -> Unit
    )

    suspend fun startTransactionToContainer(transactionUUID: UUID, progressCallback: suspend (Long) -> Unit)

    fun clearByUUID(uuid: UUID)
}
