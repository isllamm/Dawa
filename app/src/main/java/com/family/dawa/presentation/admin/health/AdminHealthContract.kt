package com.family.dawa.presentation.admin.health

import android.content.Context
import com.family.dawa.core.base.ViewEffect
import com.family.dawa.core.base.ViewIntent
import com.family.dawa.core.base.ViewState
import com.family.dawa.core.permissions.HealthCheckItem

sealed interface AdminHealthIntent : ViewIntent {
    data class Refresh(val context: Context) : AdminHealthIntent
}

data class AdminHealthState(
    val items: List<HealthCheckItem> = emptyList()
) : ViewState

sealed interface AdminHealthEffect : ViewEffect
