package com.rasayili.sms.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "android.intent.action.RESPOND_VIA_MESSAGE") {
            val uri = intent.data
            val recipient = uri?.schemeSpecificPart ?: ""
            val message = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            if (recipient.isNotEmpty() && message.isNotEmpty()) {
                @Suppress("DEPRECATION")
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(recipient, null, message, null, null)
            }
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
