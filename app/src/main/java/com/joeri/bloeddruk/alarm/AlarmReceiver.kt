package com.joeri.bloeddruk.alarm

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.joeri.bloeddruk.BloeddrukApp
import com.joeri.bloeddruk.MainActivity
import com.joeri.bloeddruk.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(AlarmScheduler.EXTRA_ID, -1)
        if (id < 0) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarm = AppDatabase.get(context).alarmDao().getAll().firstOrNull { it.id == id }
                if (alarm != null && alarm.enabled) {
                    AlarmPlayer.play(context, alarm.soundUri)
                    showNotification(context, alarm.id, alarm.isMorning)
                    // Plan meteen het alarm van morgen.
                    AlarmScheduler.schedule(context, alarm)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun showNotification(context: Context, id: Int, morning: Boolean) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val open = PendingIntent.getActivity(
            context, 100 + id,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, BloeddrukApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Tijd om je bloeddruk te meten")
            .setContentText(if (morning) "Ochtendmeting" else "Avondmeting")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(id, notification)
    }
}
