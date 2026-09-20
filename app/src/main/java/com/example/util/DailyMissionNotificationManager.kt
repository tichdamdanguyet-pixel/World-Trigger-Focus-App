package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AgentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Manages local notifications and alarms for daily Solo Rank War missions.
 * Reminds users if daily missions have not been finished by their scheduled reminder time.
 */
object DailyMissionNotificationManager {

    const val CHANNEL_ID = "border_daily_missions_channel"
    const val NOTIFICATION_ID = 2001
    const val REMINDER_REQUEST_CODE = 3001
    private const val TAG = "MissionNotification"

    /**
     * Creates the notification channel for Android 8.0+ (API 26+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Solo Rank War - Nhiệm Vụ Hằng Ngày"
            val descriptionText = "Nhắc nhở hoàn thành nhiệm vụ tác chiến và học tập Border Agent"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules the daily alarm for the reminder at the specified hour and minute.
     */
    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyMissionReminderReceiver::class.java).apply {
            action = DailyMissionReminderReceiver.ACTION_CHECK_DAILY_MISSIONS
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            flags
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Daily mission reminder scheduled for: ${calendar.time}")
        } catch (e: SecurityException) {
            // In Android 12+, exact alarm permission might be restricted; fallback to set()
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.w(TAG, "Scheduled fallback non-exact alarm: ${e.message}")
        }
    }

    /**
     * Cancels the scheduled daily alarm
     */
    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyMissionReminderReceiver::class.java).apply {
            action = DailyMissionReminderReceiver.ACTION_CHECK_DAILY_MISSIONS
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            flags
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Daily mission reminder alarm cancelled.")
    }

    /**
     * Shows the mission reminder notification if permission is granted.
     */
    fun showMissionReminderNotification(
        context: Context,
        incompleteCount: Int,
        totalCount: Int,
        firstIncompleteTitle: String? = null
    ) {
        // Check POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Cannot show notification: POST_NOTIFICATIONS permission not granted")
                return
            }
        }

        createNotificationChannel(context)

        // PendingIntent to launch app and navigate to Solo Rank War
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "SOLO_RANK")
            putExtra("OPEN_SOLO_SUBTAB", 1) // Mission tab
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            tapIntent,
            pendingIntentFlags
        )

        val title = "Border Agency: Nhắc nhở Solo Rank War!"
        val contentText = if (incompleteCount == 1 && !firstIncompleteTitle.isNullOrBlank()) {
            "Bạn còn 1 nhiệm vụ chưa hoàn thành hôm nay: \"$firstIncompleteTitle\""
        } else {
            "Bạn còn $incompleteCount/$totalCount nhiệm vụ tác chiến hôm nay chưa hoàn tất!"
        }

        val iconRes = try {
            R.drawable.ic_radar
        } catch (_: Throwable) {
            android.R.drawable.ic_popup_reminder
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$contentText\nHãy hoàn thành để nhận điểm thưởng Trion PTS và thăng hạng Border Agent trước khi qua ngày mới!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .setColor(0xFF00E5FF.toInt())

        val notification = builder.build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "Dispatched notification for $incompleteCount incomplete missions.")
    }
}

/**
 * BroadcastReceiver triggered by AlarmManager to check pending daily missions.
 */
class DailyMissionReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val repository = AgentRepository(context.applicationContext)
        if (!repository.isDailyMissionReminderEnabled()) {
            return
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Use CoroutineScope to inspect Room database on IO thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val missions = repository.getDailyMissionsSync(today)
                val incomplete = missions.filter { !it.isCompleted }

                if (incomplete.isNotEmpty()) {
                    DailyMissionNotificationManager.showMissionReminderNotification(
                        context = context,
                        incompleteCount = incomplete.size,
                        totalCount = missions.size,
                        firstIncompleteTitle = incomplete.firstOrNull()?.title
                    )
                }

                // Reschedule for next day at the saved reminder time
                val (hour, minute) = repository.getDailyMissionReminderTime()
                DailyMissionNotificationManager.scheduleDailyReminder(context, hour, minute)
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Failed to check daily missions: ${e.message}", e)
            }
        }
    }

    companion object {
        const val ACTION_CHECK_DAILY_MISSIONS = "com.example.ACTION_CHECK_DAILY_MISSIONS"
    }
}
