package com.family.dawa.presentation.admin.settings

import com.family.dawa.core.base.ViewEffect
import com.family.dawa.core.base.ViewIntent
import com.family.dawa.core.base.ViewState
import com.family.dawa.domain.model.AppSettings

sealed interface AdminSettingsIntent : ViewIntent {
    data class PatientNameChanged(val name: String) : AdminSettingsIntent
    data class CaregiverNameChanged(val name: String) : AdminSettingsIntent
    data class GraceMinutesChanged(val minutes: String) : AdminSettingsIntent
    data class RealertMinutesChanged(val minutes: String) : AdminSettingsIntent
    data object SaveSettings : AdminSettingsIntent
    data object ReseedDemoData : AdminSettingsIntent
}

data class AdminSettingsState(
    val settings: AppSettings = AppSettings(),
    val patientName: String = "جدو",
    val caregiverName: String = "تيتا",
    val graceMinutes: String = "60",
    val realertMinutes: String = "5"
) : ViewState

sealed interface AdminSettingsEffect : ViewEffect {
    data object NavigateBack : AdminSettingsEffect
}
