package ru.barinov.cryptography.hash

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import ru.barinov.core.toByteArray

private const val SALT = "FFD6F5E1"
private const val ITERATIONS_COST = 10
private const val MEMORY_COST = 1024 * 42

internal class HashCreatorImpl: HashCreator {

    private val argon = Argon2Kt()

    override fun createHash(input: CharArray): ByteArray =
        createHash(input.toByteArray())

    override fun createHash(input: ByteArray): ByteArray {
        val result = argon.hash(
            Argon2Mode.ARGON2_ID,
            input,
            SALT.toByteArray(),
            ITERATIONS_COST,
            MEMORY_COST
        )
        return result.encodedOutputAsByteArray()
    }
}

interface HashCreator{

    fun createHash(input: CharArray): ByteArray

    fun createHash(input: ByteArray): ByteArray
}
