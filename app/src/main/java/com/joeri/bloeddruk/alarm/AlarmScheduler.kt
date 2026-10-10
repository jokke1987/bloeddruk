package com.joeri.bloeddruk.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.joeri.bloeddruk.data.AlarmSetting
import com.joeri.bloeddruk.data.AppDatabase
import java.util.Calendar

object AlarmScheduler {
    const val EXTRA_ID = "alarm_id"

    private fun pendingIntent(context: Context, id: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            id,
            Intent(context, AlarmReceiver::class.java).putExtra(EXTRA_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    /** Plant het alarm voor de eerstvolgende keer dat het tijdstip voorkomt (of annuleert het). */
    fun schedule(context: Context, alarm: AlarmSetting) {
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = pendingIntent(context, alarm.id)
        if (!alarm.enabled) {
            am.cancel(pi)
            return
        }
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }

    suspend fun rescheduleAll(context: Context) {
        AppDatabase.get(context).alarmDao().getAll().forEach { schedule(context, it) }
    }
}
