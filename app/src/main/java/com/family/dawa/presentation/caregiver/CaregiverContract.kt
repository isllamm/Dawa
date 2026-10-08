package com.family.dawa.presentation.caregiver

import com.family.dawa.core.base.ViewEffect
import com.family.dawa.core.base.ViewIntent
import com.family.dawa.core.base.ViewState
import com.family.dawa.domain.model.DoseItem
import com.family.dawa.domain.model.HomeState
import com.family.dawa.domain.model.Slot

sealed interface CaregiverIntent : ViewIntent {
    data class ConfirmTaken(val slot: Slot) : CaregiverIntent
    data class AcknowledgeMissed(val slot: Slot) : CaregiverIntent
    data object ReplayVoice : CaregiverIntent
}

data class CaregiverState(
    val homeState: HomeState = HomeState.Idle(null, emptyList()),
    val doneOverlaySlot: Slot? = null
) : ViewState

sealed interface CaregiverEffect : ViewEffect {
    data class PlayDueVoice(val items: List<DoseItem>) : CaregiverEffect
    data object PlayDoneVoice : CaregiverEffect
    data class PlayMissedVoice(val contactName: String?) : CaregiverEffect
    data class PlayIdleVoice(val nextSlotFormatted: String?) : CaregiverEffect
}
