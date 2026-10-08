package com.family.dawa.domain.usecase.caregiver

import app.cash.turbine.test
import com.family.dawa.domain.model.HomeState
import com.family.dawa.domain.repository.IDoseRepository
import com.family.dawa.domain.scheduler.IAlarmScheduler
import com.family.dawa.testutil.slot
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CaregiverUseCasesTest {

    private val doseRepository: IDoseRepository = mockk(relaxed = true)
    private val alarmScheduler: IAlarmScheduler = mockk(relaxed = true)

    @Test
    fun testGetCaregiverHomeState_returnsRepositoryFlow() = runTest {
        val idle = HomeState.Idle(nextSlot = slot(), todaySlots = emptyList())
        every { doseRepository.observeHomeState() } returns flowOf(idle)

        GetCaregiverHomeStateUseCase(doseRepository)().test {
            assertEquals(idle, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testConfirmDoseSlot_confirmsThenResyncsAlarms() = runTest {
        val doseSlot = slot()

        ConfirmDoseSlotUseCase(doseRepository, alarmScheduler)(doseSlot)

        coVerifyOrder {
            doseRepository.confirmSlot(doseSlot)
            alarmScheduler.resync()
        }
    }

    @Test
    fun testAcknowledgeMissedSlot_acknowledgesThenResyncsAlarms() = runTest {
        val doseSlot = slot()

        AcknowledgeMissedSlotUseCase(doseRepository, alarmScheduler)(doseSlot)

        coVerifyOrder {
            doseRepository.acknowledgeMissed(doseSlot)
            alarmScheduler.resync()
        }
    }
}
