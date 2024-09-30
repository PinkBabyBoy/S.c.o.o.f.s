package ru.barinov.core

import android.content.Context
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.util.Locale
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


fun Long.bytesToMbSting() =
    "${String.format(Locale.getDefault(), "%.2f", (this.toFloat()/ 1024 / 1024).toMinDisplayable())} mb"

internal fun Float.toMinDisplayable(): Float {
    return if(this < 0.01f) 0.01f else this
}

fun String.trimFilePath(): String {
    val segments = split('/').filter { it.isNotEmpty() }
    return when {
        segments.size < 3 -> this.trimFileName(12)
        else
        -> "${segments.first().trimFileName(12)}${File.separator}...${File.separator}${segments.last().trimFileName(12)}"
    }
}

fun String.folderName(): String = split('/').last()

fun String.trimFileName(limit: Int): String = when{
    length <= limit -> this
    else -> {
        val segments = split('.')
        if(segments.isEmpty() || segments.size == 1){
            take(limit)
        } else {
            "${segments.take(segments.size - 1)
                .joinToString()
                .replace(" ", "")
                .replace(",", ".")
                .take(limit)}...${segments.last()}"
        }
    }
}

fun Long.getBytes(): ByteArray{
    return ByteBuffer.allocate(Long.SIZE_BYTES).putLong(this).array()

}

fun Int.getBytes(): ByteArray{
    return ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()
}

fun Long.mb(): Float = (this / (1024 * 1024)).toFloat()

fun String.hasNoSpecialSymbols() = !matches("^[a-zA-Z0-9]{4,10}\$".toRegex())

