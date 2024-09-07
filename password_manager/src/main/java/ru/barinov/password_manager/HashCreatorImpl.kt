package ru.barinov.password_manager

import android.util.Log
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import ru.barinov.core.toByteArray
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import kotlin.text.Charsets.UTF_8

private const val SALT = "FFD6F5E1"
private const val ITERATIONS_COST = 10
private const val MEMORY_COST = 1024 * 42

internal class HashCreatorImpl: HashCreator {

    override fun createHash(password: CharArray): ByteArray {
        val argon = Argon2Kt()
        val result = argon.hash(
            Argon2Mode.ARGON2_ID,
            password.toByteArray(),
            SALT.toByteArray(),
            ITERATIONS_COST,
            MEMORY_COST
        )
       return result.encodedOutputAsByteArray()
    }
}

fun interface HashCreator{

    fun createHash(password: CharArray): ByteArray
}
