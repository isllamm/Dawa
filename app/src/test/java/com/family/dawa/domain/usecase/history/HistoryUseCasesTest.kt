package com.family.dawa.domain.usecase.history

import app.cash.turbine.test
import com.family.dawa.domain.model.DoseEvent
import com.family.dawa.domain.model.EventStatus
import com.family.dawa.domain.model.SlotStatus
import com.family.dawa.domain.model.SlotWithStatus
import com.family.dawa.domain.repository.IDoseRepository
import com.family.dawa.testutil.TEST_DATE
import com.family.dawa.testutil.slot
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryUseCasesTest {

    private val doseRepository: IDoseRepository = mockk(relaxed = true)

    @Test
    fun testGetTodayTimeline_returnsRepositoryTimeline() = runTest {
        val timeline = listOf(SlotWithStatus(slot(), SlotStatus.UPCOMING))
        every { doseRepository.observeTodayTimeline() } returns flowOf(timeline)

        GetTodayTimelineUseCase(doseRepository)().test {
            assertEquals(timeline, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testGetDoseHistory_passesDateRangeToRepository() = runTest {
        val start = TEST_DATE.minusDays(14)
        val events = listOf(
            DoseEvent(id = 1, scheduleId = 1, medicationId = 1, slotDate = TEST_DATE, slotTimeMinutes = 480, status = EventStatus.COMPLETED)
        )
        every { doseRepository.getEventsBetween(start, TEST_DATE) } returns flowOf(events)

        GetDoseHistoryUseCase(doseRepository)(start, TEST_DATE).test {
            assertEquals(events, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testUpdateDoseEventStatus_updatesRepository() = runTest {
        UpdateDoseEventStatusUseCase(doseRepository)(eventId = 3, newStatus = EventStatus.SKIPPED)

        coVerify { doseRepository.updateEventStatus(3, EventStatus.SKIPPED) }
    }
}
