package com.joeri.bloeddruk

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager

class BloeddrukApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Bloeddruk-herinneringen",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Herinnering om je bloeddruk te meten"
            enableVibration(true)
            // Geen kanaalgeluid, zodat AlarmPlayer als enige het gekozen geluid afspeelt zonder dubbele tonen.
            setSound(null, null)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "bloeddruk_alarm"
    }
}
