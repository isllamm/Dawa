package com.family.dawa

import com.family.dawa.domain.engine.DoseEngine
import com.family.dawa.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DoseEngineTest {

    private val zone = ZoneId.of("Africa/Cairo")
    private val testDate = LocalDate.of(2026, 10, 8) // Thursday (dayOfWeek = 4)

    @Test
    fun testGenerateSlotsForDate_groupsByTimeAndFiltersDisabled() {
        val med1 = Medication(id = 1, name = "بانادول", active = true)
        val med2 = Medication(id = 2, name = "كونكور", active = true)
        val medArchived = Medication(id = 3, name = "قديم", active = false)

        val sched1 = Schedule(id = 101, medicationId = 1, timeOfDayMinutes = 480, daysOfWeekMask = 127, enabled = true)
        val sched2 = Schedule(id = 102, medicationId = 2, timeOfDayMinutes = 480, daysOfWeekMask = 127, enabled = true)
        val sched3 = Schedule(id = 103, medicationId = 1, timeOfDayMinutes = 840, daysOfWeekMask = 127, enabled = true)
        val schedDisabled = Schedule(id = 104, medicationId = 1, timeOfDayMinutes = 1200, daysOfWeekMask = 127, enabled = false)
        val schedArchivedMed = Schedule(id = 105, medicationId = 3, timeOfDayMinutes = 1200, daysOfWeekMask = 127, enabled = true)

        val slots = DoseEngine.generateSlotsForDate(
            date = testDate,
            schedules = listOf(sched1, sched2, sched3, schedDisabled, schedArchivedMed),
            medications = mapOf(1L to med1, 2L to med2, 3L to medArchived),
            photos = emptyMap()
        )

        // Expected 2 slots: 480 (with 2 meds) and 840 (with 1 med)
        assertEquals(2, slots.size)
        assertEquals(480, slots[0].timeMinutes)
        assertEquals(2, slots[0].items.size)
        assertEquals(840, slots[1].timeMinutes)
        assertEquals(1, slots[1].items.size)
    }

    @Test
    fun testDeriveSlotStatus_transitions() {
        val slot = Slot(
            date = testDate,
            timeMinutes = 480, // 08:00 AM
            items = listOf(DoseItem(scheduleId = 1, medicationId = 1, medicationName = "بانادول"))
        )

        // 1. Before slot time -> UPCOMING
        val beforeTime = testDate.atTime(LocalTime.of(7, 30)).atZone(zone)
        val (statusUpcoming, _) = DoseEngine.deriveSlotStatus(slot, emptyList(), beforeTime, graceMinutes = 60)
        assertEquals(SlotStatus.UPCOMING, statusUpcoming)

        // 2. Exactly at slot time -> DUE
        val atTime = testDate.atTime(LocalTime.of(8, 0)).atZone(zone)
        val (statusDue, _) = DoseEngine.deriveSlotStatus(slot, emptyList(), atTime, graceMinutes = 60)
        assertEquals(SlotStatus.DUE, statusDue)

        // 3. Within grace (8:45 AM) -> DUE
        val withinGrace = testDate.atTime(LocalTime.of(8, 45)).atZone(zone)
        val (statusDue2, _) = DoseEngine.deriveSlotStatus(slot, emptyList(), withinGrace, graceMinutes = 60)
        assertEquals(SlotStatus.DUE, statusDue2)

        // 4. After grace (9:01 AM) -> MISSED
        val afterGrace = testDate.atTime(LocalTime.of(9, 1)).atZone(zone)
        val (statusMissed, _) = DoseEngine.deriveSlotStatus(slot, emptyList(), afterGrace, graceMinutes = 60)
        assertEquals(SlotStatus.MISSED, statusMissed)

        // 5. If COMPLETED event exists -> COMPLETED even if after grace
        val completedEvent = DoseEvent(
            id = 1,
            scheduleId = 1,
            medicationId = 1,
            slotDate = testDate,
            slotTimeMinutes = 480,
            status = EventStatus.COMPLETED
        )
        val (statusCompleted, _) = DoseEngine.deriveSlotStatus(slot, listOf(completedEvent), afterGrace, graceMinutes = 60)
        assertEquals(SlotStatus.COMPLETED, statusCompleted)
    }

    @Test
    fun testResolveHomeState_duePriorityOverMissed() {
        val dueSlot = Slot(testDate, 480, emptyList())
        val missedSlot = Slot(testDate, 360, emptyList())

        val todaySlotsWithStatus = listOf(
            SlotWithStatus(missedSlot, SlotStatus.MISSED),
            SlotWithStatus(dueSlot, SlotStatus.DUE)
        )

        val state = DoseEngine.resolveHomeState(
            todaySlotsWithStatus = todaySlotsWithStatus,
            yesterdaySlotsWithStatus = emptyList(),
            tomorrowSlots = emptyList(),
            contact = null,
            acknowledgedSlotKeys = emptySet()
        )

        // DUE must take priority over MISSED
        assertTrue(state is HomeState.Due)
        assertEquals(480, (state as HomeState.Due).slot.timeMinutes)
    }
}
