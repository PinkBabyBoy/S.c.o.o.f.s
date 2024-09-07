package ru.barinov.transaction_manager

import ru.barinov.core.FileEntity
import ru.barinov.transactionsmanager.KeyStateProvider

interface KeyManager: KeyStateProvider {

    fun loadKey(
        keyFile: FileEntity,
        password: CharArray,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )

    fun unbindKey()
}
