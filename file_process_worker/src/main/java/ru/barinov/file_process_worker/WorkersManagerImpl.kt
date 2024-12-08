package ru.barinov.file_process_worker

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class WorkersManagerImpl(private val appContext: Context): WorkersManager {

    private val _hasActiveWork = MutableStateFlow(false)
    override val hasActiveWork = _hasActiveWork.asStateFlow()


    override fun startEncryptWork(transactionId: String, isLongTransaction: Boolean, totalSize: Long) {
        val data = Data.Builder()
            .putInt(TYPE_KEY, WorkType.ENCRYPTION.ordinal)
            .putString(TRANSACTION_ID_KEY, transactionId)
            .putBoolean(IS_LONG_TRANSACTION_KEY, isLongTransaction)
            .putLong(TOTAL_SIZE_KEY, totalSize)
            .build()
        val workRequest =
            OneTimeWorkRequest.Builder(ScoofWorker::class)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag("EncryptionWork")
                .setInputData(data)
                .build()
        WorkManager.getInstance(appContext).enqueue(workRequest)
    }


    override fun startDecryptWork() {}
}
