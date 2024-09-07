package ru.barinov.usb_connection.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.binds
import org.koin.dsl.module
import ru.barinov.external_data.MSDFileSystemProvider
import ru.barinov.external_data.MSDRootProvider
import ru.barinov.external_data.MassStorageEventBus
import ru.barinov.usb_connection.MsdConnectionBroadcastReceiver

val usbConnectionModule = module {

    single {
        MsdConnectionBroadcastReceiver(androidContext())
    } binds arrayOf(MSDRootProvider::class, MSDFileSystemProvider::class, MassStorageEventBus::class)
}
