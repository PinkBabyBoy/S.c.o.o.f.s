package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.read_worker.di.readWorkerModule
import ru.barinov.write_worker.di.writeWorkerModule

class FileWorksInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(readWorkerModule)
        loadKoinModules(writeWorkerModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
