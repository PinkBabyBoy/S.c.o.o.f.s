package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.transaction_manager.di.fileWriterModule

class TransactionManagerInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(fileWriterModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}