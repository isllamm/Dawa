package com.family.dawa.domain.ledger

import com.family.dawa.data.db.DoseEventDao
import com.family.dawa.data.db.DoseEventEntity
import com.family.dawa.data.repo.MedicationRepository
import com.family.dawa.domain.model.EventStatus
import com.family.dawa.domain.model.Medication
import com.family.dawa.domain.model.RecordedBy
import com.family.dawa.domain.model.Schedule
import com.family.dawa.testutil.TEST_DATE
import com.family.dawa.testutil.at
import com.family.dawa.testutil.doseItem
import com.family.dawa.testutil.slot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import io.mockk.slot as captureSlot

class DoseLedgerTest {

    private val doseEventDao: DoseEventDao = mockk(relaxed = true)
    private val medicationRepository: MedicationRepository = mockk()
    private lateinit var ledger: DoseLedger

    private val twoMedSlot = slot(
        timeMinutes = 480,
        items = listOf(
            doseItem(scheduleId = 11, medicationId = 1, name = "بانادول", quantityHalves = 2),
            doseItem(scheduleId = 12, medicationId = 2, name = "كونكور", quantityHalves = 1)
        )
    )

    @Before
    fun setUp() {
        ledger = DoseLedger(doseEventDao, medicationRepository)
    }

    private fun storedEvent(
        id: Long = 5,
        timeMinutes: Int = 480,
        status: EventStatus = EventStatus.MISSED
    ) = DoseEventEntity(
        id = id,
        scheduleId = 11,
        medicationId = 1,
        slotDate = TEST_DATE,
        slotTimeMinutes = timeMinutes,
        status = status,
        recordedAt = 0L,
        recordedBy = RecordedBy.SYSTEM
    )

    // region confirmSlot

    @Test
    fun testConfirmSlot_insertsOneCompletedEventPerItem() = runTest {
        val now = at(8, 10)
        val nowMillis = now.toInstant().toEpochMilli()
        val inserted = mutableListOf<DoseEventEntity>()
        coEvery { doseEventDao.insertOrReplace(capture(inserted)) } returns 1L

        ledger.confirmSlot(twoMedSlot, now)

        assertEquals(
            listOf(
                DoseEventEntity(
                    scheduleId = 11, medicationId = 1, slotDate = TEST_DATE, slotTimeMinutes = 480,
                    status = EventStatus.COMPLETED, recordedAt = nowMillis, recordedBy = RecordedBy.CAREGIVER,
                    acknowledgedAt = nowMillis, medNameSnapshot = "بانادول", quantityHalvesSnapshot = 2
                ),
                DoseEventEntity(
                    scheduleId = 12, medicationId = 2, slotDate = TEST_DATE, slotTimeMinutes = 480,
                    status = EventStatus.COMPLETED, recordedAt = nowMillis, recordedBy = RecordedBy.CAREGIVER,
                    acknowledgedAt = nowMillis, medNameSnapshot = "كونكور", quantityHalvesSnapshot = 1
                )
            ),
            inserted
        )
    }

    @Test
    fun testConfirmSlot_usesGivenRecordedBy() = runTest {
        val inserted = mutableListOf<DoseEventEntity>()
        coEvery { doseEventDao.insertOrReplace(capture(inserted)) } returns 1L

        ledger.confirmSlot(twoMedSlot, at(8, 10), RecordedBy.ADMIN)

        assertTrue(inserted.all { it.recordedBy == RecordedBy.ADMIN })
    }

    // endregion

    // region acknowledgeMissed

    @Test
    fun testAcknowledgeMissed_withNoEventInsertsAcknowledgedMissedEvents() = runTest {
        val now = at(9, 30)
        val nowMillis = now.toInstant().toEpochMilli()
        coEvery { doseEventDao.getEventsForDateSync(TEST_DATE) } returns emptyList()
        val inserted = captureSlot<List<DoseEventEntity>>()
        coEvery { doseEventDao.insertAllOrIgnore(capture(inserted)) } returns Unit

        ledger.acknowledgeMissed(twoMedSlot, now)

        assertEquals(2, inserted.captured.size)
        inserted.captured.forEach {
            assertEquals(EventStatus.MISSED, it.status)
            assertEquals(RecordedBy.SYSTEM, it.recordedBy)
            assertEquals(nowMillis, it.acknowledgedAt)
            assertEquals(480, it.slotTimeMinutes)
        }
        coVerify(exactly = 0) { doseEventDao.acknowledgeSlot(any(), any(), any()) }
    }

    @Test
    fun testAcknowledgeMissed_withExistingEventOnlyMarksItAcknowledged() = runTest {
        val now = at(9, 30)
        coEvery { doseEventDao.getEventsForDateSync(TEST_DATE) } returns listOf(storedEvent(timeMinutes = 480))

        ledger.acknowledgeMissed(twoMedSlot, now)

        coVerify { doseEventDao.acknowledgeSlot(TEST_DATE, 480, now.toInstant().toEpochMilli()) }
        coVerify(exactly = 0) { doseEventDao.insertAllOrIgnore(any()) }
    }

    @Test
    fun testAcknowledgeMissed_eventForAnotherTimeDoesNotCount() = runTest {
        coEvery { doseEventDao.getEventsForDateSync(TEST_DATE) } returns listOf(storedEvent(timeMinutes = 840))

        ledger.acknowledgeMissed(twoMedSlot, at(9, 30))

        coVerify { doseEventDao.insertAllOrIgnore(match { it.size == 2 }) }
        coVerify(exactly = 0) { doseEventDao.acknowledgeSlot(any(), any(), any()) }
    }

    // endregion

    // region reconcile

    private val med = Medication(id = 1, name = "بانادول")

    private fun givenSchedules(vararg schedules: Schedule) {
        coEvery { medicationRepository.getAllActiveSchedulesSync() } returns schedules.toList()
        coEvery { medicationRepository.getAllActiveMedicationsSync() } returns mapOf(1L to med)
        coEvery { medicationRepository.getAllPhotosSync() } returns emptyMap()
    }

    @Test
    fun testReconcile_recordsMissedForSlotPastGraceWithNoEvent() = runTest {
        givenSchedules(Schedule(id = 11, medicationId = 1, timeOfDayMinutes = 480))
        coEvery { doseEventDao.getEventsForDateSync(any()) } returns emptyList()
        val inserted = mutableListOf<List<DoseEventEntity>>()
        coEvery { doseEventDao.insertAllOrIgnore(capture(inserted)) } returns Unit

        ledger.reconcile(now = at(9, 1), graceMinutes = 60)

        val today = inserted.flatten().single { it.slotDate == TEST_DATE }
        assertEquals(EventStatus.MISSED, today.status)
        assertEquals(RecordedBy.SYSTEM, today.recordedBy)
        assertEquals(null, today.acknowledgedAt)
        // recordedAt is the end of the grace period, not "now"
        assertEquals(at(9, 0).toInstant().toEpochMilli(), today.recordedAt)
    }

    @Test
    fun testReconcile_alsoRecordsMissedSlotsFromYesterday() = runTest {
        givenSchedules(Schedule(id = 11, medicationId = 1, timeOfDayMinutes = 1200))
        coEvery { doseEventDao.getEventsForDateSync(any()) } returns emptyList()
        val inserted = mutableListOf<List<DoseEventEntity>>()
        coEvery { doseEventDao.insertAllOrIgnore(capture(inserted)) } returns Unit

        ledger.reconcile(now = at(9, 0), graceMinutes = 60)

        // Today's 20:00 slot is still in the future, so only yesterday's is recorded
        assertEquals(listOf(TEST_DATE.minusDays(1)), inserted.flatten().map { it.slotDate })
    }

    @Test
    fun testReconcile_skipsSlotStillWithinGrace() = runTest {
        givenSchedules(Schedule(id = 11, medicationId = 1, timeOfDayMinutes = 480))
        coEvery { doseEventDao.getEventsForDateSync(TEST_DATE.minusDays(1)) } returns listOf(
            storedEvent().copy(slotDate = TEST_DATE.minusDays(1))
        )
        coEvery { doseEventDao.getEventsForDateSync(TEST_DATE) } returns emptyList()

        ledger.reconcile(now = at(8, 30), graceMinutes = 60)

        coVerify(exactly = 0) { doseEventDao.insertAllOrIgnore(any()) }
    }

    @Test
    fun testReconcile_skipsSlotThatAlreadyHasEvent() = runTest {
        givenSchedules(Schedule(id = 11, medicationId = 1, timeOfDayMinutes = 480))
        coEvery { doseEventDao.getEventsForDateSync(TEST_DATE.minusDays(1)) } returns listOf(
            storedEvent().copy(slotDate = TEST_DATE.minusDays(1))
        )
        coEvery { doseEventDao.getEventsForDateSync(TEST_DATE) } returns listOf(storedEvent(status = EventStatus.COMPLETED))

        ledger.reconcile(now = at(12, 0), graceMinutes = 60)

        coVerify(exactly = 0) { doseEventDao.insertAllOrIgnore(any()) }
    }

    // endregion

    @Test
    fun testResetTodayEvents_deletesEventsForThatDate() = runTest {
        ledger.resetTodayEvents(TEST_DATE)

        coVerify { doseEventDao.deleteEventsForDate(TEST_DATE) }
    }

    // region updateEventStatus

    @Test
    fun testUpdateEventStatus_updatesMatchingEventAsAdmin() = runTest {
        val now = at(15, 0)
        coEvery { doseEventDao.getEventsBetweenSync(any(), any()) } returns listOf(storedEvent(id = 4), storedEvent(id = 5))
        val updated = captureSlot<DoseEventEntity>()
        coEvery { doseEventDao.update(capture(updated)) } returns Unit

        ledger.updateEventStatus(eventId = 5, newStatus = EventStatus.COMPLETED, now = now)

        assertEquals(
            storedEvent(id = 5).copy(
                status = EventStatus.COMPLETED,
                recordedAt = now.toInstant().toEpochMilli(),
                recordedBy = RecordedBy.ADMIN
            ),
            updated.captured
        )
    }

    @Test
    fun testUpdateEventStatus_unknownIdDoesNothing() = runTest {
        coEvery { doseEventDao.getEventsBetweenSync(any(), any()) } returns listOf(storedEvent(id = 4))

        ledger.updateEventStatus(eventId = 99, newStatus = EventStatus.COMPLETED, now = at(15, 0))

        coVerify(exactly = 0) { doseEventDao.update(any()) }
    }

    @Test
    fun testUpdateEventStatus_searchesLast30DaysFromGivenTime() = runTest {
        // A date far from the real clock, as the debug time offset can produce
        val debugNow = at(15, 0, date = TEST_DATE.plusYears(1))
        coEvery { doseEventDao.getEventsBetweenSync(any(), any()) } returns emptyList()

        ledger.updateEventStatus(eventId = 5, newStatus = EventStatus.COMPLETED, now = debugNow)

        coVerify { doseEventDao.getEventsBetweenSync(debugNow.toLocalDate().minusDays(30), debugNow.toLocalDate()) }
    }

    // endregion
}
