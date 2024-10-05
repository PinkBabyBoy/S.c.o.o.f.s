package ru.barinov.cryptography.hash

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import ru.barinov.core.toByteArray
import java.nio.ByteBuffer

internal class HashValidatorImpl: HashValidator {

    private val argon = Argon2Kt()

    override fun validate(storedHash: ByteArray, input: CharArray): Boolean {
        return validate(storedHash, input.toByteArray())
    }

    override fun validate(storedHash: ByteArray, input: ByteArray): Boolean {
        return argon.verify(
            mode = Argon2Mode.ARGON2_ID,
            encoded = storedHash.decodeToString(),
            password =  input,
        )
    }
}

interface HashValidator {

    fun validate(storedHash: ByteArray, input: CharArray): Boolean

    fun validate(storedHash: ByteArray, input: ByteArray): Boolean
}
