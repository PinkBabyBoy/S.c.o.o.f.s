package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.file_browser.di.fileObserverModule

class FileObserverInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(fileObserverModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
