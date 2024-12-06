package ru.barinov.file_process_worker

import kotlinx.coroutines.flow.StateFlow

interface WorkersManager {

    val hasActiveWork: StateFlow<Boolean>

    fun startEncryptWork(transactionId: String, isLongTransaction: Boolean, totalSize: Long)

    fun startDecryptWork()
}
