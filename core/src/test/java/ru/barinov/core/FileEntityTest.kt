package ru.barinov.core

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File


@RunWith(JUnit4::class)
class FileEntityTest {

    @Test
    fun `FileId creation from filepath should work correctly`() {
        val filepath = Filepath("test/path/file.txt")
        val fileId = FileId.byFilePath(filepath)
        assertEquals("test/path/file.txt", fileId.value)
    }

    @Test
    fun `FileId creation from name should work correctly`() {
        val name = "test.txt"
        val fileId = FileId.byName(name)
        assertEquals(name, fileId.value)
    }

    @Test
    fun `FileId creation from pointer should work correctly`() {
        val pointer = 12345L
        val fileId = FileId.byPointer(pointer)
        assertEquals(pointer.toString(), fileId.value)
    }

    @Test
    fun `FileId restoration should work correctly`() {
        val originalId = "test-id"
        val restoredId = FileId.restore(originalId)
        assertEquals(originalId, restoredId.value)
    }

    @Test
    fun `File to InternalFile conversion should work correctly`() {
        val file = File("test.txt")
        val internalFile = file.toInternalFileEntity()
        assertEquals(file.name, internalFile.name.value)
        assertEquals(file.path, internalFile.path.value)
        assertEquals(file.isDirectory, internalFile.isDir)
    }

    @Test
    fun `File to ContainerFile conversion should work correctly`() {
        val file = File("container.dat")
        val containerFile = file.toContainerFileEntity()
        assertEquals(file.name, containerFile.name.value)
        assertEquals(file.path, containerFile.path.value)
        assertEquals(file.isDirectory, containerFile.isDir)
    }
} 