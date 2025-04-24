package ru.barinov.transaction_manager

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CleanerTest {

    private class TestCleaner : Cleaner {
        var clearCount = 0
        override fun clearStoredData() {
            clearCount++
        }
    }

    @Test
    fun `Cleaner should execute clearStoredData`() {
        val cleaner = TestCleaner()
        cleaner.clearStoredData()
        assertEquals(1, cleaner.clearCount)
    }

    @Test
    fun `Cleaner should execute clearStoredData multiple times`() {
        val cleaner = TestCleaner()
        cleaner.clearStoredData()
        cleaner.clearStoredData()
        cleaner.clearStoredData()
        assertEquals(3, cleaner.clearCount)
    }
} 