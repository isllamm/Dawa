package com.family.dawa.domain.usecase.history

import com.family.dawa.domain.model.DoseEvent
import com.family.dawa.domain.model.EventStatus
import com.family.dawa.domain.model.SlotWithStatus
import com.family.dawa.domain.repository.IDoseRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class GetTodayTimelineUseCase(
    private val doseRepository: IDoseRepository
) {
    operator fun invoke(): Flow<List<SlotWithStatus>> = doseRepository.observeTodayTimeline()
}

class GetDoseHistoryUseCase(
    private val doseRepository: IDoseRepository
) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<DoseEvent>> =
        doseRepository.getEventsBetween(startDate, endDate)
}

class UpdateDoseEventStatusUseCase(
    private val doseRepository: IDoseRepository
) {
    suspend operator fun invoke(eventId: Long, newStatus: EventStatus) {
        doseRepository.updateEventStatus(eventId, newStatus)
    }
}
