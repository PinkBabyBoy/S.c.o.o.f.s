package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.loadKoinModules
import ru.barinov.plain_explorer.di.plainExplorerModule

class PlainExplorerInitializer: Initializer<Unit> {
    override fun create(context: Context) {
        loadKoinModules(plainExplorerModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}