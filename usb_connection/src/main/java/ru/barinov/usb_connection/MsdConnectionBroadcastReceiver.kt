package ru.barinov.usb_connection

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.jahnen.libaums.core.BuildConfig
import me.jahnen.libaums.core.UsbMassStorageDevice
import me.jahnen.libaums.core.fs.FileSystem
import me.jahnen.libaums.core.fs.UsbFile
import ru.barinov.external_data.MSDFileSystemProvider
import ru.barinov.external_data.MSDRootProvider
import ru.barinov.external_data.MassStorageEventBus
import ru.barinov.external_data.MassStorageState
import java.io.IOException

class MsdConnectionBroadcastReceiver(
    private val context: Context
) : BroadcastReceiver(), MSDRootProvider, MSDFileSystemProvider, MassStorageEventBus {

    private var currentConnection: Pair<FileSystem, UsbMassStorageDevice>? = null

    private val _massStorageState =
        MutableStateFlow<MassStorageState>(MassStorageState.Detached)

    override val massStorageState = _massStorageState.asStateFlow()
    private val usbManager = context.getSystemService(UsbManager::class.java)

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    init {
        ContextCompat.registerReceiver(
            context, this, IntentFilter().apply {
                addAction(ACTION_USB_PERMISSION)
                addAction(ACTION_USB_STATE)
            },
            RECEIVER_NOT_EXPORTED
        )
        coroutineScope.launch {
            UsbMassStorageDevice.getMassStorageDevices(context).firstOrNull()?.let { device ->
                if (usbManager.hasPermission(device.usbDevice)) {
                    initDevice(device.usbDevice)
                } else {
                    askPermission(device.usbDevice)
                }
            }
        }
    }

    override val msdRoot: UsbFile?
        get() = (massStorageState.value as? MassStorageState.Ready)?.msdFileSystem?.rootDirectory

    override val fileSystem: FileSystem?
        get() = (massStorageState.value as? MassStorageState.Ready)?.msdFileSystem


    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        coroutineScope.launch {
            when (intent.action) {
                ACTION_USB_STATE -> {
                    if (!intent.getBooleanExtra(USB_STATE_KEY, true) &&
                        hasMassStorageAttached()
                    ) {
                        _massStorageState.emit(MassStorageState.Detached)
                        close()
                    }
                }

                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    device?.apply {
                        if (!usbManager.hasPermission(this)) {
                            askPermission(this)
                        } else {
                            initDevice(this)
                        }
                    }
                }

                UsbManager.ACTION_USB_ACCESSORY_DETACHED -> {
                    close()
                }

                ACTION_USB_PERMISSION -> {
                    val device: UsbDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                UsbManager.EXTRA_DEVICE,
                                UsbDevice::class.java
                            )
                        } else {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let { initDevice(it) }
                    } else {

                    }
                }

                else -> {}
            }
        }
    }

    private fun hasMassStorageAttached() =
        UsbMassStorageDevice.getMassStorageDevices(context).isEmpty()

    private fun close() {
        currentConnection?.second?.close()
        currentConnection = null
    }

    private fun askPermission(device: UsbDevice) {
        if (usbManager.hasPermission(device)) {
            coroutineScope.launch {
                initDevice(device)
            }
        } else {
            val permissionIntent =
                PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(ACTION_USB_PERMISSION).setPackage(context.packageName),
                    PendingIntent.FLAG_MUTABLE
                )
            usbManager.requestPermission(device, permissionIntent)
        }
    }

    private suspend fun initDevice(device: UsbDevice) = runCatching {
        UsbMassStorageDevice.getMassStorageDevices(context).find {
            it.usbDevice.deviceId == device.deviceId
        }?.let { massStorage ->
            massStorage.init()
            massStorage.partitions.firstOrNull()!!.fileSystem to massStorage
        }
    }.fold(
        onSuccess = { initializedDevice ->
            if (initializedDevice == null) throw MassStorageNotRecognizedException()
            currentConnection = initializedDevice
            initializedDevice.first.rootDirectory
            _massStorageState.emit(MassStorageState.Ready(initializedDevice.first))
        },
        onFailure = {
            if (BuildConfig.DEBUG) {
                it.printStackTrace()
            }
            handleInitException(it)
        }
    )

    private fun handleInitException(it: Throwable) {
        when (it) {

        }
    }


    companion object {
        const val ACTION_USB_PERMISSION = "ru.barinov.safestore.USB_PERMISSION"
        const val ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE"
        const val USB_STATE_KEY = "connect_state"
    }

}

class MassStorageNotRecognizedException : IOException()

