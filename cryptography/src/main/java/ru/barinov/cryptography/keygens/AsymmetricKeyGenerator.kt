package ru.barinov.cryptography.keygens

import java.security.KeyPair

fun interface AsymmetricKeyGenerator {

    fun generateNewKeyPair(): KeyPair

}
