package ru.barinov.password_manager

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import ru.barinov.core.toByteArray

internal class HashValidatorImpl: HashValidator {

    override fun validate(storedHash: ByteArray, password: CharArray): Boolean {
        val argon = Argon2Kt()
        return argon.verify(
            mode = Argon2Mode.ARGON2_ID,
            encoded = storedHash.decodeToString(),
            password =  password.toByteArray(),
        )
    }
}

fun interface HashValidator {

    fun validate(storedHash: ByteArray, password: CharArray): Boolean
}
