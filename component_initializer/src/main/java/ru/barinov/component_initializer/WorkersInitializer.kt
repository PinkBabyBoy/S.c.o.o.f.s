package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.file_process_worker.di.workersModule

class WorkersInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(workersModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
