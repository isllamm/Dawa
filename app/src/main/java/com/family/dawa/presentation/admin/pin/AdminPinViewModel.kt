package com.family.dawa.presentation.admin.pin

import androidx.lifecycle.viewModelScope
import com.family.dawa.core.base.MviViewModel
import com.family.dawa.domain.usecase.settings.VerifyAdminPinUseCase
import kotlinx.coroutines.launch

class AdminPinViewModel(
    private val verifyAdminPinUseCase: VerifyAdminPinUseCase
) : MviViewModel<AdminPinIntent, AdminPinState, AdminPinEffect>(AdminPinState()) {

    override fun handleIntent(intent: AdminPinIntent) {
        when (intent) {
            is AdminPinIntent.DigitEntered -> {
                val current = state.value.enteredPin
                if (current.length < 4) {
                    val next = current + intent.digit
                    updateState { copy(enteredPin = next, isError = false) }

                    if (next.length == 4) {
                        verifyPin(next)
                    }
                }
            }
            is AdminPinIntent.Backspace -> {
                val current = state.value.enteredPin
                if (current.isNotEmpty()) {
                    updateState { copy(enteredPin = current.dropLast(1), isError = false) }
                }
            }
            is AdminPinIntent.Clear -> {
                updateState { copy(enteredPin = "", isError = false) }
            }
        }
    }

    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            val isValid = verifyAdminPinUseCase(pin)
            if (isValid) {
                emitEffect(AdminPinEffect.NavigateToDashboard)
            } else {
                updateState { copy(isError = true, enteredPin = "", isLoading = false) }
            }
        }
    }
}
