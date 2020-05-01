package com.example.malytom

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            remoteMessage.data.let {

                MainActivity.OFF_NOTIFICATION_TITLE = it["title"]!!
                MainActivity.OFF_NOTIFICATION_MESSAGE = it["message"]!!
                MainActivity.OFF_NOTIFICATION_TIME = it["time"]!!
                OfflineNotificationHelper.updateNotification(applicationContext)
            }
        }

        remoteMessage.notification?.let {
            NotificationHelper.displayNotification(applicationContext, it.title!!, it.body!!)
        }
    }
}