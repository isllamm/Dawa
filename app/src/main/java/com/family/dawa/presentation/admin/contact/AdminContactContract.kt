package com.family.dawa.presentation.admin.contact

import com.family.dawa.core.base.ViewEffect
import com.family.dawa.core.base.ViewIntent
import com.family.dawa.core.base.ViewState

sealed interface AdminContactIntent : ViewIntent {
    data class NameChanged(val name: String) : AdminContactIntent
    data class PhoneChanged(val phone: String) : AdminContactIntent
    data object Save : AdminContactIntent
}

data class AdminContactState(
    val name: String = "",
    val phone: String = "",
    val isLoading: Boolean = false
) : ViewState

sealed interface AdminContactEffect : ViewEffect {
    data object NavigateBack : AdminContactEffect
}
