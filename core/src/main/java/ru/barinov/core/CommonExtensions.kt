package ru.barinov.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.nio.CharBuffer
import kotlin.text.Charsets.UTF_8

fun <T> Flow<T>.mutableStateIn(
    scope: CoroutineScope,
    initialValue: T
): MutableStateFlow<T> {
    val flow = MutableStateFlow(initialValue)

    scope.launch {
        this@mutableStateIn.collect(flow)
    }

    return flow
}

fun File.truncate(toSize: Long){
    outputStream().channel.truncate(toSize)
}

fun CoroutineScope.launchWithMutex(mutex: Mutex, block: suspend () -> Unit) =
    launch { mutex.withLock{ block() } }

fun <R>CoroutineScope.launchCatching(
    block: suspend () -> R,
    onError: (Throwable) -> Unit = {},
    onSuccess: (R) -> Unit = {}
){
    suspend fun execute(){
        runCatching { block() }.fold(
            onFailure = onError,
            onSuccess = onSuccess
        )
    }

    launch { execute() }
}

fun CharArray.toByteArray(): ByteArray {
    val buffer = ByteArray(size * 2)
    forEachIndexed { i, c ->
        buffer[i * 2] = (c.code shr 8).toByte()
        buffer[i * 2 + 1] = c.code.toByte()
    }
    return buffer
}

inline fun <T> Iterable<T>.forEachScoped(action: T.() -> Unit): Unit {
    for (element in this) action(element)
}