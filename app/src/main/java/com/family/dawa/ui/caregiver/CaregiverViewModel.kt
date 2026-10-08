package com.family.dawa.ui.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.dawa.core.audio.VoicePlayer
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.data.repo.DoseRepository
import com.family.dawa.domain.model.DoseItem
import com.family.dawa.domain.model.HomeState
import com.family.dawa.domain.model.Slot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CaregiverViewModel(
    private val doseRepository: DoseRepository,
    private val voicePlayer: VoicePlayer
) : ViewModel() {

    private val _isDoneOverlay = MutableStateFlow<Slot?>(null)
    val isDoneOverlay: StateFlow<Slot?> = _isDoneOverlay.asStateFlow()

    val homeState: StateFlow<HomeState> = doseRepository.observeHomeState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState.Idle(null, emptyList()))

    private var lastPlayedStateKey: String? = null
    private var doneJob: Job? = null

    init {
        // Automatically play voice when state changes (e.g. becomes DUE or MISSED)
        viewModelScope.launch {
            homeState.collectLatest { state ->
                val stateKey = when (state) {
                    is HomeState.Due -> "due_${state.slot.key}"
                    is HomeState.Missed -> "missed_${state.slot.key}"
                    is HomeState.Idle -> "idle_${state.nextSlot?.key}"
                    is HomeState.Done -> "done_${state.slot.key}"
                }

                if (stateKey != lastPlayedStateKey) {
                    lastPlayedStateKey = stateKey
                    when (state) {
                        is HomeState.Due -> voicePlayer.playDueAlert(state.items)
                        is HomeState.Missed -> voicePlayer.playMissedAlert(state.contact?.name)
                        is HomeState.Idle -> {
                            // Only play idle on manual request, don't spam
                        }
                        is HomeState.Done -> voicePlayer.playDoneAlert()
                    }
                }
            }
        }
    }

    fun onConfirmTaken(slot: Slot) {
        viewModelScope.launch {
            doseRepository.confirmSlot(slot)
            voicePlayer.playDoneAlert()
            _isDoneOverlay.value = slot

            doneJob?.cancel()
            doneJob = launch {
                delay(4500) // Show DONE for 4.5 seconds
                _isDoneOverlay.value = null
            }
        }
    }

    fun onAcknowledgeMissed(slot: Slot) {
        viewModelScope.launch {
            doseRepository.acknowledgeMissed(slot)
        }
    }

    fun replayVoice() {
        when (val state = homeState.value) {
            is HomeState.Due -> voicePlayer.playDueAlert(state.items)
            is HomeState.Missed -> voicePlayer.playMissedAlert(state.contact?.name)
            is HomeState.Idle -> {
                val nextTime = state.nextSlot?.let { ArabicFormatters.formatMinutesOfDay(it.timeMinutes) }
                voicePlayer.playIdleAnnouncement(nextTime)
            }
            is HomeState.Done -> voicePlayer.playDoneAlert()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voicePlayer.stop()
    }
}
