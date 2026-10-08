package com.family.dawa.domain.usecase.settings

import app.cash.turbine.test
import com.family.dawa.domain.model.AppSettings
import com.family.dawa.domain.repository.ISettingsRepository
import com.family.dawa.domain.scheduler.IAlarmScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUseCasesTest {

    private val settingsRepository: ISettingsRepository = mockk(relaxed = true)
    private val alarmScheduler: IAlarmScheduler = mockk(relaxed = true)
    private val settings = AppSettings(patientName = "جدو", graceMinutes = 30)

    @Test
    fun testGetSettings_flowAndCurrentComeFromRepository() = runTest {
        every { settingsRepository.settingsFlow } returns flowOf(settings)
        coEvery { settingsRepository.getSettings() } returns settings
        val useCase = GetSettingsUseCase(settingsRepository)

        useCase().test {
            assertEquals(settings, awaitItem())
            awaitComplete()
        }
        assertEquals(settings, useCase.current())
    }

    @Test
    fun testUpdateSettings_passesAllValuesThenResyncs() = runTest {
        UpdateSettingsUseCase(settingsRepository, alarmScheduler)(
            patientName = "جدو",
            caregiverName = "تيتا",
            graceMinutes = 45,
            realertMinutes = 10,
            voiceEnabled = false,
            vibrationEnabled = true,
            breakfastMinutes = 420,
            lunchMinutes = 780,
            dinnerMinutes = 1140,
            sleepMinutes = 1320
        )

        coVerifyOrder {
            settingsRepository.updateSettings(
                patientName = "جدو",
                caregiverName = "تيتا",
                graceMinutes = 45,
                realertMinutes = 10,
                voiceEnabled = false,
                vibrationEnabled = true,
                breakfastMinutes = 420,
                lunchMinutes = 780,
                dinnerMinutes = 1140,
                sleepMinutes = 1320
            )
            alarmScheduler.resync()
        }
    }

    @Test
    fun testUpdateSettings_setDisclaimerAcceptedDoesNotResync() = runTest {
        UpdateSettingsUseCase(settingsRepository, alarmScheduler).setDisclaimerAccepted(true)

        coVerify { settingsRepository.setDisclaimerAccepted(true) }
        coVerify(exactly = 0) { alarmScheduler.resync() }
    }

    @Test
    fun testVerifyAdminPin_returnsRepositoryResult() = runTest {
        coEvery { settingsRepository.verifyAdminPin("1234") } returns true
        coEvery { settingsRepository.verifyAdminPin("0000") } returns false
        val useCase = VerifyAdminPinUseCase(settingsRepository)

        assertTrue(useCase("1234"))
        assertFalse(useCase("0000"))
    }

    @Test
    fun testUpdateAdminPin_savesPin() = runTest {
        UpdateAdminPinUseCase(settingsRepository)("4321")

        coVerify { settingsRepository.setAdminPin("4321") }
    }
}
