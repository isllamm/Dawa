package com.family.dawa.di

import com.family.dawa.data.db.DawaDatabase
import com.family.dawa.domain.ledger.DoseLedger
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { DawaDatabase.getInstance(androidContext()) }
    single { get<DawaDatabase>().medicationDao() }
    single { get<DawaDatabase>().medicationPhotoDao() }
    single { get<DawaDatabase>().scheduleDao() }
    single { get<DawaDatabase>().doseEventDao() }
    single { get<DawaDatabase>().contactDao() }
    single { DoseLedger(get(), get()) }
}
