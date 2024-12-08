package ru.barinov.file_process_worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.barinov.transaction_manager.FileWriter
import java.util.UUID

internal const val TYPE_KEY = "WORK_TYPE"
internal const val TRANSACTION_ID_KEY = "TRANSACTION_ID"
internal const val IS_LONG_TRANSACTION_KEY = "IS_LONG_TRANSACTION"
internal const val TOTAL_SIZE_KEY = "TOTAL_SIZE"

class ScoofWorker(private val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters), KoinComponent {

    private val workType = inputData.getInt(TYPE_KEY, 0).let { WorkType.entries[it] }

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    private val fileWriter: FileWriter by inject()

    private val notificationBuilderBase = notificationBuilderBase()


    override suspend fun doWork(): Result {
        runCatching {
            when (workType) {
                WorkType.ENCRYPTION -> encrypt()
                WorkType.DECRYPTION -> TODO()
            }
        }.onFailure {
            if(it is CancellationException) throw it
            return Result.failure()
        }
        return Result.success()
    }

    private suspend fun encrypt() {
        Log.e("@@@", "WTF")
        val transactionId = inputData.getString(TRANSACTION_ID_KEY)!!
        val isLongJob = inputData.getBoolean(IS_LONG_TRANSACTION_KEY, false)
        val totalSize = inputData.getLong(TOTAL_SIZE_KEY, 0L)
        if (isLongJob)
            setForegroundAsync(
                ForegroundInfo(
                    1118,
                    notificationBuilderBase.setProgress(100, 0, false).build(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            )
        var accumulation: Long = 0
        fileWriter.startTransactionToContainer(UUID.fromString(transactionId)) {
            if (isLongJob) {
                Log.e("@@@", "${it}")
                accumulation += it
//                setProgress()
                val percent = (accumulation.toDouble() / it) * 100
//                notificationBuilderBase.setProgress(100, percent.toInt(), false).build().also {
//                    notificationManager.notify(1118, it)
//                }
            }

        }
    }


    private fun notificationBuilderBase(): NotificationCompat.Builder {
        createChannel()
        val cancelIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)
        val title = when (workType) {
            WorkType.ENCRYPTION -> ru.barinov.core.R.string.encryption_notification_title
            WorkType.DECRYPTION -> ru.barinov.core.R.string.decryption_notification_title
        }.let(context::getText)
        return NotificationCompat.Builder(context, id.toString())
            .setContentTitle(title)
            .setChannelId("12213")
            .setTicker(title)
            .setSmallIcon(ru.barinov.core.R.drawable.anonimus)
            .setOngoing(true) // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(
                ru.barinov.core.R.drawable.baseline_cancel_24,
                context.getText(ru.barinov.core.R.string.cancel),
                cancelIntent
            )
    }

    private fun createChannel() {
        val channel = NotificationChannel("12213", "Arra", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }
}

enum class WorkType {
    ENCRYPTION, DECRYPTION,
}
