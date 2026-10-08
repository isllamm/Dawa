package com.family.dawa.presentation.admin.settings

import app.cash.turbine.test
import com.family.dawa.domain.model.AppSettings
import com.family.dawa.domain.usecase.debug.ReseedDemoDataUseCase
import com.family.dawa.domain.usecase.settings.GetSettingsUseCase
import com.family.dawa.domain.usecase.settings.UpdateSettingsUseCase
import com.family.dawa.testutil.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AdminSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = AppSettings(
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
    private val settingsFlow = MutableStateFlow(settings)
    private val getSettings: GetSettingsUseCase = mockk()
    private val updateSettings: UpdateSettingsUseCase = mockk(relaxed = true)
    private val reseedDemoData: ReseedDemoDataUseCase = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { getSettings() } returns settingsFlow
    }

    private fun createViewModel() = AdminSettingsViewModel(getSettings, updateSettings, reseedDemoData)

    @Test
    fun testInit_fillsFormFromSettings() {
        val vm = createViewModel()

        assertEquals(
            AdminSettingsState(
                settings = settings,
                patientName = "جدو",
                caregiverName = "تيتا",
                graceMinutes = "45",
                realertMinutes = "10"
            ),
            vm.state.value
        )
    }

    @Test
    fun testFieldChanges_updateState() {
        val vm = createViewModel()

        vm.sendIntent(AdminSettingsIntent.PatientNameChanged("بابا"))
        vm.sendIntent(AdminSettingsIntent.CaregiverNameChanged("ماما"))
        vm.sendIntent(AdminSettingsIntent.GraceMinutesChanged("30"))
        vm.sendIntent(AdminSettingsIntent.RealertMinutesChanged("3"))

        val state = vm.state.value
        assertEquals("بابا", state.patientName)
        assertEquals("ماما", state.caregiverName)
        assertEquals("30", state.graceMinutes)
        assertEquals("3", state.realertMinutes)
    }

    @Test
    fun testSave_sendsEditedValuesAndKeepsOtherSettings() = runTest {
        val vm = createViewModel()
        vm.sendIntent(AdminSettingsIntent.PatientNameChanged(" بابا "))
        vm.sendIntent(AdminSettingsIntent.GraceMinutesChanged("30"))

        vm.effect.test {
            vm.sendIntent(AdminSettingsIntent.SaveSettings)
            assertEquals(AdminSettingsEffect.NavigateBack, awaitItem())
        }
        coVerify {
            updateSettings(
                patientName = "بابا",
                caregiverName = "تيتا",
                graceMinutes = 30,
                realertMinutes = 10,
                voiceEnabled = false,
                vibrationEnabled = true,
                breakfastMinutes = 420,
                lunchMinutes = 780,
                dinnerMinutes = 1140,
                sleepMinutes = 1320
            )
        }
    }

    @Test
    fun testSave_invalidNumbersFallBackToDefaults() {
        val vm = createViewModel()
        vm.sendIntent(AdminSettingsIntent.GraceMinutesChanged("abc"))
        vm.sendIntent(AdminSettingsIntent.RealertMinutesChanged(""))

        vm.sendIntent(AdminSettingsIntent.SaveSettings)

        coVerify {
            updateSettings(
                patientName = any(),
                caregiverName = any(),
                graceMinutes = 60,
                realertMinutes = 5,
                voiceEnabled = any(),
                vibrationEnabled = any(),
                breakfastMinutes = any(),
                lunchMinutes = any(),
                dinnerMinutes = any(),
                sleepMinutes = any()
            )
        }
    }

    @Test
    fun testReseedDemoData_reseedsAndNavigatesBack() = runTest {
        val vm = createViewModel()

        vm.effect.test {
            vm.sendIntent(AdminSettingsIntent.ReseedDemoData)
            assertEquals(AdminSettingsEffect.NavigateBack, awaitItem())
        }
        coVerify { reseedDemoData() }
    }
}
