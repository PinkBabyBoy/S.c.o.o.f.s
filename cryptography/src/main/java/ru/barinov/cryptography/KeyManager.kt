package ru.barinov.cryptography

import ru.barinov.core.Addable


interface KeyManager: KeyStateProvider {

    fun loadKey(
        keyFile: Addable,
        password: CharArray,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )

    fun unbindKey()
}
