package com.rasayili.sms.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.rasayili.sms.MainActivity

class ComposeSmsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data = this@ComposeSmsActivity.intent?.data
            putExtra(Intent.EXTRA_TEXT, this@ComposeSmsActivity.intent?.getStringExtra(Intent.EXTRA_TEXT))
        }
        startActivity(intent)
        finish()
    }
}