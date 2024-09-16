package ru.barinov.cryptography

import ru.barinov.core.FileEntity


interface KeyManager: KeyStateProvider {

    fun loadKey(
        keyFile: FileEntity,
        password: CharArray,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )

    fun unbindKey()
}
