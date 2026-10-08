package com.family.dawa.presentation.admin.history

import com.family.dawa.domain.model.DoseEvent
import com.family.dawa.domain.model.EventStatus
import com.family.dawa.domain.usecase.history.GetDoseHistoryUseCase
import com.family.dawa.domain.usecase.history.UpdateDoseEventStatusUseCase
import com.family.dawa.testutil.FixedTimeProvider
import com.family.dawa.testutil.MainDispatcherRule
import com.family.dawa.testutil.TEST_DATE
import com.family.dawa.testutil.at
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AdminHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getDoseHistory: GetDoseHistoryUseCase = mockk()
    private val updateDoseEventStatus: UpdateDoseEventStatusUseCase = mockk(relaxed = true)
    private val timeProvider = FixedTimeProvider(at(9))

    private val events = listOf(
        DoseEvent(id = 7, scheduleId = 1, medicationId = 1, slotDate = TEST_DATE, slotTimeMinutes = 480, status = EventStatus.MISSED)
    )

    private fun createViewModel() = AdminHistoryViewModel(getDoseHistory, updateDoseEventStatus, timeProvider)

    @Test
    fun testInit_loadsLast14DaysUpToToday() {
        every { getDoseHistory(any(), any()) } returns flowOf(events)

        val vm = createViewModel()

        verify { getDoseHistory(TEST_DATE.minusDays(14), TEST_DATE) }
        assertEquals(AdminHistoryState(events = events, today = TEST_DATE, isLoading = false), vm.state.value)
    }

    @Test
    fun testMarkGivenLate_marksEventCompleted() {
        every { getDoseHistory(any(), any()) } returns flowOf(events)
        val vm = createViewModel()

        vm.sendIntent(AdminHistoryIntent.MarkGivenLate(eventId = 7))

        coVerify { updateDoseEventStatus(7, EventStatus.COMPLETED) }
    }
}
