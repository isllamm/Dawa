package com.family.dawa.di

import com.family.dawa.domain.usecase.caregiver.*
import com.family.dawa.domain.usecase.contact.*
import com.family.dawa.domain.usecase.debug.*
import com.family.dawa.domain.usecase.history.*
import com.family.dawa.domain.usecase.medications.*
import com.family.dawa.domain.usecase.settings.*
import org.koin.dsl.module

val useCaseModule = module {
    // Caregiver
    factory { GetCaregiverHomeStateUseCase(get()) }
    factory { ConfirmDoseSlotUseCase(get(), get()) }
    factory { AcknowledgeMissedSlotUseCase(get(), get()) }

    // Medications
    factory { GetMedicationsUseCase(get()) }
    factory { GetMedicationByIdUseCase(get()) }
    factory { SaveMedicationUseCase(get(), get()) }
    factory { ArchiveMedicationUseCase(get(), get()) }

    // History & Timeline
    factory { GetTodayTimelineUseCase(get()) }
    factory { GetDoseHistoryUseCase(get()) }
    factory { UpdateDoseEventStatusUseCase(get()) }

    // Settings & PIN
    factory { GetSettingsUseCase(get()) }
    factory { UpdateSettingsUseCase(get(), get()) }
    factory { VerifyAdminPinUseCase(get()) }
    factory { UpdateAdminPinUseCase(get()) }

    // Contact
    factory { GetPrimaryContactUseCase(get()) }
    factory { SavePrimaryContactUseCase(get()) }

    // Debug
    factory { SetDebugTimeOffsetUseCase(get(), get(), get()) }
    factory { ResetTodayEventsUseCase(get(), get()) }
    factory { ReseedDemoDataUseCase(get(), get()) }
}
