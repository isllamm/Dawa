package com.family.dawa.presentation.admin.health

import android.content.Context
import com.family.dawa.core.base.MviViewModel
import com.family.dawa.core.permissions.PermissionChecker

class AdminHealthViewModel(
    context: Context
) : MviViewModel<AdminHealthIntent, AdminHealthState, AdminHealthEffect>(
    AdminHealthState(items = PermissionChecker.checkHealth(context))
) {

    override fun handleIntent(intent: AdminHealthIntent) {
        when (intent) {
            is AdminHealthIntent.Refresh -> {
                updateState { copy(items = PermissionChecker.checkHealth(intent.context)) }
            }
        }
    }
}
