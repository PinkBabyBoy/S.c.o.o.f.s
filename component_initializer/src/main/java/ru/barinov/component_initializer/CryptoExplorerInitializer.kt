package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.loadKoinModules
import ru.barinov.crypto_container_explorer.di.cryptoExplorerModule

class CryptoExplorerInitializer: Initializer<Unit> {
    override fun create(context: Context) {
        loadKoinModules(cryptoExplorerModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}