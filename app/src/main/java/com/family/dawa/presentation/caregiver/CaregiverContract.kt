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
    /** Sent by the screen when it starts or stops being visible. Voice alerts only play while visible. */
    data class ScreenVisibilityChanged(val visible: Boolean) : CaregiverIntent
}

data class CaregiverState(
    val homeState: HomeState = HomeState.Idle(null, emptyList()),
    val doneOverlaySlot: Slot? = null,
    /** False until the first real home state arrives, so the screen doesn't act on the placeholder. */
    val isLoaded: Boolean = false
) : ViewState

sealed interface CaregiverEffect : ViewEffect {
    data class PlayDueVoice(val items: List<DoseItem>) : CaregiverEffect
    data object PlayDoneVoice : CaregiverEffect
    data class PlayMissedVoice(val contactName: String?) : CaregiverEffect
    data class PlayIdleVoice(val nextSlotFormatted: String?) : CaregiverEffect
}
