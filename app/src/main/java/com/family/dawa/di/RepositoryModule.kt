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
import org.koin.dsl.module

val repositoryModule = module {
    single<IMedicationRepository> { MedicationRepository(get(), get(), get()) }
    single<IContactRepository> { ContactRepository(get()) }
    single<ISettingsRepository> { SettingsRepository(androidContext()) }
    single<IDoseRepository> { DoseRepository(get(), get(), get(), get(), get(), get()) }
}
