package com.family.dawa.presentation.admin.medications

import androidx.lifecycle.viewModelScope
import com.family.dawa.core.base.MviViewModel
import com.family.dawa.domain.usecase.medications.ArchiveMedicationUseCase
import com.family.dawa.domain.usecase.medications.GetMedicationsUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminMedListViewModel(
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val archiveMedicationUseCase: ArchiveMedicationUseCase
) : MviViewModel<AdminMedListIntent, AdminMedListState, AdminMedListEffect>(AdminMedListState()) {

    init {
        viewModelScope.launch {
            getMedicationsUseCase.getAll().collectLatest { meds ->
                updateState { copy(medications = meds, isLoading = false) }
            }
        }
    }

    override fun handleIntent(intent: AdminMedListIntent) {
        when (intent) {
            is AdminMedListIntent.ToggleArchive -> {
                viewModelScope.launch {
                    archiveMedicationUseCase(
                        id = intent.medication.id,
                        activate = !intent.medication.active
                    )
                }
            }
        }
    }
}
