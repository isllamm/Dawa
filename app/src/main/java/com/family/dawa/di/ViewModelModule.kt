package com.family.dawa.di

import com.family.dawa.presentation.admin.contact.AdminContactViewModel
import com.family.dawa.presentation.admin.dashboard.AdminDashboardViewModel
import com.family.dawa.presentation.admin.debug.DebugViewModel
import com.family.dawa.presentation.admin.health.AdminHealthViewModel
import com.family.dawa.presentation.admin.history.AdminHistoryViewModel
import com.family.dawa.presentation.admin.medications.AdminMedListViewModel
import com.family.dawa.presentation.admin.medications.editor.AdminMedEditorViewModel
import com.family.dawa.presentation.admin.pin.AdminPinViewModel
import com.family.dawa.presentation.admin.settings.AdminSettingsViewModel
import com.family.dawa.presentation.caregiver.CaregiverViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { CaregiverViewModel(get(), get(), get(), get()) }
    viewModel { AdminPinViewModel(get()) }
    viewModel { AdminDashboardViewModel(get()) }
    viewModel { AdminMedListViewModel(get(), get()) }
    viewModel { AdminMedEditorViewModel(get(), get(), get(), get(), get()) }
    viewModel { AdminHistoryViewModel(get(), get(), get()) }
    viewModel { AdminContactViewModel(get(), get()) }
    viewModel { AdminSettingsViewModel(get(), get(), get()) }
    viewModel { AdminHealthViewModel(androidContext()) }
    viewModel { DebugViewModel(get(), get(), get(), get()) }
}
