package com.family.dawa

import com.family.dawa.domain.engine.AlarmTriggers
import com.family.dawa.domain.engine.DoseEngine
import com.family.dawa.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    // region generateSlotsForDate

    private val med = Medication(
        id = 1,
        name = "بانادول",
        strength = "500mg",
        audioPath = "/audio/1.m4a",
        colorTag = 0xFF2B78E4.toInt(),
        shapeTag = "OVAL"
    )

    private fun slotsFor(
        schedules: List<Schedule>,
        photos: Map<Long, List<MedicationPhoto>> = emptyMap(),
        medications: Map<Long, Medication> = mapOf(1L to med),
        date: LocalDate = testDate
    ) = DoseEngine.generateSlotsForDate(date, schedules, medications, photos)

    @Test
    fun testGenerateSlotsForDate_copiesMedicationAndScheduleFieldsIntoItem() {
        val schedule = Schedule(
            id = 7,
            medicationId = 1,
            timeOfDayMinutes = 480,
            mealRelation = MealRelation.AFTER_BREAKFAST,
            quantityHalves = 1
        )

        val item = slotsFor(listOf(schedule)).single().items.single()

        assertEquals(
            DoseItem(
                scheduleId = 7,
                medicationId = 1,
                medicationName = "بانادول",
                strength = "500mg",
                quantityHalves = 1,
                primaryPhotoPath = null,
                audioPath = "/audio/1.m4a",
                mealRelation = MealRelation.AFTER_BREAKFAST,
                colorTag = 0xFF2B78E4.toInt(),
                shapeTag = "OVAL"
            ),
            item
        )
    }

    @Test
    fun testGenerateSlotsForDate_sortsSlotsByTime() {
        val schedules = listOf(
            Schedule(id = 1, medicationId = 1, timeOfDayMinutes = 1200),
            Schedule(id = 2, medicationId = 1, timeOfDayMinutes = 480),
            Schedule(id = 3, medicationId = 1, timeOfDayMinutes = 840)
        )

        val times = slotsFor(schedules).map { it.timeMinutes }

        assertEquals(listOf(480, 840, 1200), times)
    }

    @Test
    fun testGenerateSlotsForDate_skipsDaysNotInMask() {
        // testDate is Thursday = bit 3. Mask without Thursday:
        val notThursday = 127 and (1 shl 3).inv()
        val schedule = Schedule(id = 1, medicationId = 1, timeOfDayMinutes = 480, daysOfWeekMask = notThursday)

        assertTrue(slotsFor(listOf(schedule)).isEmpty())
        assertEquals(1, slotsFor(listOf(schedule), date = testDate.plusDays(1)).size)
    }

    @Test
    fun testGenerateSlotsForDate_respectsStartAndEndDates() {
        val startsTomorrow = Schedule(id = 1, medicationId = 1, timeOfDayMinutes = 480, startDate = testDate.plusDays(1))
        val endedYesterday = Schedule(id = 2, medicationId = 1, timeOfDayMinutes = 600, endDate = testDate.minusDays(1))
        val startsToday = Schedule(id = 3, medicationId = 1, timeOfDayMinutes = 720, startDate = testDate)
        val endsToday = Schedule(id = 4, medicationId = 1, timeOfDayMinutes = 840, endDate = testDate)

        val times = slotsFor(listOf(startsTomorrow, endedYesterday, startsToday, endsToday)).map { it.timeMinutes }

        assertEquals(listOf(720, 840), times)
    }

    @Test
    fun testGenerateSlotsForDate_skipsScheduleWithUnknownMedication() {
        val schedule = Schedule(id = 1, medicationId = 99, timeOfDayMinutes = 480)

        assertTrue(slotsFor(listOf(schedule)).isEmpty())
    }

    @Test
    fun testGenerateSlotsForDate_prefersPrimaryPhotoThenFirstPhoto() {
        val schedule = Schedule(id = 1, medicationId = 1, timeOfDayMinutes = 480)
        val first = MedicationPhoto(id = 1, medicationId = 1, path = "/first.jpg")
        val primary = MedicationPhoto(id = 2, medicationId = 1, path = "/primary.jpg", isPrimary = true)

        val withPrimary = slotsFor(listOf(schedule), photos = mapOf(1L to listOf(first, primary)))
        val withoutPrimary = slotsFor(listOf(schedule), photos = mapOf(1L to listOf(first)))

        assertEquals("/primary.jpg", withPrimary.single().items.single().primaryPhotoPath)
        assertEquals("/first.jpg", withoutPrimary.single().items.single().primaryPhotoPath)
    }

    // endregion

    // region deriveSlotStatus

    private val slot0800 = Slot(testDate, 480, listOf(DoseItem(scheduleId = 1, medicationId = 1, medicationName = "بانادول")))

    private fun event(status: EventStatus, timeMinutes: Int = 480, date: LocalDate = testDate) = DoseEvent(
        id = 1,
        scheduleId = 1,
        medicationId = 1,
        slotDate = date,
        slotTimeMinutes = timeMinutes,
        status = status
    )

    @Test
    fun testDeriveSlotStatus_mapsEachEventStatusAndReturnsEvent() {
        val afterGrace = testDate.atTime(LocalTime.of(10, 0)).atZone(zone)

        for ((eventStatus, expected) in listOf(
            EventStatus.COMPLETED to SlotStatus.COMPLETED,
            EventStatus.SKIPPED to SlotStatus.SKIPPED,
            EventStatus.MISSED to SlotStatus.MISSED
        )) {
            val e = event(eventStatus)
            val (status, returnedEvent) = DoseEngine.deriveSlotStatus(slot0800, listOf(e), afterGrace, graceMinutes = 60)
            assertEquals(expected, status)
            assertEquals(e, returnedEvent)
        }
    }

    @Test
    fun testDeriveSlotStatus_ignoresEventsForOtherSlots() {
        val beforeTime = testDate.atTime(LocalTime.of(7, 0)).atZone(zone)
        val otherTime = event(EventStatus.COMPLETED, timeMinutes = 840)
        val otherDate = event(EventStatus.COMPLETED, date = testDate.minusDays(1))

        val (status, returnedEvent) = DoseEngine.deriveSlotStatus(
            slot0800, listOf(otherTime, otherDate), beforeTime, graceMinutes = 60
        )

        assertEquals(SlotStatus.UPCOMING, status)
        assertNull(returnedEvent)
    }

    @Test
    fun testDeriveSlotStatus_earlyWindowMakesSlotDueBeforeItsTime() {
        val tenMinutesEarly = testDate.atTime(LocalTime.of(7, 50)).atZone(zone)

        val (withWindow, _) = DoseEngine.deriveSlotStatus(slot0800, emptyList(), tenMinutesEarly, 60, earlyWindowMinutes = 15)
        val (withoutWindow, _) = DoseEngine.deriveSlotStatus(slot0800, emptyList(), tenMinutesEarly, 60)

        assertEquals(SlotStatus.DUE, withWindow)
        assertEquals(SlotStatus.UPCOMING, withoutWindow)
    }

    @Test
    fun testDeriveSlotStatus_exactlyAtGraceEndIsMissed() {
        val graceEnd = testDate.atTime(LocalTime.of(9, 0)).atZone(zone)

        val (status, _) = DoseEngine.deriveSlotStatus(slot0800, emptyList(), graceEnd, graceMinutes = 60)

        assertEquals(SlotStatus.MISSED, status)
    }

    // endregion

    // region resolveHomeState

    private val contact = Contact(id = 1, name = "ماما", phone = "000")

    private fun resolve(
        today: List<SlotWithStatus> = emptyList(),
        yesterday: List<SlotWithStatus> = emptyList(),
        tomorrow: List<Slot> = emptyList(),
        acknowledged: Set<String> = emptySet()
    ) = DoseEngine.resolveHomeState(today, yesterday, tomorrow, contact, acknowledged)

    @Test
    fun testResolveHomeState_dueSlotFromYesterdayIsUsedWhenNothingDueToday() {
        val lateYesterday = Slot(testDate.minusDays(1), 1410, slot0800.items)

        val state = resolve(yesterday = listOf(SlotWithStatus(lateYesterday, SlotStatus.DUE)))

        assertEquals(HomeState.Due(lateYesterday, lateYesterday.items, emptyList()), state)
    }

    @Test
    fun testResolveHomeState_unacknowledgedMissedShowsMissedWithContact() {
        val today = listOf(SlotWithStatus(slot0800, SlotStatus.MISSED))

        val state = resolve(today = today)

        assertEquals(HomeState.Missed(slot0800, contact, today), state)
    }

    @Test
    fun testResolveHomeState_acknowledgedMissedFallsBackToIdle() {
        val upcoming = Slot(testDate, 840, emptyList())
        val today = listOf(
            SlotWithStatus(slot0800, SlotStatus.MISSED),
            SlotWithStatus(upcoming, SlotStatus.UPCOMING)
        )

        val state = resolve(today = today, acknowledged = setOf(slot0800.key))

        assertEquals(HomeState.Idle(nextSlot = upcoming, todaySlots = today), state)
    }

    @Test
    fun testResolveHomeState_idleUsesTomorrowWhenNothingLeftToday() {
        val tomorrowFirst = Slot(testDate.plusDays(1), 480, emptyList())
        val today = listOf(SlotWithStatus(slot0800, SlotStatus.COMPLETED))

        val state = resolve(today = today, tomorrow = listOf(tomorrowFirst, Slot(testDate.plusDays(1), 840, emptyList())))

        assertEquals(HomeState.Idle(nextSlot = tomorrowFirst, todaySlots = today), state)
    }

    @Test
    fun testResolveHomeState_idleWithNoSlotsAtAll() {
        assertEquals(HomeState.Idle(nextSlot = null, todaySlots = emptyList()), resolve())
    }

    // endregion

    // region calculateAlarmTriggers

    private fun epochAt(date: LocalDate, hour: Int, minute: Int = 0) =
        date.atTime(LocalTime.of(hour, minute)).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun testCalculateAlarmTriggers_nextDueIsFirstUpcomingToday() {
        val now = testDate.atTime(LocalTime.of(7, 0)).atZone(zone)
        val today = listOf(
            SlotWithStatus(slot0800, SlotStatus.UPCOMING),
            SlotWithStatus(Slot(testDate, 840, emptyList()), SlotStatus.UPCOMING)
        )

        val triggers = DoseEngine.calculateAlarmTriggers(today, emptyList(), now, graceMinutes = 60, realertMinutes = 5)

        assertEquals(AlarmTriggers(epochAt(testDate, 8), null, null), triggers)
    }

    @Test
    fun testCalculateAlarmTriggers_nextDueFallsBackToTomorrow() {
        val now = testDate.atTime(LocalTime.of(22, 0)).atZone(zone)
        val tomorrow = listOf(Slot(testDate.plusDays(1), 480, emptyList()))

        val triggers = DoseEngine.calculateAlarmTriggers(emptyList(), tomorrow, now, 60, 5)

        assertEquals(epochAt(testDate.plusDays(1), 8), triggers.dueEpochMillis)
    }

    @Test
    fun testCalculateAlarmTriggers_noSlotsGivesNoTriggers() {
        val now = testDate.atTime(LocalTime.of(22, 0)).atZone(zone)

        val triggers = DoseEngine.calculateAlarmTriggers(emptyList(), emptyList(), now, 60, 5)

        assertEquals(AlarmTriggers(null, null, null), triggers)
    }

    @Test
    fun testCalculateAlarmTriggers_dueSlotSetsRealertAndMissedCheck() {
        val now = testDate.atTime(LocalTime.of(8, 10)).atZone(zone)
        val today = listOf(SlotWithStatus(slot0800, SlotStatus.DUE))

        val triggers = DoseEngine.calculateAlarmTriggers(today, emptyList(), now, graceMinutes = 60, realertMinutes = 5)

        assertEquals(epochAt(testDate, 8, 15), triggers.realertEpochMillis)
        assertEquals(epochAt(testDate, 9), triggers.missedCheckEpochMillis)
    }

    @Test
    fun testCalculateAlarmTriggers_noRealertWhenItWouldPassGraceEnd() {
        val now = testDate.atTime(LocalTime.of(8, 57)).atZone(zone)
        val today = listOf(SlotWithStatus(slot0800, SlotStatus.DUE))

        val triggers = DoseEngine.calculateAlarmTriggers(today, emptyList(), now, graceMinutes = 60, realertMinutes = 5)

        assertNull(triggers.realertEpochMillis)
        assertEquals(epochAt(testDate, 9), triggers.missedCheckEpochMillis)
    }

    // endregion

    @Test
    fun testScheduleIsDueOnDay_readsMaskBitsMondayToSunday() {
        val mondayAndSunday = Schedule(timeOfDayMinutes = 480, daysOfWeekMask = 0b1000001)

        assertEquals(listOf(true, false, false, false, false, false, true), (1..7).map { mondayAndSunday.isDueOnDay(it) })
        assertTrue((1..7).all { Schedule(timeOfDayMinutes = 480).isDueOnDay(it) })
    }

    @Test
    fun testSlotKey_combinesDateAndTime() {
        assertEquals("2026-10-08_480", slot0800.key)
    }
}
