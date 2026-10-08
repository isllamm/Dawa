package com.family.dawa.presentation.admin.contact

import androidx.lifecycle.viewModelScope
import com.family.dawa.core.base.MviViewModel
import com.family.dawa.domain.model.Contact
import com.family.dawa.domain.usecase.contact.GetPrimaryContactUseCase
import com.family.dawa.domain.usecase.contact.SavePrimaryContactUseCase
import kotlinx.coroutines.launch

class AdminContactViewModel(
    private val getPrimaryContactUseCase: GetPrimaryContactUseCase,
    private val savePrimaryContactUseCase: SavePrimaryContactUseCase
) : MviViewModel<AdminContactIntent, AdminContactState, AdminContactEffect>(AdminContactState()) {

    init {
        viewModelScope.launch {
            val contact = getPrimaryContactUseCase.get()
            if (contact != null) {
                updateState { copy(name = contact.name, phone = contact.phone) }
            }
        }
    }

    override fun handleIntent(intent: AdminContactIntent) {
        when (intent) {
            is AdminContactIntent.NameChanged -> updateState { copy(name = intent.name) }
            is AdminContactIntent.PhoneChanged -> updateState { copy(phone = intent.phone) }
            is AdminContactIntent.Save -> {
                val current = state.value
                if (current.name.isBlank() || current.phone.isBlank()) return
                viewModelScope.launch {
                    savePrimaryContactUseCase(Contact(name = current.name.trim(), phone = current.phone.trim()))
                    emitEffect(AdminContactEffect.NavigateBack)
                }
            }
        }
    }
}
