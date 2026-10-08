package com.family.dawa.presentation.caregiver

import androidx.lifecycle.viewModelScope
import com.family.dawa.core.audio.VoicePlayer
import com.family.dawa.core.base.MviViewModel
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.domain.model.HomeState
import com.family.dawa.domain.usecase.caregiver.AcknowledgeMissedSlotUseCase
import com.family.dawa.domain.usecase.caregiver.ConfirmDoseSlotUseCase
import com.family.dawa.domain.usecase.caregiver.GetCaregiverHomeStateUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CaregiverViewModel(
    private val getCaregiverHomeStateUseCase: GetCaregiverHomeStateUseCase,
    private val confirmDoseSlotUseCase: ConfirmDoseSlotUseCase,
    private val acknowledgeMissedSlotUseCase: AcknowledgeMissedSlotUseCase,
    private val voicePlayer: VoicePlayer
) : MviViewModel<CaregiverIntent, CaregiverState, CaregiverEffect>(CaregiverState()) {

    private var doneJob: Job? = null
    private var lastPlayedStateKey: String? = null
    private var isScreenVisible = false

    init {
        viewModelScope.launch {
            getCaregiverHomeStateUseCase().collectLatest { homeState ->
                updateState { copy(homeState = homeState, isLoaded = true) }
                autoPlayVoiceIfNeeded()
            }
        }
    }

    /**
     * Auto audio feedback on critical state transitions. Only while the screen is visible, so a
     * screen left open in the background doesn't talk over the full-screen reminder.
     */
    private fun autoPlayVoiceIfNeeded() {
        if (!isScreenVisible || !state.value.isLoaded) return
        val homeState = state.value.homeState

        val stateKey = when (homeState) {
            is HomeState.Due -> "due_${homeState.slot.key}"
            is HomeState.Missed -> "missed_${homeState.slot.key}"
            is HomeState.Idle -> "idle_${homeState.nextSlot?.key}"
            is HomeState.Done -> "done_${homeState.slot.key}"
        }

        if (stateKey != lastPlayedStateKey) {
            lastPlayedStateKey = stateKey
            when (homeState) {
                is HomeState.Due -> voicePlayer.playDueAlert(homeState.items)
                is HomeState.Missed -> voicePlayer.playMissedAlert(homeState.contact?.name)
                is HomeState.Done -> voicePlayer.playDoneAlert()
                is HomeState.Idle -> { /* do not disturb on idle transition */ }
            }
        }
    }

    override fun handleIntent(intent: CaregiverIntent) {
        when (intent) {
            is CaregiverIntent.ConfirmTaken -> {
                viewModelScope.launch {
                    confirmDoseSlotUseCase(intent.slot)
                    voicePlayer.playDoneAlert()
                    updateState { copy(doneOverlaySlot = intent.slot) }

                    doneJob?.cancel()
                    doneJob = launch {
                        delay(4500)
                        updateState { copy(doneOverlaySlot = null) }
                    }
                }
            }
            is CaregiverIntent.AcknowledgeMissed -> {
                viewModelScope.launch {
                    acknowledgeMissedSlotUseCase(intent.slot)
                }
            }
            is CaregiverIntent.ReplayVoice -> {
                when (val current = state.value.homeState) {
                    is HomeState.Due -> voicePlayer.playDueAlert(current.items)
                    is HomeState.Missed -> voicePlayer.playMissedAlert(current.contact?.name)
                    is HomeState.Idle -> {
                        val nextTime = current.nextSlot?.let { ArabicFormatters.formatMinutesOfDay(it.timeMinutes) }
                        voicePlayer.playIdleAnnouncement(nextTime)
                    }
                    is HomeState.Done -> voicePlayer.playDoneAlert()
                }
            }
            is CaregiverIntent.ScreenVisibilityChanged -> {
                isScreenVisible = intent.visible
                // Not stopping the voice when hidden: the reminder screen opening on top would
                // otherwise cut off its own alert (both share one VoicePlayer).
                autoPlayVoiceIfNeeded()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voicePlayer.stop()
    }
}
