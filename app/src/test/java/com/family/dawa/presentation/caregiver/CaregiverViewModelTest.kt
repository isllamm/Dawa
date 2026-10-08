package com.family.dawa.presentation.caregiver

import com.family.dawa.core.audio.VoicePlayer
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.domain.model.Contact
import com.family.dawa.domain.model.HomeState
import com.family.dawa.domain.model.Slot
import com.family.dawa.domain.model.SlotStatus
import com.family.dawa.domain.model.SlotWithStatus
import com.family.dawa.domain.usecase.caregiver.AcknowledgeMissedSlotUseCase
import com.family.dawa.domain.usecase.caregiver.ConfirmDoseSlotUseCase
import com.family.dawa.domain.usecase.caregiver.GetCaregiverHomeStateUseCase
import com.family.dawa.testutil.MainDispatcherRule
import com.family.dawa.testutil.clearForTest
import com.family.dawa.testutil.doseItem
import com.family.dawa.testutil.slot
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaregiverViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val homeStates = MutableStateFlow<HomeState>(HomeState.Idle(nextSlot = null, todaySlots = emptyList()))
    private val getHomeState: GetCaregiverHomeStateUseCase = mockk()
    private val confirmDoseSlot: ConfirmDoseSlotUseCase = mockk(relaxed = true)
    private val acknowledgeMissedSlot: AcknowledgeMissedSlotUseCase = mockk(relaxed = true)
    private val voicePlayer: VoicePlayer = mockk(relaxed = true)

    private val morning = slot(timeMinutes = 480, items = listOf(doseItem(name = "بانادول")))
    private val afternoon = slot(timeMinutes = 840, items = listOf(doseItem(scheduleId = 2, name = "كونكور")))
    private val contact = Contact(id = 1, name = "ماما", phone = "0100")

    @Before
    fun setUp() {
        every { getHomeState() } returns homeStates
    }

    /** By default the screen is visible, as when the caregiver is looking at it. */
    private fun createViewModel(visible: Boolean = true) =
        CaregiverViewModel(getHomeState, confirmDoseSlot, acknowledgeMissedSlot, voicePlayer).apply {
            if (visible) sendIntent(CaregiverIntent.ScreenVisibilityChanged(visible = true))
        }

    private fun due(slot: Slot, todaySlots: List<SlotWithStatus> = emptyList()) =
        HomeState.Due(slot = slot, items = slot.items, todaySlots = todaySlots)

    // region home state and automatic voice

    @Test
    fun testInit_stateFollowsHomeStateFlow() {
        val vm = createViewModel()

        homeStates.value = due(morning)

        assertEquals(due(morning), vm.state.value.homeState)
    }

    @Test
    fun testInit_isNotLoadedUntilFirstHomeStateArrives() {
        val pending = MutableSharedFlow<HomeState>(replay = 1)
        every { getHomeState() } returns pending
        val vm = createViewModel()
        assertFalse(vm.state.value.isLoaded)

        pending.tryEmit(due(morning))

        assertTrue(vm.state.value.isLoaded)
        assertEquals(due(morning), vm.state.value.homeState)
    }

    @Test
    fun testIdleState_playsNothing() {
        createViewModel()

        verify(exactly = 0) { voicePlayer.playDueAlert(any(), any()) }
        verify(exactly = 0) { voicePlayer.playMissedAlert(any()) }
        verify(exactly = 0) { voicePlayer.playDoneAlert() }
        verify(exactly = 0) { voicePlayer.playIdleAnnouncement(any()) }
    }

    @Test
    fun testDueState_playsDueAlertOnlyOncePerSlot() {
        createViewModel()

        homeStates.value = due(morning)
        // Same slot again with a changed timeline must not replay the alert
        homeStates.value = due(morning, todaySlots = listOf(SlotWithStatus(morning, SlotStatus.DUE)))

        verify(exactly = 1) { voicePlayer.playDueAlert(morning.items, any()) }
    }

    @Test
    fun testDueState_newSlotPlaysAgain() {
        createViewModel()

        homeStates.value = due(morning)
        homeStates.value = due(afternoon)

        verify(exactly = 1) { voicePlayer.playDueAlert(morning.items, any()) }
        verify(exactly = 1) { voicePlayer.playDueAlert(afternoon.items, any()) }
    }

    @Test
    fun testMissedState_playsMissedAlertWithContactName() {
        createViewModel()

        homeStates.value = HomeState.Missed(slot = morning, contact = contact, todaySlots = emptyList())

        verify(exactly = 1) { voicePlayer.playMissedAlert("ماما") }
    }

    @Test
    fun testDoneState_playsDoneAlert() {
        createViewModel()

        homeStates.value = HomeState.Done(slot = morning, nextSlot = null)

        verify(exactly = 1) { voicePlayer.playDoneAlert() }
    }

    @Test
    fun testHiddenScreen_doesNotAutoPlay() {
        createViewModel(visible = false)

        homeStates.value = due(morning)

        verify(exactly = 0) { voicePlayer.playDueAlert(any(), any()) }
    }

    @Test
    fun testBecomingVisible_playsAlertThatArrivedWhileHidden() {
        val vm = createViewModel(visible = false)
        homeStates.value = due(morning)

        vm.sendIntent(CaregiverIntent.ScreenVisibilityChanged(visible = true))

        verify(exactly = 1) { voicePlayer.playDueAlert(morning.items, any()) }
    }

    @Test
    fun testHiddenThenVisibleAgain_doesNotReplaySameSlot() {
        val vm = createViewModel()
        homeStates.value = due(morning)

        vm.sendIntent(CaregiverIntent.ScreenVisibilityChanged(visible = false))
        vm.sendIntent(CaregiverIntent.ScreenVisibilityChanged(visible = true))

        verify(exactly = 1) { voicePlayer.playDueAlert(morning.items, any()) }
        // Hiding must not cut off the voice: the reminder screen on top shares the same player
        verify(exactly = 0) { voicePlayer.stop() }
    }

    // endregion

    // region intents

    @Test
    fun testConfirmTaken_confirmsPlaysDoneAndShowsOverlayFor4500ms() = runTest {
        val vm = createViewModel()

        vm.sendIntent(CaregiverIntent.ConfirmTaken(morning))

        coVerify { confirmDoseSlot(morning) }
        verify { voicePlayer.playDoneAlert() }
        assertEquals(morning, vm.state.value.doneOverlaySlot)

        advanceTimeBy(4_499)
        assertEquals(morning, vm.state.value.doneOverlaySlot)

        advanceTimeBy(2)
        assertNull(vm.state.value.doneOverlaySlot)
    }

    @Test
    fun testConfirmTaken_secondConfirmRestartsOverlayTimer() = runTest {
        val vm = createViewModel()

        vm.sendIntent(CaregiverIntent.ConfirmTaken(morning))
        advanceTimeBy(3_000)
        vm.sendIntent(CaregiverIntent.ConfirmTaken(afternoon))

        advanceTimeBy(2_000) // 5s after the first confirm: first timer must not clear the overlay
        assertEquals(afternoon, vm.state.value.doneOverlaySlot)

        advanceTimeBy(2_600) // 4.6s after the second confirm
        assertNull(vm.state.value.doneOverlaySlot)
    }

    @Test
    fun testAcknowledgeMissed_callsUseCase() {
        val vm = createViewModel()

        vm.sendIntent(CaregiverIntent.AcknowledgeMissed(morning))

        coVerify { acknowledgeMissedSlot(morning) }
    }

    @Test
    fun testReplayVoice_dueReplaysDueAlert() {
        val vm = createViewModel()
        homeStates.value = due(morning)

        vm.sendIntent(CaregiverIntent.ReplayVoice)

        // Once automatically, once from the replay
        verify(exactly = 2) { voicePlayer.playDueAlert(morning.items, any()) }
    }

    @Test
    fun testReplayVoice_missedReplaysMissedAlert() {
        val vm = createViewModel()
        homeStates.value = HomeState.Missed(slot = morning, contact = null, todaySlots = emptyList())

        vm.sendIntent(CaregiverIntent.ReplayVoice)

        verify(exactly = 2) { voicePlayer.playMissedAlert(null) }
    }

    @Test
    fun testReplayVoice_doneReplaysDoneAlert() {
        val vm = createViewModel()
        homeStates.value = HomeState.Done(slot = morning, nextSlot = null)

        vm.sendIntent(CaregiverIntent.ReplayVoice)

        verify(exactly = 2) { voicePlayer.playDoneAlert() }
    }

    @Test
    fun testReplayVoice_idleAnnouncesNextSlotTime() {
        val vm = createViewModel()
        homeStates.value = HomeState.Idle(nextSlot = afternoon, todaySlots = emptyList())

        vm.sendIntent(CaregiverIntent.ReplayVoice)

        verify { voicePlayer.playIdleAnnouncement(ArabicFormatters.formatMinutesOfDay(840)) }
    }

    @Test
    fun testReplayVoice_idleWithoutNextSlotAnnouncesNull() {
        val vm = createViewModel()

        vm.sendIntent(CaregiverIntent.ReplayVoice)

        verify { voicePlayer.playIdleAnnouncement(null) }
    }

    // endregion

    @Test
    fun testOnCleared_stopsVoicePlayer() {
        val vm = createViewModel()

        vm.clearForTest()

        verify { voicePlayer.stop() }
    }
}
