package com.family.dawa.presentation.admin.medications

import com.family.dawa.core.base.ViewEffect
import com.family.dawa.core.base.ViewIntent
import com.family.dawa.core.base.ViewState
import com.family.dawa.domain.model.Medication

sealed interface AdminMedListIntent : ViewIntent {
    data class ToggleArchive(val medication: Medication) : AdminMedListIntent
}

data class AdminMedListState(
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = true
) : ViewState

sealed interface AdminMedListEffect : ViewEffect
