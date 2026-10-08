package com.family.dawa.domain.engine

import com.family.dawa.domain.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

object DoseEngine {

    /**
     * Generates all slots for a given date from the list of schedules, medications, and photos.
     */
    fun generateSlotsForDate(
        date: LocalDate,
        schedules: List<Schedule>,
        medications: Map<Long, Medication>,
        photos: Map<Long, List<MedicationPhoto>>
    ): List<Slot> {
        val dayOfWeekValue = date.dayOfWeek.value // 1 (Mon) .. 7 (Sun)
        val doseItemsByTime = mutableMapOf<Int, MutableList<DoseItem>>()

        for (schedule in schedules) {
            if (!schedule.enabled) continue
            val med = medications[schedule.medicationId] ?: continue
            if (!med.active) continue

            // Check date range
            if (schedule.startDate != null && date.isBefore(schedule.startDate)) continue
            if (schedule.endDate != null && date.isAfter(schedule.endDate)) continue

            // Check day of week
            if (!schedule.isDueOnDay(dayOfWeekValue)) continue

            // Find primary photo or first photo
            val medPhotos = photos[schedule.medicationId] ?: emptyList()
            val primaryPhoto = medPhotos.firstOrNull { it.isPrimary } ?: medPhotos.firstOrNull()

            val item = DoseItem(
                scheduleId = schedule.id,
                medicationId = med.id,
                medicationName = med.name,
                strength = med.strength,
                quantityHalves = schedule.quantityHalves,
                primaryPhotoPath = primaryPhoto?.path,
                audioPath = med.audioPath,
                mealRelation = schedule.mealRelation,
                colorTag = med.colorTag,
                shapeTag = med.shapeTag
            )

            doseItemsByTime.getOrPut(schedule.timeOfDayMinutes) { mutableListOf() }.add(item)
        }

        return doseItemsByTime.entries
            .sortedBy { it.key }
            .map { (timeMinutes, items) ->
                Slot(
                    date = date,
                    timeMinutes = timeMinutes,
                    items = items
                )
            }
    }

    /**
     * Derives the status of a specific slot based on persisted events and current time.
     */
    fun deriveSlotStatus(
        slot: Slot,
        events: List<DoseEvent>,
        now: ZonedDateTime,
        graceMinutes: Int,
        earlyWindowMinutes: Int = 0
    ): Pair<SlotStatus, DoseEvent?> {
        // Find if any event exists for this slot
        val matchingEvent = events.firstOrNull { it.slotDate == slot.date && it.slotTimeMinutes == slot.timeMinutes }
        if (matchingEvent != null) {
            return when (matchingEvent.status) {
                EventStatus.COMPLETED -> SlotStatus.COMPLETED to matchingEvent
                EventStatus.SKIPPED -> SlotStatus.SKIPPED to matchingEvent
                EventStatus.MISSED -> SlotStatus.MISSED to matchingEvent
            }
        }

        val slotTime = LocalTime.of(slot.timeMinutes / 60, slot.timeMinutes % 60)
        val slotDateTime = slot.date.atTime(slotTime).atZone(now.zone)

        val dueStart = slotDateTime.minusMinutes(earlyWindowMinutes.toLong())
        val graceEnd = slotDateTime.plusMinutes(graceMinutes.toLong())

        val status = when {
            now.isBefore(dueStart) -> SlotStatus.UPCOMING
            now.isBefore(graceEnd) -> SlotStatus.DUE
            else -> SlotStatus.MISSED
        }

        return status to null
    }

    /**
     * Resolves the single Caregiver HomeState.
     * Priority: DUE > MISSED (unacknowledged) > IDLE.
     */
    fun resolveHomeState(
        todaySlotsWithStatus: List<SlotWithStatus>,
        yesterdaySlotsWithStatus: List<SlotWithStatus>,
        tomorrowSlots: List<Slot>,
        contact: Contact?,
        acknowledgedSlotKeys: Set<String>
    ): HomeState {
        // 1. Check if any slot is currently DUE (check today first, then yesterday midnight cross)
        val dueSlot = todaySlotsWithStatus.firstOrNull { it.status == SlotStatus.DUE }
            ?: yesterdaySlotsWithStatus.firstOrNull { it.status == SlotStatus.DUE }

        if (dueSlot != null) {
            return HomeState.Due(
                slot = dueSlot.slot,
                items = dueSlot.slot.items,
                todaySlots = todaySlotsWithStatus
            )
        }

        // 2. Check for an unacknowledged MISSED slot today
        val unacknowledgedMissed = todaySlotsWithStatus.firstOrNull {
            it.status == SlotStatus.MISSED && !acknowledgedSlotKeys.contains(it.slot.key)
        }

        if (unacknowledgedMissed != null) {
            return HomeState.Missed(
                slot = unacknowledgedMissed.slot,
                contact = contact,
                todaySlots = todaySlotsWithStatus
            )
        }

        // 3. Fallback to IDLE, showing the next upcoming slot
        val nextSlotToday = todaySlotsWithStatus.firstOrNull { it.status == SlotStatus.UPCOMING }?.slot
        val nextSlot = nextSlotToday ?: tomorrowSlots.firstOrNull()

        return HomeState.Idle(
            nextSlot = nextSlot,
            todaySlots = todaySlotsWithStatus
        )
    }

    /**
     * Calculates the exact next triggers for AlarmManager.
     */
    fun calculateAlarmTriggers(
        todaySlotsWithStatus: List<SlotWithStatus>,
        tomorrowSlots: List<Slot>,
        now: ZonedDateTime,
        graceMinutes: Int,
        realertMinutes: Int
    ): AlarmTriggers {
        val zone = now.zone

        // 1. Next DUE alarm (earliest UPCOMING slot today or tomorrow)
        val nextUpcoming = todaySlotsWithStatus.firstOrNull { it.status == SlotStatus.UPCOMING }?.slot
            ?: tomorrowSlots.firstOrNull()

        val nextDueEpochMillis = nextUpcoming?.let { slot ->
            val slotTime = LocalTime.of(slot.timeMinutes / 60, slot.timeMinutes % 60)
            slot.date.atTime(slotTime).atZone(zone).toInstant().toEpochMilli()
        }

        // 2. Check currently DUE slot for realert and missed check
        val currentDue = todaySlotsWithStatus.firstOrNull { it.status == SlotStatus.DUE }
        var nextRealertEpochMillis: Long? = null
        var missedCheckEpochMillis: Long? = null

        if (currentDue != null) {
            val slot = currentDue.slot
            val slotTime = LocalTime.of(slot.timeMinutes / 60, slot.timeMinutes % 60)
            val dueAt = slot.date.atTime(slotTime).atZone(zone)
            val graceEnd = dueAt.plusMinutes(graceMinutes.toLong())

            missedCheckEpochMillis = graceEnd.toInstant().toEpochMilli()

            val candidateRealert = now.plusMinutes(realertMinutes.toLong())
            if (candidateRealert.isBefore(graceEnd)) {
                nextRealertEpochMillis = candidateRealert.toInstant().toEpochMilli()
            }
        }

        return AlarmTriggers(
            dueEpochMillis = nextDueEpochMillis,
            realertEpochMillis = nextRealertEpochMillis,
            missedCheckEpochMillis = missedCheckEpochMillis
        )
    }
}

data class AlarmTriggers(
    val dueEpochMillis: Long?,
    val realertEpochMillis: Long?,
    val missedCheckEpochMillis: Long?
)
