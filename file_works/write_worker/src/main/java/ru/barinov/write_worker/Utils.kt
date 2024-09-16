package ru.barinov.write_worker

import java.nio.ByteBuffer

fun Long.getBytes(): ByteArray{
    return ByteBuffer.allocate(Long.SIZE_BYTES).putLong(this).array()

}

fun Int.getBytes(): ByteArray{
    return ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()
}

fun Long.getMbs(): Float = (this / (1024 * 1024)).toFloat()