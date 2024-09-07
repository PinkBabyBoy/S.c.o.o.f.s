package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.cryptography.di.cryptographyModule

class CryptographyInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(cryptographyModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
