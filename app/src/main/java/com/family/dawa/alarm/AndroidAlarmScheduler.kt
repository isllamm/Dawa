package com.family.dawa.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.family.dawa.domain.scheduler.IAlarmScheduler
import com.family.dawa.ui.MainActivity

class AndroidAlarmScheduler(
    private val context: Context,
    private val alarmSync: AlarmSync
) : IAlarmScheduler {

    override suspend fun resync() {
        alarmSync.resync()
    }

    override fun scheduleTestAlarmInTenSeconds() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + 10_000L

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmSync.ACTION_DUE_ALARM
        }
        val pi = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPi = PendingIntent.getActivity(
            context,
            0,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAt, showPi),
            pi
        )
    }
}
