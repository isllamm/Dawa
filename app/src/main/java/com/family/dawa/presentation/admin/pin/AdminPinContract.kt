package com.family.dawa.presentation.admin.pin

import com.family.dawa.core.base.ViewEffect
import com.family.dawa.core.base.ViewIntent
import com.family.dawa.core.base.ViewState

sealed interface AdminPinIntent : ViewIntent {
    data class DigitEntered(val digit: String) : AdminPinIntent
    data object Backspace : AdminPinIntent
    data object Clear : AdminPinIntent
}

data class AdminPinState(
    val enteredPin: String = "",
    val isError: Boolean = false,
    val isLoading: Boolean = false
) : ViewState

sealed interface AdminPinEffect : ViewEffect {
    data object NavigateToDashboard : AdminPinEffect
}
