package com.example.malytom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var uniqueID: String? = null
    private val PREF_UNIQUE_ID = "PREF_UNIQUE_ID"
    private val APP_SETTINGS = "APP_SETTINGS"
    private val APP_INIT = "APP_INIT"
    private val APP_TOKEN = "APP_TOKEN"
    private val TAG_FIREBASE = "Firebase"
    lateinit var bAdapter: BluetoothAdapter
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var token: String? = null

    //Defined the required values
    companion object {
        const val CHANNEL_ID = "simplified_coding"
        private const val CHANNEL_NAME = "Simplified Coding"
        private const val CHANNEL_DESC = "Android Push Notification Tutorial"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createOrRetrieveId(applicationContext)
        initApp(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_DESC
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

        }

        //opening dashboard
        buttonOpenDashboard.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("http://maly-tom.web.app")
            startActivity(i)
        }

        //copying the token
        buttonCopyToken.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("token", textViewToken.text)
            clipboard.primaryClip = clip
            Toast.makeText(this@MainActivity, "Copied", Toast.LENGTH_LONG).show()
        }
    }

    @Synchronized
    fun createOrRetrieveId(context: Context): String? {
        if (uniqueID == null) {
            val sharedPrefs = context.getSharedPreferences(
                PREF_UNIQUE_ID, Context.MODE_PRIVATE
            )
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null)
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString()
                val editor = sharedPrefs.edit()
                editor.putString(PREF_UNIQUE_ID, uniqueID)
                editor.commit()
            }
        }
        return uniqueID
    }

    @Synchronized
    fun initApp(context: Context) {
        val pm: PackageManager = applicationContext.packageManager
        val hasBluetooth = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        var btName = ""
        if (hasBluetooth) {
            bAdapter = BluetoothAdapter.getDefaultAdapter()
            btName = bAdapter.name
        }
        val sharedPrefs = context.getSharedPreferences(
            APP_SETTINGS, Context.MODE_PRIVATE
        )
        val init = sharedPrefs.getBoolean(APP_INIT, false)
        if (!init) {
            // get FCM token
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        textViewToken.text = task.exception?.message
                        return@OnCompleteListener
                    }
                    token = task.result?.token

                    // save data in db
                    val user: MutableMap<String, Any> = HashMap()
                    user["token"] = token!!
                    user["bluetooth"] = btName
                    db.collection("Users").document(uniqueID!!)
                        .set(user)
                        .addOnSuccessListener { Log.d(TAG_FIREBASE,"DocumentSnapshot added with ID: ") }
                        .addOnFailureListener { e -> Log.w(TAG_FIREBASE, "Error adding document", e) }

                    // save data locally
                    val editor = sharedPrefs.edit()
                    editor.putBoolean(APP_INIT, true)
                    editor.putString(APP_TOKEN, token)
                    editor.commit()
                    setLayoutData()
                })
        } else {
            // get FCM token and save bt name
            token = sharedPrefs.getString(APP_TOKEN, null)
            db.collection("Users").document(uniqueID!!)
                .update("bluetooth", btName)
                .addOnSuccessListener { Log.d(TAG_FIREBASE,"DocumentSnapshot added with ID: ") }
                .addOnFailureListener { e -> Log.w(TAG_FIREBASE, "Error adding document", e) }
            setLayoutData()
        }
    }

    private fun setLayoutData() {
        progressbar.visibility = View.INVISIBLE
        textViewMessage.text = "Your FCM Token is:"
        textViewToken.text = token
    }
}
