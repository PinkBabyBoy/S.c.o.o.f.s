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

private const val ENC_TAG = "EncryptionWork"
private const val DEC_TAG = "DecryptionWork"

internal class WorkersManagerImpl(
    appContext: Context,
    private val preferences: AppPreferences
): WorkersManager {

    private val _hasActiveWork = MutableStateFlow(false)
    override val hasActiveWork = _hasActiveWork.asStateFlow()
    private val manager = WorkManager.getInstance(appContext)



    override fun startEncryptWork(transactionId: String, isLongTransaction: Boolean, totalSize: Long) {
        val uName = preferences.workUniqName?: return
        val data = Data.Builder()
            .putInt(TYPE_KEY, WorkType.ENCRYPTION.ordinal)
            .putString(TRANSACTION_ID_KEY, transactionId)
            .putBoolean(IS_LONG_TRANSACTION_KEY, isLongTransaction)
            .putLong(TOTAL_SIZE_KEY, totalSize)
            .build()
        val workRequest =
            OneTimeWorkRequest.Builder(ScoofWorker::class)
                .setId(transactionId.let(UUID::fromString))
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag(ENC_TAG)
                .setInputData(data)
                .build()
        manager.beginUniqueWork(
            uniqueWorkName = uName,
            existingWorkPolicy = ExistingWorkPolicy.APPEND_OR_REPLACE,
            request = workRequest
        ).enqueue()
    }

    override fun startDecryptWork() {}
}
