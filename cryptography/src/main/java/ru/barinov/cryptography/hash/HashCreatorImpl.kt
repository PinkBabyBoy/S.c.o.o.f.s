package ru.barinov.cryptography.hash

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import ru.barinov.core.toByteArray

private const val SALT = "FFD6F5E1"

internal class HashCreatorImpl: HashCreator {

    private val argon = Argon2Kt()

    override fun createHash(input: CharArray, hashMode: HashMode): ByteArray =
        createHash(input.toByteArray(), hashMode)

    override fun createHash(input: ByteArray, hashMode: HashMode): ByteArray {
        val result = argon.hash(
            Argon2Mode.ARGON2_ID,
            input,
            SALT.toByteArray(),
            hashMode.iterationCost,
            hashMode.memoryCost
        )
        return result.encodedOutputAsByteArray()
    }
}

interface HashCreator{

    fun createHash(input: CharArray, hashMode: HashMode): ByteArray

    fun createHash(input: ByteArray, hashMode: HashMode): ByteArray
}

enum class HashMode(val iterationCost: Int, val memoryCost: Int) {
    PASSWORD(10, 1024 * 42), KEY_HASH(4, 1024 * 12)
}
