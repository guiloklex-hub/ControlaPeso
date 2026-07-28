package br.com.paivalab.controlapeso.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.paivalab.controlapeso.MainActivity
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.first

class MeasurementReminderWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val preferences = AppPreferencesRepository(applicationContext)
            .preferences.first()
        if (
            !preferences.remindersEnabled ||
            !ReminderPolicy.isSelected(
                preferences.reminderDaysMask,
                LocalDate.now().dayOfWeek
            )
        ) {
            return Result.success()
        }
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        createChannel()
        val openApp = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java).putExtra(
                MainActivity.EXTRA_DESTINATION,
                MainActivity.DESTINATION_LIVE_MEASUREMENT
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.reminder_title))
            .setContentText(applicationContext.getString(R.string.reminder_body))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.reminder_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    companion object {
        const val UNIQUE_WORK_NAME = "measurement_reminder"
        private const val CHANNEL_ID = "measurement_reminders"
        private const val NOTIFICATION_ID = 3107
    }
}
