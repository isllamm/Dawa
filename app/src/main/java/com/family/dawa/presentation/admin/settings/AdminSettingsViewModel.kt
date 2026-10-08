package com.family.dawa.presentation.admin.settings

import androidx.lifecycle.viewModelScope
import com.family.dawa.core.base.MviViewModel
import com.family.dawa.domain.usecase.debug.ReseedDemoDataUseCase
import com.family.dawa.domain.usecase.settings.GetSettingsUseCase
import com.family.dawa.domain.usecase.settings.UpdateSettingsUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminSettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val reseedDemoDataUseCase: ReseedDemoDataUseCase
) : MviViewModel<AdminSettingsIntent, AdminSettingsState, AdminSettingsEffect>(AdminSettingsState()) {

    init {
        viewModelScope.launch {
            getSettingsUseCase().collectLatest { s ->
                updateState {
                    copy(
                        settings = s,
                        patientName = s.patientName,
                        caregiverName = s.caregiverName,
                        graceMinutes = s.graceMinutes.toString(),
                        realertMinutes = s.realertMinutes.toString()
                    )
                }
            }
        }
    }

    override fun handleIntent(intent: AdminSettingsIntent) {
        when (intent) {
            is AdminSettingsIntent.PatientNameChanged -> updateState { copy(patientName = intent.name) }
            is AdminSettingsIntent.CaregiverNameChanged -> updateState { copy(caregiverName = intent.name) }
            is AdminSettingsIntent.GraceMinutesChanged -> updateState { copy(graceMinutes = intent.minutes) }
            is AdminSettingsIntent.RealertMinutesChanged -> updateState { copy(realertMinutes = intent.minutes) }
            is AdminSettingsIntent.SaveSettings -> {
                val current = state.value
                viewModelScope.launch {
                    updateSettingsUseCase(
                        patientName = current.patientName.trim(),
                        caregiverName = current.caregiverName.trim(),
                        graceMinutes = current.graceMinutes.toIntOrNull() ?: 60,
                        realertMinutes = current.realertMinutes.toIntOrNull() ?: 5,
                        voiceEnabled = current.settings.voiceEnabled,
                        vibrationEnabled = current.settings.vibrationEnabled,
                        breakfastMinutes = current.settings.breakfastMinutes,
                        lunchMinutes = current.settings.lunchMinutes,
                        dinnerMinutes = current.settings.dinnerMinutes,
                        sleepMinutes = current.settings.sleepMinutes
                    )
                    emitEffect(AdminSettingsEffect.NavigateBack)
                }
            }
            is AdminSettingsIntent.ReseedDemoData -> {
                viewModelScope.launch {
                    reseedDemoDataUseCase()
                    emitEffect(AdminSettingsEffect.NavigateBack)
                }
            }
        }
    }
}
