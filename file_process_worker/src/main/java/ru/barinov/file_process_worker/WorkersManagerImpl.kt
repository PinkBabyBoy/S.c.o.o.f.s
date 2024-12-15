package ru.barinov.file_process_worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.barinov.preferences.AppPreferences
import java.util.UUID

private const val UNIQUE_WORK_NAME = "ScoofWorkingHard"
private const val ENC_TAG = "EncryptionWork"
private const val DEC_TAG = "DecryptionWork"

internal class WorkersManagerImpl(
    private val appContext: Context,
    private val preferences: AppPreferences
): WorkersManager {

    private val _hasActiveWork = MutableStateFlow(false)
    override val hasActiveWork = _hasActiveWork.asStateFlow()
    private val manager = WorkManager.getInstance(appContext)



    override fun startEncryptWork(transactionId: String, isLongTransaction: Boolean, totalSize: Long) {
        val id = preferences.workId?.let(UUID::fromString) ?: return
        val data = Data.Builder()
            .putInt(TYPE_KEY, WorkType.ENCRYPTION.ordinal)
            .putString(TRANSACTION_ID_KEY, transactionId)
            .putBoolean(IS_LONG_TRANSACTION_KEY, isLongTransaction)
            .putLong(IS_LONG_TRANSACTION_KEY, totalSize)
            .build()
        val workRequest =
            OneTimeWorkRequest.Builder(ScoofWorker::class)
                .setId(id)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag(ENC_TAG)
                .setInputData(data)
                .build()
        manager.beginUniqueWork(
            uniqueWorkName = UNIQUE_WORK_NAME,
            existingWorkPolicy = ExistingWorkPolicy.APPEND_OR_REPLACE,
            request = workRequest
        ).enqueue()
    }

    override fun startDecryptWork() {}
}
