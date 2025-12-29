package com.noxi.noxiapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import java.util.concurrent.TimeUnit
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Bildirim yöneticisi
 */
object NotificationHelper {
    private const val CHANNEL_ID = "workout_reminder_channel"
    private const val CHANNEL_NAME = "Antrenman Hatırlatıcıları"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Antrenman günü geldiğinde hatırlatma yapar"
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    // Basit bir bildirim gönder
    fun showNotification(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Varsayılan ikon, kendi ikonunuzla değiştirin
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            
        val notificationManager = 
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
    
    // Yarınki antrenman için planlama yap (WorkManager kullanarak)
    // Basitçe her gün sabah 09:00'da çalışacak bir Worker planlayabiliriz
    // Worker içinde veritabanı/dosya kontrolü yapılıp o gün "plannedDayId" varsa bildirim atılır.
    // Ancak burada basitçe "Bir sonraki planlanan antrenman" için bir Work oluşturacağız.
    
    fun scheduleWorkoutReminder(context: Context, targetDate: LocalDate, dayName: String) {
        val now = LocalDateTime.now()
        // Bildirim saati: O günün sabahı 09:00
        val targetTime = targetDate.atTime(9, 0)
        
        if (targetTime.isBefore(now)) return // Geçmiş zaman
        
        val initialDelay = ChronoUnit.MINUTES.between(now, targetTime)
        
        val inputData = androidx.work.Data.Builder()
            .putString("title", "Bugün Antrenman Günü!")
            .putString("message", "Bugün '$dayName' programını yapmayı unutma!")
            .build()
            
        val reminderWork = OneTimeWorkRequestBuilder<WorkoutReminderWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag("workout_reminder_${targetDate}")
            .build()
            
        WorkManager.getInstance(context).enqueueUniqueWork(
            "workout_reminder_${targetDate}",
            ExistingWorkPolicy.REPLACE,
            reminderWork
        )
    }
}

class WorkoutReminderWorker(
    context: Context, 
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Antrenman Zamanı"
        val message = inputData.getString("message") ?: "Hadi spora!"
        
        NotificationHelper.showNotification(applicationContext, title, message)
        
        return Result.success()
    }
}
