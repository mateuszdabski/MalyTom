package com.example.malytom

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
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
        FirebaseMessaging.getInstance().subscribeToTopic("temtest")

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
            Toast.makeText(this@ProfileActivity, "Copied", Toast.LENGTH_LONG).show()
        }
    }

    private fun setLayoutData() {
        progressbar.visibility = View.INVISIBLE
        textViewMessage.text = "Your FCM Token is:"
        textViewToken.text = token
    }
}
