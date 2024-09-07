package ru.barinov.transaction_manager

import java.util.UUID

fun interface Cleaner {

    fun clearStoredData()
}