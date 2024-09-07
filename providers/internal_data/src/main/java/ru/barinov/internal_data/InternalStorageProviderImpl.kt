package ru.barinov.internal_data

import android.os.Environment
import ru.barinov.permission_manager.PermissionChecker
import java.io.File

internal class InternalStorageProviderImpl(
    private val permissionChecker: PermissionChecker
): InternalStorageProvider {

    override fun getInternalRoot(): File? {
        return if (permissionChecker.hasPermissionToRead()) {
            Environment.getExternalStorageDirectory()
        } else{
            null
        }
    }
}
