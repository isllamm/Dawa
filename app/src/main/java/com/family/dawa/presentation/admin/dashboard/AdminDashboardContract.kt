package com.family.dawa.presentation.admin.dashboard

import android.content.Context
import com.family.dawa.core.base.ViewEffect
import com.family.dawa.core.base.ViewIntent
import com.family.dawa.core.base.ViewState
import com.family.dawa.domain.model.SlotWithStatus

sealed interface AdminDashboardIntent : ViewIntent {
    data class CheckPermissions(val context: Context) : AdminDashboardIntent
}

data class AdminDashboardState(
    val timeline: List<SlotWithStatus> = emptyList(),
    val hasPermissionIssues: Boolean = false,
    val isLoading: Boolean = true
) : ViewState

sealed interface AdminDashboardEffect : ViewEffect
