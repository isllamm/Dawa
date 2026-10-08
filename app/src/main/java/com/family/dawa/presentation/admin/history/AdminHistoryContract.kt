package com.family.dawa.presentation.admin.history

import com.family.dawa.core.base.ViewEffect
import com.family.dawa.core.base.ViewIntent
import com.family.dawa.core.base.ViewState
import com.family.dawa.domain.model.DoseEvent
import java.time.LocalDate

sealed interface AdminHistoryIntent : ViewIntent {
    data class MarkGivenLate(val eventId: Long) : AdminHistoryIntent
}

data class AdminHistoryState(
    val events: List<DoseEvent> = emptyList(),
    val today: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true
) : ViewState

sealed interface AdminHistoryEffect : ViewEffect
