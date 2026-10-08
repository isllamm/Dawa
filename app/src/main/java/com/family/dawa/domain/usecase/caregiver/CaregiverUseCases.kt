package com.family.dawa.domain.usecase.caregiver

import com.family.dawa.domain.model.HomeState
import com.family.dawa.domain.model.Slot
import com.family.dawa.domain.repository.IDoseRepository
import com.family.dawa.domain.scheduler.IAlarmScheduler
import kotlinx.coroutines.flow.Flow

class GetCaregiverHomeStateUseCase(
    private val doseRepository: IDoseRepository
) {
    operator fun invoke(): Flow<HomeState> = doseRepository.observeHomeState()
}

class ConfirmDoseSlotUseCase(
    private val doseRepository: IDoseRepository,
    private val alarmScheduler: IAlarmScheduler
) {
    suspend operator fun invoke(slot: Slot) {
        doseRepository.confirmSlot(slot)
        alarmScheduler.resync()
    }
}

class AcknowledgeMissedSlotUseCase(
    private val doseRepository: IDoseRepository,
    private val alarmScheduler: IAlarmScheduler
) {
    suspend operator fun invoke(slot: Slot) {
        doseRepository.acknowledgeMissed(slot)
        alarmScheduler.resync()
    }
}
