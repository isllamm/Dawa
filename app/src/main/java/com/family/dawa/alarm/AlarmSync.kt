package com.family.dawa.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.family.dawa.core.time.TimeProvider
import com.family.dawa.data.db.DoseEventDao
import com.family.dawa.data.repo.MedicationRepository
import com.family.dawa.data.settings.SettingsRepository
import com.family.dawa.domain.engine.DoseEngine
import com.family.dawa.domain.model.SlotWithStatus
import com.family.dawa.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmSync(
    private val context: Context,
    private val medicationRepository: MedicationRepository,
    private val doseEventDao: DoseEventDao,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val ACTION_DUE_ALARM = "com.family.dawa.ACTION_DUE_ALARM"
        const val ACTION_REALERT = "com.family.dawa.ACTION_REALERT"
        const val ACTION_MISSED_CHECK = "com.family.dawa.ACTION_MISSED_CHECK"

        const val REQ_DUE = 1001
        const val REQ_REALERT = 1002
        const val REQ_MISSED_CHECK = 1003
    }

    suspend fun resync() = withContext(Dispatchers.IO) {
        val now = timeProvider.nowZoned()
        val today = now.toLocalDate()
        val tomorrow = today.plusDays(1)

        val settings = settingsRepository.getSettings()
        val schedules = medicationRepository.getAllActiveSchedulesSync()
        val meds = medicationRepository.getAllActiveMedicationsSync()
        val photos = medicationRepository.getAllPhotosSync()

        val todayEvents = doseEventDao.getEventsForDateSync(today).map { it.toDomain() }

        val todaySlots = DoseEngine.generateSlotsForDate(today, schedules, meds, photos)
        val tomorrowSlots = DoseEngine.generateSlotsForDate(tomorrow, schedules, meds, photos)

        val todaySlotsWithStatus = todaySlots.map { slot ->
            val (status, event) = DoseEngine.deriveSlotStatus(slot, todayEvents, now, settings.graceMinutes)
            SlotWithStatus(slot, status, event)
        }

        val triggers = DoseEngine.calculateAlarmTriggers(
            todaySlotsWithStatus = todaySlotsWithStatus,
            tomorrowSlots = tomorrowSlots,
            now = now,
            graceMinutes = settings.graceMinutes,
            realertMinutes = settings.realertMinutes
        )

        // 1. Schedule Next DUE alarm
        if (triggers.dueEpochMillis != null && canScheduleExact()) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_DUE_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQ_DUE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val showIntent = Intent(context, MainActivity::class.java)
            val showPendingIntent = PendingIntent.getActivity(
                context,
                0,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggers.dueEpochMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } else {
            cancelAlarm(REQ_DUE, ACTION_DUE_ALARM)
        }

        // 2. Schedule REALERT alarm
        if (triggers.realertEpochMillis != null && canScheduleExact()) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_REALERT
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQ_REALERT,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggers.realertEpochMillis,
                pendingIntent
            )
        } else {
            cancelAlarm(REQ_REALERT, ACTION_REALERT)
        }

        // 3. Schedule MISSED_CHECK alarm
        if (triggers.missedCheckEpochMillis != null && canScheduleExact()) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_MISSED_CHECK
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQ_MISSED_CHECK,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggers.missedCheckEpochMillis,
                pendingIntent
            )
        } else {
            cancelAlarm(REQ_MISSED_CHECK, ACTION_MISSED_CHECK)
        }
    }

    private fun cancelAlarm(requestCode: Int, action: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun canScheduleExact(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}
