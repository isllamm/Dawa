package com.family.dawa.presentation.navigation

object Routes {
    const val CAREGIVER = "caregiver"
    const val ADMIN_PIN = "admin_pin"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_MEDS = "admin_meds"
    const val ADMIN_MED_EDITOR = "admin_med_editor/{id}"
    const val ADMIN_HISTORY = "admin_history"
    const val ADMIN_CONTACT = "admin_contact"
    const val ADMIN_SETTINGS = "admin_settings"
    const val ADMIN_HEALTH = "admin_health"
    const val ADMIN_DEBUG = "admin_debug"

    fun medEditor(id: Long) = "admin_med_editor/$id"
}
