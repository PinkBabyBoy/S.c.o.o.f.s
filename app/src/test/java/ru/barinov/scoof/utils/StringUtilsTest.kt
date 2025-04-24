package ru.barinov.scoof.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilsTest {

    @Test
    fun testCapitalize() {
        assertEquals("Hello", StringUtils.capitalize("hello"))
        assertEquals("Hello", StringUtils.capitalize("Hello"))
        assertEquals("", StringUtils.capitalize(""))
        assertEquals("A", StringUtils.capitalize("a"))
    }


    @Test
    fun testReverse() {
        assertEquals("olleh", StringUtils.reverse("hello"))
        assertEquals("", StringUtils.reverse(""))
        assertEquals("a", StringUtils.reverse("a"))
        assertEquals("54321", StringUtils.reverse("12345"))
    }
} 