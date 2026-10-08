package com.family.dawa.ui.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.family.dawa.di.AppContainer

class CaregiverViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaregiverViewModel::class.java)) {
            return CaregiverViewModel(
                doseRepository = container.doseRepository,
                voicePlayer = container.voicePlayer
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
