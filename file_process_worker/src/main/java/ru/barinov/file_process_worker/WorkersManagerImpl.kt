package ru.barinov.file_process_worker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class WorkersManagerImpl: WorkersManager {

    private val _hasActiveWork = MutableStateFlow(false)
    override val hasActiveWork = _hasActiveWork.asStateFlow()

    fun startEncryptWork() {}


    fun startDecryptWork() {}
}
