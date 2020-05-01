package com.example.malytom

import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d("XYZ", "From: ${remoteMessage.from}")
        remoteMessage.data?.let {
            Log.d("XYZ", "Message data payload: " + remoteMessage.data)
            val title = it["title"]
            val message = it["message"]
            val time = it["time"]


            MainActivity.OFF_NOTIFICATION_TITLE = title!!
            MainActivity.OFF_NOTIFICATION_MESSAGE = message!!
            MainActivity.OFF_NOTIFICATION_TIME = time!!
            OfflineNotificationHelper.updateNotification(applicationContext)
        }

        remoteMessage.notification?.let {
            val title = it.title
            val body = it.body

            NotificationHelper.displayNotification(applicationContext, title!!, body!!)
        }
    }
}