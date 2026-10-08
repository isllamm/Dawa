package com.family.dawa.di

import com.family.dawa.data.repo.ContactRepository
import com.family.dawa.data.repo.DoseRepository
import com.family.dawa.data.repo.MedicationRepository
import com.family.dawa.data.settings.SettingsRepository
import com.family.dawa.domain.repository.IContactRepository
import com.family.dawa.domain.repository.IDoseRepository
import com.family.dawa.domain.repository.IMedicationRepository
import com.family.dawa.domain.repository.ISettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

// Each repository is registered under its concrete type and bound to its interface,
// so both resolve to the same singleton (DataStore allows only one instance per file).
val repositoryModule = module {
    single { MedicationRepository(get(), get(), get()) } bind IMedicationRepository::class
    single { ContactRepository(get()) } bind IContactRepository::class
    single { SettingsRepository(androidContext()) } bind ISettingsRepository::class
    single { DoseRepository(get(), get(), get(), get(), get(), get()) } bind IDoseRepository::class
}
