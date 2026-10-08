package com.family.dawa.presentation.admin.debug

import com.family.dawa.core.base.ViewEffect
import com.family.dawa.core.base.ViewIntent
import com.family.dawa.core.base.ViewState
import java.time.ZonedDateTime

sealed interface DebugIntent : ViewIntent {
    data class FastForward(val addedMinutes: Long) : DebugIntent
    data object ResetOffset : DebugIntent
    data object TriggerTenSecondAlarm : DebugIntent
    data object ResetToday : DebugIntent
}

data class DebugState(
    val currentOffsetMs: Long = 0L,
    val simulatedNow: ZonedDateTime = ZonedDateTime.now(),
    val statusMessage: String = ""
) : ViewState

sealed interface DebugEffect : ViewEffect
