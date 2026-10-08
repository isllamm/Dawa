package com.family.dawa.domain.usecase.debug

import com.family.dawa.core.time.DebugTimeProvider
import com.family.dawa.data.demo.DemoSeeder
import com.family.dawa.domain.repository.IDoseRepository
import com.family.dawa.domain.repository.ISettingsRepository
import com.family.dawa.domain.scheduler.IAlarmScheduler
import com.family.dawa.testutil.FixedTimeProvider
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DebugUseCasesTest {

    private val settingsRepository: ISettingsRepository = mockk(relaxed = true)
    private val doseRepository: IDoseRepository = mockk(relaxed = true)
    private val demoSeeder: DemoSeeder = mockk(relaxed = true)
    private val alarmScheduler: IAlarmScheduler = mockk(relaxed = true)

    @Test
    fun testSetDebugTimeOffset_setsClockSavesOffsetThenResyncs() = runTest {
        val timeProvider = DebugTimeProvider(FixedTimeProvider())

        SetDebugTimeOffsetUseCase(timeProvider, settingsRepository, alarmScheduler)(3_600_000L)

        assertEquals(3_600_000L, timeProvider.offsetMillis)
        coVerifyOrder {
            settingsRepository.setDebugTimeOffset(3_600_000L)
            alarmScheduler.resync()
        }
    }

    @Test
    fun testResetTodayEvents_resetsThenResyncs() = runTest {
        ResetTodayEventsUseCase(doseRepository, alarmScheduler)()

        coVerifyOrder {
            doseRepository.resetTodayEvents()
            alarmScheduler.resync()
        }
    }

    @Test
    fun testReseedDemoData_forceSeedsThenResyncs() = runTest {
        ReseedDemoDataUseCase(demoSeeder, alarmScheduler)()

        coVerifyOrder {
            demoSeeder.forceSeed()
            alarmScheduler.resync()
        }
    }
}
