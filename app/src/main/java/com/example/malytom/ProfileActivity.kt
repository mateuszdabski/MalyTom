package com.example.malytom

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    companion object {
        private const val NEWS = "News"
        private const val EVENTS = "Events"
    }

    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // kod zdublowany, mozna przekazac ten token z poprzedniego activity, ale w ostatecznej wersji
        // i tak prawdopodobnie zostanie usunieta ta czesc ponizej, wiec niech jest jak jest poki co
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    textViewToken.text = task.exception?.message
                    return@OnCompleteListener
                }
                token = task.result?.token
                setLayoutData()
            })

        //opening dashboard
        buttonOpenDashboard.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("http://maly-tom.web.app")
            startActivity(i)
        }

        //copying the token
        buttonCopyToken.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip = ClipData.newPlainText("token", textViewToken.text)
            Toast.makeText(this@ProfileActivity, "Copied", Toast.LENGTH_LONG).show()
        }

        buttonNewsSubscribe.setOnClickListener {
            FirebaseMessaging.getInstance().subscribeToTopic(NEWS).addOnSuccessListener {
                Toast.makeText(this, "Subscribe Topic: $NEWS", Toast.LENGTH_SHORT).show()
            }
        }

        buttonNewsUnsubscribe.setOnClickListener {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(NEWS).addOnSuccessListener {
                Toast.makeText(this, "Unsubscribe Topic: $NEWS", Toast.LENGTH_SHORT).show()
            }
        }

        buttonEventsSubscribe.setOnClickListener {
            FirebaseMessaging.getInstance().subscribeToTopic(EVENTS).addOnSuccessListener {
                Toast.makeText(this, "Subscribe Topic: $EVENTS", Toast.LENGTH_SHORT).show()
            }
        }

        buttonEventsUnsubscribe.setOnClickListener {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(EVENTS).addOnSuccessListener {
                Toast.makeText(this, "Unsubscribe Topic: $EVENTS", Toast.LENGTH_SHORT).show()
            }
        }

        buttonOfflineNotification.setOnClickListener {
            //scheduleNotification()
            Toast.makeText(this@ProfileActivity, "This feature is turned off", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun setLayoutData() {
        progressbar.visibility = View.INVISIBLE
        textViewMessage.text = "Your FCM Token is:"
        textViewToken.text = token
    }

    private fun scheduleNotification() {
        val notificationIntent = Intent(this, MyNotificationPublisher::class.java)
        val alarmIntent = PendingIntent.getBroadcast(
            this,
            0,
            notificationIntent,
            0
        )

        val alarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 20 * 1000,
            alarmIntent
        )
    }
}
