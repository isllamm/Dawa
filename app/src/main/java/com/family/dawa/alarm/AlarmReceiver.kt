package com.family.dawa.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.family.dawa.core.time.DebugTimeProvider
import com.family.dawa.data.db.DoseEventDao
import com.family.dawa.domain.engine.DoseEngine
import com.family.dawa.domain.ledger.DoseLedger
import com.family.dawa.domain.model.SlotStatus
import com.family.dawa.domain.model.SlotWithStatus
import com.family.dawa.domain.repository.IMedicationRepository
import com.family.dawa.domain.repository.ISettingsRepository
import com.family.dawa.domain.scheduler.IAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val timeProvider: DebugTimeProvider by inject()
    private val settingsRepository: ISettingsRepository by inject()
    private val doseLedger: DoseLedger by inject()
    private val medicationRepository: IMedicationRepository by inject()
    private val doseEventDao: DoseEventDao by inject()
    private val notificationHelper: NotificationHelper by inject()
    private val alarmScheduler: IAlarmScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
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
                val now = timeProvider.nowZoned()
                val today = now.toLocalDate()
                val settings = settingsRepository.getSettings()

                // Reconcile past events
                doseLedger.reconcile(now, settings.graceMinutes)

                val schedules = medicationRepository.getAllActiveSchedulesSync()
                val meds = medicationRepository.getAllActiveMedicationsSync()
                val photos = medicationRepository.getAllPhotosSync()
                val todayEvents = doseEventDao.getEventsForDateSync(today).map { it.toDomain() }

                val todaySlots = DoseEngine.generateSlotsForDate(today, schedules, meds, photos)
                val todaySlotsWithStatus = todaySlots.map { slot ->
                    val (status, event) = DoseEngine.deriveSlotStatus(slot, todayEvents, now, settings.graceMinutes)
                    SlotWithStatus(slot, status, event)
                }

                val currentDue = todaySlotsWithStatus.firstOrNull { it.status == SlotStatus.DUE }

                when (intent.action) {
                    AlarmSync.ACTION_DUE_ALARM, AlarmSync.ACTION_REALERT -> {
                        if (currentDue != null) {
                            notificationHelper.showDueNotification(
                                currentDue.slot,
                                currentDue.slot.items
                            )
                        }
                    }
                    AlarmSync.ACTION_MISSED_CHECK -> {
                        notificationHelper.cancelDueNotification()
                        val missedSlot = todaySlotsWithStatus.firstOrNull { it.status == SlotStatus.MISSED }
                        if (missedSlot != null) {
                            notificationHelper.showMissedNotification(missedSlot.slot)
                        }
                    }
                }

                // Resync next alarms
                alarmScheduler.resync()
            } finally {
                wakeLock.release()
                pendingResult.finish()
            }
        }
    }
}
