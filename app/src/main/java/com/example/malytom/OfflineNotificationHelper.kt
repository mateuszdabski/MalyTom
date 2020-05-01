package com.example.malytom
import android.app.AlarmManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.PendingIntent
import android.content.Intent
import android.icu.util.Calendar
import android.os.SystemClock
import android.util.Log

object OfflineNotificationHelper {

    fun updateNotification(context: Context) {

        val notificationIntent = Intent(context, MyNotificationPublisher::class.java)
        val alarmIntent = PendingIntent.getBroadcast(
            context,
            0,
            notificationIntent,
            0
        )
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmManager.set(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            SystemClock.elapsedRealtime() + 20 * 1000,
//            alarmIntent
//        )
        val hAndM = MainActivity.OFF_NOTIFICATION_TIME.split(":")
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hAndM[0].toInt())
            set(Calendar.MINUTE, hAndM[1].toInt())
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmIntent
        )
    }

}