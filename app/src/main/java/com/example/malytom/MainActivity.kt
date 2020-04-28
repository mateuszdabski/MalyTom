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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
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
    lateinit var fbAuth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var token: String? = null

    //Defined the required values
    companion object {
        const val CHANNEL_ID = "test_notifications"
        private const val CHANNEL_NAME = "Test Notifications"
        private const val CHANNEL_DESC = "Testing push notifications"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        fbAuth = FirebaseAuth.getInstance()
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
        progressbar.visibility = View.INVISIBLE
        buttonSignUp.setOnClickListener {
            createUser()
        }
    }

    private fun createOrRetrieveId(context: Context): String? {
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

    private fun initApp(context: Context) {
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
                })
        } else {
            // get FCM token and save bt name
            token = sharedPrefs.getString(APP_TOKEN, null)
            db.collection("Users").document(uniqueID!!)
                .update("bluetooth", btName)
                .addOnSuccessListener { Log.d(TAG_FIREBASE,"DocumentSnapshot added with ID: ") }
                .addOnFailureListener { e -> Log.w(TAG_FIREBASE, "Error adding document", e) }
        }
    }

    private fun createUser() {
        val email: String = editTextEmail.text.toString().trim()
        val password: String = editTextPassword.text.toString().trim()

        if (email.isEmpty()){
            editTextEmail.error = "Email required"
            editTextEmail.requestFocus()
            return
        }
        if (password.isEmpty()){
            editTextPassword.error = "Password required"
            editTextPassword.requestFocus()
            return
        }
        if (password.length<6){
            editTextPassword.error = "Password should be at least 6 characters long"
            editTextPassword.requestFocus()
            return
        }

        progressbar.visibility = View.VISIBLE
        fbAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "createUserWithEmail:success")
                    createOrRetrieveId(applicationContext)
                    initApp(applicationContext)
                    startProfileActivity()
                } else {
                    Log.w("Firebase", "createUserWithEmail:failure", task.exception);
                    if (task.exception is FirebaseAuthUserCollisionException){
                        userLogin(email, password)
                    } else {
                        progressbar.visibility = View.INVISIBLE
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun userLogin(email: String, password: String){
        fbAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    createOrRetrieveId(applicationContext)
                    initApp(applicationContext)
                    startProfileActivity()
                } else {
                    progressbar.visibility = View.INVISIBLE
                    Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startProfileActivity(){
        val i = Intent(this, ProfileActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(i)
    }
}
