package com.family.dawa.presentation.admin.dashboard

import androidx.lifecycle.viewModelScope
import com.family.dawa.core.base.MviViewModel
import com.family.dawa.core.permissions.PermissionChecker
import com.family.dawa.domain.usecase.history.GetTodayTimelineUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val getTodayTimelineUseCase: GetTodayTimelineUseCase
) : MviViewModel<AdminDashboardIntent, AdminDashboardState, AdminDashboardEffect>(AdminDashboardState()) {

    init {
        viewModelScope.launch {
            getTodayTimelineUseCase().collectLatest { timelineList ->
                updateState { copy(timeline = timelineList, isLoading = false) }
            }
        }
    }

    override fun handleIntent(intent: AdminDashboardIntent) {
        when (intent) {
            is AdminDashboardIntent.CheckPermissions -> {
                val issues = PermissionChecker.checkHealth(intent.context).any { !it.isOk }
                updateState { copy(hasPermissionIssues = issues) }
            }
        }
    }
}
