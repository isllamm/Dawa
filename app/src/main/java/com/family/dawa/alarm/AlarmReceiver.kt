package com.family.dawa.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.family.dawa.DawaApp
import com.family.dawa.domain.engine.DoseEngine
import com.family.dawa.domain.model.SlotStatus
import com.family.dawa.domain.model.SlotWithStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? DawaApp ?: return
        val container = app.container

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "dawa:alarm_receiver"
        ).apply {
            setReferenceCounted(false)
            acquire(10000) // max 10 seconds
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = container.timeProvider.nowZoned()
                val today = now.toLocalDate()
                val settings = container.settingsRepository.getSettings()

                // Reconcile past events
                container.doseLedger.reconcile(now, settings.graceMinutes)

                val schedules = container.medicationRepository.getAllActiveSchedulesSync()
                val meds = container.medicationRepository.getAllActiveMedicationsSync()
                val photos = container.medicationRepository.getAllPhotosSync()
                val todayEvents = container.doseEventDao.getEventsForDateSync(today).map { it.toDomain() }

                val todaySlots = DoseEngine.generateSlotsForDate(today, schedules, meds, photos)
                val todaySlotsWithStatus = todaySlots.map { slot ->
                    val (status, event) = DoseEngine.deriveSlotStatus(slot, todayEvents, now, settings.graceMinutes)
                    SlotWithStatus(slot, status, event)
                }

                val currentDue = todaySlotsWithStatus.firstOrNull { it.status == SlotStatus.DUE }

                when (intent.action) {
                    AlarmSync.ACTION_DUE_ALARM, AlarmSync.ACTION_REALERT -> {
                        if (currentDue != null) {
                            container.notificationHelper.showDueNotification(
                                currentDue.slot,
                                currentDue.slot.items
                            )
                        }
                    }
                    AlarmSync.ACTION_MISSED_CHECK -> {
                        container.notificationHelper.cancelDueNotification()
                        val missedSlot = todaySlotsWithStatus.firstOrNull { it.status == SlotStatus.MISSED }
                        if (missedSlot != null) {
                            container.notificationHelper.showMissedNotification(missedSlot.slot)
                        }
                    }
                }

                // Resync next alarms
                container.alarmSync.resync()
            } finally {
                wakeLock.release()
                pendingResult.finish()
            }
        }
    }
}
