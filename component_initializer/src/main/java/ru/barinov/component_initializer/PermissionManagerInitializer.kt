package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.permission_manager.di.permissionManagerModule

class PermissionManagerInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(permissionManagerModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
