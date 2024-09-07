package ru.barinov.component_initializer

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.GlobalContext.loadKoinModules
import ru.barinov.usb_connection.di.usbConnectionModule

class UsbConnectionInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        loadKoinModules(usbConnectionModule)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
