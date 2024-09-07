package ru.barinov.cryptography

class TagCreatorImpl: TagCreator {

    override fun createPHashTag(size: Int, type: Int): ByteArray {
        return "PH$type$size".toByteArray()
    }
}