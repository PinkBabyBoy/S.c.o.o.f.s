package ru.barinov.read_worker.util

import java.io.FilterInputStream
import java.io.InputStream
import kotlin.math.min

class LimitedInputStream(base: InputStream, private var limit: Long): FilterInputStream(base){

    override fun available(): Int {
        return min(super.available().toLong(), limit).toInt()
    }

    override fun read(): Int {
        if (limit <= 0) return -1
        val res = super.read()
        if (res != -1) limit--
        return res
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        require(off >= 0 && len >= 0 && off + len <= b.size) { "Invalid read parameters" }
        if (limit <= 0) return -1

        val bytesToRead = min(min(len.toLong(), limit), Int.MAX_VALUE.toLong()).toInt()
        val bytesRead = super.read(b, off, bytesToRead)

        if (bytesRead > 0) limit -= bytesRead
        return bytesRead
    }

    override fun skip(n: Long): Long {
        val bytesToSkip = min(n, limit)
        val skipped = super.skip(bytesToSkip)
        limit -= skipped
        return skipped
    }

}
