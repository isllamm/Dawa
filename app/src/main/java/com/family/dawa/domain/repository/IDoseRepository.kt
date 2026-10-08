package com.family.dawa.domain.repository

import com.family.dawa.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface IDoseRepository {
    fun observeHomeState(): Flow<HomeState>
    fun observeTodayTimeline(): Flow<List<SlotWithStatus>>
    suspend fun confirmSlot(slot: Slot)
    suspend fun acknowledgeMissed(slot: Slot)
    suspend fun reconcile()
    suspend fun resetTodayEvents()
    fun getEventsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<DoseEvent>>
    suspend fun updateEventStatus(eventId: Long, newStatus: EventStatus)
}
