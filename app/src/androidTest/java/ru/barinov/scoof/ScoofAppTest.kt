package ru.barinov.scoof

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Инструментальный тест для проверки инициализации приложения
 */
@RunWith(AndroidJUnit4::class)
class ScoofAppTest {

    @Test
    fun testApplicationContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull("Контекст приложения не должен быть null", context)
    }
    
    @Test
    fun testAppClassInitialization() {
        val application = ApplicationProvider.getApplicationContext<ScoofApp>()
        assertNotNull("Экземпляр приложения не должен быть null", application)
    }
} 