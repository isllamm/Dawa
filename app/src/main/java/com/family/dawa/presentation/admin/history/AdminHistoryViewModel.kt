package com.family.dawa.presentation.admin.history

import androidx.lifecycle.viewModelScope
import com.family.dawa.core.base.MviViewModel
import com.family.dawa.core.time.TimeProvider
import com.family.dawa.domain.model.EventStatus
import com.family.dawa.domain.usecase.history.GetDoseHistoryUseCase
import com.family.dawa.domain.usecase.history.UpdateDoseEventStatusUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminHistoryViewModel(
    private val getDoseHistoryUseCase: GetDoseHistoryUseCase,
    private val updateDoseEventStatusUseCase: UpdateDoseEventStatusUseCase,
    private val timeProvider: TimeProvider
) : MviViewModel<AdminHistoryIntent, AdminHistoryState, AdminHistoryEffect>(AdminHistoryState()) {

    init {
        val today = timeProvider.today()
        val start = today.minusDays(14)
        updateState { copy(today = today) }

        viewModelScope.launch {
            getDoseHistoryUseCase(start, today).collectLatest { eventList ->
                updateState { copy(events = eventList, isLoading = false) }
            }
        }
    }

    override fun handleIntent(intent: AdminHistoryIntent) {
        when (intent) {
            is AdminHistoryIntent.MarkGivenLate -> {
                viewModelScope.launch {
                    updateDoseEventStatusUseCase(intent.eventId, EventStatus.COMPLETED)
                }
            }
        }
    }
}
