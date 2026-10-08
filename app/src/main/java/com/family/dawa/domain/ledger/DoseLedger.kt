package com.family.dawa.domain.ledger

import com.family.dawa.data.db.DoseEventDao
import com.family.dawa.data.db.DoseEventEntity
import com.family.dawa.data.repo.MedicationRepository
import com.family.dawa.domain.engine.DoseEngine
import com.family.dawa.domain.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

class DoseLedger(
    private val doseEventDao: DoseEventDao,
    private val medicationRepository: MedicationRepository
) {

    /**
     * Confirms that all medications in this slot were administered.
     * Inserts COMPLETED events atomically.
     */
    suspend fun confirmSlot(
        slot: Slot,
        now: ZonedDateTime,
        recordedBy: RecordedBy = RecordedBy.CAREGIVER
    ) {
        val nowMillis = now.toInstant().toEpochMilli()
        val entities = slot.items.map { item ->
            DoseEventEntity(
                scheduleId = item.scheduleId,
                medicationId = item.medicationId,
                slotDate = slot.date,
                slotTimeMinutes = slot.timeMinutes,
                status = EventStatus.COMPLETED,
                recordedAt = nowMillis,
                recordedBy = recordedBy,
                acknowledgedAt = nowMillis,
                medNameSnapshot = item.medicationName,
                quantityHalvesSnapshot = item.quantityHalves
            )
        }
        // Replace in case any partial record existed
        for (entity in entities) {
            doseEventDao.insertOrReplace(entity)
        }
    }

    /**
     * Caregiver taps "حاضر" on a missed slot reminder.
     * Marks all events for this slot as acknowledged so the screen returns to Idle.
     */
    suspend fun acknowledgeMissed(slot: Slot, now: ZonedDateTime) {
        val nowMillis = now.toInstant().toEpochMilli()
        // Ensure missed events exist in DB
        val existingEvents = doseEventDao.getEventsForDateSync(slot.date)
        val hasEvent = existingEvents.any { it.slotTimeMinutes == slot.timeMinutes }

        if (!hasEvent) {
            val entities = slot.items.map { item ->
                DoseEventEntity(
                    scheduleId = item.scheduleId,
                    medicationId = item.medicationId,
                    slotDate = slot.date,
                    slotTimeMinutes = slot.timeMinutes,
                    status = EventStatus.MISSED,
                    recordedAt = nowMillis,
                    recordedBy = RecordedBy.SYSTEM,
                    acknowledgedAt = nowMillis,
                    medNameSnapshot = item.medicationName,
                    quantityHalvesSnapshot = item.quantityHalves
                )
            }
            doseEventDao.insertAllOrIgnore(entities)
        } else {
            doseEventDao.acknowledgeSlot(slot.date, slot.timeMinutes, nowMillis)
        }
    }

    /**
     * Reconciles past schedules: any schedule slot that has elapsed past (dueTime + grace)
     * without a confirmation is permanently recorded as MISSED.
     */
    suspend fun reconcile(now: ZonedDateTime, graceMinutes: Int) {
        val schedules = medicationRepository.getAllActiveSchedulesSync()
        val meds = medicationRepository.getAllActiveMedicationsSync()
        val photos = medicationRepository.getAllPhotosSync()

        val today = now.toLocalDate()
        val datesToScan = listOf(today.minusDays(1), today)

        for (date in datesToScan) {
            val slots = DoseEngine.generateSlotsForDate(date, schedules, meds, photos)
            val events = doseEventDao.getEventsForDateSync(date)

            for (slot in slots) {
                val hasEvent = events.any { it.slotTimeMinutes == slot.timeMinutes }
                if (hasEvent) continue

                val slotTime = LocalTime.of(slot.timeMinutes / 60, slot.timeMinutes % 60)
                val graceEnd = slot.date.atTime(slotTime).atZone(now.zone).plusMinutes(graceMinutes.toLong())

                if (now.isAfter(graceEnd)) {
                    val missedEntities = slot.items.map { item ->
                        DoseEventEntity(
                            scheduleId = item.scheduleId,
                            medicationId = item.medicationId,
                            slotDate = slot.date,
                            slotTimeMinutes = slot.timeMinutes,
                            status = EventStatus.MISSED,
                            recordedAt = graceEnd.toInstant().toEpochMilli(),
                            recordedBy = RecordedBy.SYSTEM,
                            acknowledgedAt = null,
                            medNameSnapshot = item.medicationName,
                            quantityHalvesSnapshot = item.quantityHalves
                        )
                    }
                    doseEventDao.insertAllOrIgnore(missedEntities)
                }
            }
        }
    }

    suspend fun resetTodayEvents(today: LocalDate) {
        doseEventDao.deleteEventsForDate(today)
    }

    suspend fun updateEventStatus(eventId: Long, newStatus: EventStatus, now: ZonedDateTime) {
        val events = doseEventDao.getEventsBetweenSync(LocalDate.now().minusDays(30), LocalDate.now())
        val target = events.firstOrNull { it.id == eventId } ?: return
        doseEventDao.update(
            target.copy(
                status = newStatus,
                recordedAt = now.toInstant().toEpochMilli(),
                recordedBy = RecordedBy.ADMIN
            )
        )
    }
}
