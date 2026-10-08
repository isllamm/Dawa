package com.family.dawa.presentation.admin.debug

import com.family.dawa.core.time.DebugTimeProvider
import com.family.dawa.domain.scheduler.IAlarmScheduler
import com.family.dawa.domain.usecase.debug.ResetTodayEventsUseCase
import com.family.dawa.domain.usecase.debug.SetDebugTimeOffsetUseCase
import com.family.dawa.testutil.FixedTimeProvider
import com.family.dawa.testutil.MainDispatcherRule
import com.family.dawa.testutil.at
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DebugViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val timeProvider = DebugTimeProvider(FixedTimeProvider(at(9)))
    private val setDebugTimeOffset: SetDebugTimeOffsetUseCase = mockk()
    private val resetTodayEvents: ResetTodayEventsUseCase = mockk(relaxed = true)
    private val alarmScheduler: IAlarmScheduler = mockk(relaxed = true)

    @Before
    fun setUp() {
        // Behave like the real use case: move the shared clock
        coEvery { setDebugTimeOffset(any()) } answers { timeProvider.offsetMillis = firstArg() }
    }

    private fun createViewModel() =
        DebugViewModel(timeProvider, setDebugTimeOffset, resetTodayEvents, alarmScheduler)

    @Test
    fun testInit_showsCurrentOffsetAndSimulatedTime() {
        timeProvider.offsetMillis = 2 * 60_000L

        val vm = createViewModel()

        assertEquals(120_000L, vm.state.value.currentOffsetMs)
        assertEquals(at(9, 2), vm.state.value.simulatedNow)
    }

    @Test
    fun testFastForward_addsMinutesToOffset() {
        val vm = createViewModel()

        vm.sendIntent(DebugIntent.FastForward(addedMinutes = 60))

        coVerify { setDebugTimeOffset(3_600_000L) }
        assertEquals(3_600_000L, vm.state.value.currentOffsetMs)
        assertEquals(at(10), vm.state.value.simulatedNow)
        assertTrue(vm.state.value.statusMessage.contains("60"))
    }

    @Test
    fun testFastForward_twiceAddsUp() {
        val vm = createViewModel()

        vm.sendIntent(DebugIntent.FastForward(addedMinutes = 60))
        vm.sendIntent(DebugIntent.FastForward(addedMinutes = 30))

        coVerify { setDebugTimeOffset(5_400_000L) }
        assertEquals(at(10, 30), vm.state.value.simulatedNow)
    }

    @Test
    fun testResetOffset_returnsToRealTime() {
        val vm = createViewModel()
        vm.sendIntent(DebugIntent.FastForward(addedMinutes = 60))

        vm.sendIntent(DebugIntent.ResetOffset)

        coVerify { setDebugTimeOffset(0L) }
        assertEquals(0L, vm.state.value.currentOffsetMs)
        assertEquals(at(9), vm.state.value.simulatedNow)
        assertTrue(vm.state.value.statusMessage.isNotBlank())
    }

    @Test
    fun testTriggerTenSecondAlarm_schedulesTestAlarm() {
        val vm = createViewModel()

        vm.sendIntent(DebugIntent.TriggerTenSecondAlarm)

        verify { alarmScheduler.scheduleTestAlarmInTenSeconds() }
        assertTrue(vm.state.value.statusMessage.isNotBlank())
    }

    @Test
    fun testResetToday_clearsTodayEvents() {
        val vm = createViewModel()

        vm.sendIntent(DebugIntent.ResetToday)

        coVerify { resetTodayEvents() }
        assertTrue(vm.state.value.statusMessage.isNotBlank())
    }
}
