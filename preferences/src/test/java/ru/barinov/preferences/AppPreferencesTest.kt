package ru.barinov.preferences

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(JUnit4::class)
class AppPreferencesTest {

    private class TestAppPreferences : AppPreferences {
        override var tPass: String? = null
        override var fPass: String? = null
        override var iv: String? = null
        override var shownOnBoardings: Set<String>? = null
        override var workUniqName: String? = null
    }

    @Test
    fun `AppPreferences should correctly store and retrieve tPass`() {
        val preferences = TestAppPreferences()
        preferences.tPass = "test_password"
        assertEquals("test_password", preferences.tPass)
    }

    @Test
    fun `AppPreferences should correctly store and retrieve fPass`() {
        val preferences = TestAppPreferences()
        preferences.fPass = "file_password"
        assertEquals("file_password", preferences.fPass)
    }

    @Test
    fun `AppPreferences should correctly store and retrieve iv`() {
        val preferences = TestAppPreferences()
        preferences.iv = "initialization_vector"
        assertEquals("initialization_vector", preferences.iv)
    }

    @Test
    fun `AppPreferences should correctly store and retrieve shownOnBoardings`() {
        val preferences = TestAppPreferences()
        val boardings = setOf("boarding1", "boarding2")
        preferences.shownOnBoardings = boardings
        assertEquals(boardings, preferences.shownOnBoardings)
    }

    @Test
    fun `AppPreferences should correctly store and retrieve workUniqName`() {
        val preferences = TestAppPreferences()
        preferences.workUniqName = "unique_name"
        assertEquals("unique_name", preferences.workUniqName)
    }

    @Test
    fun `AppPreferences should handle null values`() {
        val preferences = TestAppPreferences()
        assertNull(preferences.tPass)
        assertNull(preferences.fPass)
        assertNull(preferences.iv)
        assertNull(preferences.shownOnBoardings)
        assertNull(preferences.workUniqName)
    }
} 