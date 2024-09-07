package ru.barinov.cryptography

interface TagCreator {

    fun createPHashTag(size: Int, type: Int): ByteArray
}