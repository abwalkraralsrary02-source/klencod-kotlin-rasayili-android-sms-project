package com.rasayili.sms

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class RasayiliApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "rasayili_sms_channel",
                "الرسائل الواردة (SMS)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات الرسائل القصيرة الواردة لتطبيق رسائلي"
                enableVibration(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}