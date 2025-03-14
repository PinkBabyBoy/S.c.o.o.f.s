package ru.barinov.core

import android.content.res.Resources
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.nio.ByteBuffer
import java.util.Locale

fun File.truncate(toSize: Long){
    outputStream().channel.truncate(toSize)
}

fun CoroutineScope.launchWithMutex(mutex: Mutex, block: suspend () -> Unit) =
    launch { mutex.withLock{ block() } }

fun <R>CoroutineScope.launchCatching(
    block: suspend () -> R,
    onError: (Throwable) -> Unit = {},
    onSuccess: (R) -> Unit = {}
): Job {
    suspend fun execute(){
        runCatching { block() }.fold(
            onFailure = onError,
            onSuccess = onSuccess
        )
    }

    return launch { execute() }
}

fun CharArray.toByteArray(): ByteArray {
    val buffer = ByteArray(size * 2)
    forEachIndexed { i, c ->
        buffer[i * 2] = (c.code shr 8).toByte()
        buffer[i * 2 + 1] = c.code.toByte()
    }
    return buffer
}

inline fun <T> Iterable<T>.forEachScoped(action: T.() -> Unit) {
    for (element in this) action(element)
}

fun Int.mb(): Long = this * 1_000_000L


fun Long.bytesToMbSting() =
    "${String.format(Locale.getDefault(), "%.2f", (this.toFloat()/  1_000_000).toMinDisplayable())} mb"

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

fun Short.getBytes(): ByteArray{
    return ByteBuffer.allocate(Short.SIZE_BYTES).putShort(this).array()

}

fun Int.getBytes(): ByteArray{
    return ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()
}

fun Long.mb(): Float = (this / (1024 * 1024)).toFloat()

fun String.hasNoSpecialSymbols() = !matches("^[a-zA-Z0-9]{4,10}\$".toRegex())

fun getScreenHeightWithLOffset(offsetTopDp: Int): Int {
   return Resources.getSystem().displayMetrics.heightPixels.pxToDp() - offsetTopDp
}

fun Int.pxToDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

