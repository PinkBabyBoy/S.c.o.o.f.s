package ru.barinov.file_process_worker

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent

internal const val TYPE_KEY = "WORK_TYPE"

class ScoofWorker(private val context: Context, parameters: WorkerParameters): CoroutineWorker(context, parameters), KoinComponent {

    //private val transactionManager

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        val workType = inputData.getInt(TYPE_KEY, 0).let { WorkType.entries[it] }
        setForegroundAsync(foregroundInfo(workType))
    }

    private fun foregroundInfo(workType: WorkType): ForegroundInfo {
        val title = when(workType) {
            WorkType.ENCRYPTION -> ru.barinov.core.R.string.encryption_notification_title
            WorkType.DECRYPTION -> ru.barinov.core.R.string.decryption_notification_title
        }.let(context::getText)
        val notification: Notification = NotificationCompat.Builder(context, id.toString())
            .setContentTitle(title)
            .setTicker(title)
//            .setSmallIcon(R.drawable.ic_work_notification)
            .setOngoing(true) // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(R.drawable.ic_delete, context.getText(cancel), intent)
            .build()
    }
}

enum class WorkType{
    ENCRYPTION, DECRYPTION,
}
