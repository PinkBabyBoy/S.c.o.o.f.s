package ru.barinov.cryptography.keygens

import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

interface SecretKeyGenerator {

    fun generateNewSecretKey(): SecretKey

    fun generateSyncKeyWithKeyStore(alias: String): SecretKey
}