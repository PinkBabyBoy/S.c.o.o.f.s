package ru.barinov.cryptography

import ru.barinov.core.FileEntity
import ru.barinov.core.Openable


interface KeyManager: KeyStateProvider {

    fun loadKey(
        keyFile: Openable,
        password: CharArray,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )

    fun unbindKey()
}
