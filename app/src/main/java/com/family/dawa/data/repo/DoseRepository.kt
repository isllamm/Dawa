package com.family.dawa.data.repo

import com.family.dawa.core.time.TimeProvider
import com.family.dawa.data.db.DoseEventDao
import com.family.dawa.data.settings.SettingsRepository
import com.family.dawa.domain.engine.DoseEngine
import com.family.dawa.domain.ledger.DoseLedger
import com.family.dawa.domain.model.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate

class DoseRepository(
    private val medicationRepository: MedicationRepository,
    private val doseEventDao: DoseEventDao,
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider,
    val doseLedger: DoseLedger
) {

    /**
     * Observes today's caregiver HomeState reactively whenever schedules, events, or settings change.
     */
    fun observeHomeState(): Flow<HomeState> {
        return combine(
            medicationRepository.getAllActiveMedicationsFlow(),
            contactRepository.primaryContactFlow,
            settingsRepository.settingsFlow
        ) { meds, contact, settings ->
            Triple(meds, contact, settings)
        }.flatMapLatest { (meds, contact, settings) ->
            val now = timeProvider.nowZoned()
            val today = now.toLocalDate()
            val yesterday = today.minusDays(1)
            val tomorrow = today.plusDays(1)

            combine(
                doseEventDao.getEventsForDateFlow(today),
                doseEventDao.getEventsForDateFlow(yesterday)
            ) { todayEvents, yesterdayEvents ->
                val medsMap = meds.associateBy { it.id }
                val schedules = medicationRepository.getAllActiveSchedulesSync()
                val photos = medicationRepository.getAllPhotosSync()

                val todaySlots = DoseEngine.generateSlotsForDate(today, schedules, medsMap, photos)
                val yesterdaySlots = DoseEngine.generateSlotsForDate(yesterday, schedules, medsMap, photos)
                val tomorrowSlots = DoseEngine.generateSlotsForDate(tomorrow, schedules, medsMap, photos)

                val todayEventsDomain = todayEvents.map { it.toDomain() }
                val yesterdayEventsDomain = yesterdayEvents.map { it.toDomain() }

                val todaySlotsWithStatus = todaySlots.map { slot ->
                    val (status, event) = DoseEngine.deriveSlotStatus(slot, todayEventsDomain, now, settings.graceMinutes)
                    SlotWithStatus(slot, status, event)
                }

                val yesterdaySlotsWithStatus = yesterdaySlots.map { slot ->
                    val (status, event) = DoseEngine.deriveSlotStatus(slot, yesterdayEventsDomain, now, settings.graceMinutes)
                    SlotWithStatus(slot, status, event)
                }

                val acknowledgedSlotKeys = todayEventsDomain
                    .filter { it.acknowledgedAt != null }
                    .map { "${it.slotDate}_${it.slotTimeMinutes}" }
                    .toSet()

                DoseEngine.resolveHomeState(
                    todaySlotsWithStatus = todaySlotsWithStatus,
                    yesterdaySlotsWithStatus = yesterdaySlotsWithStatus,
                    tomorrowSlots = tomorrowSlots,
                    contact = contact,
                    acknowledgedSlotKeys = acknowledgedSlotKeys
                )
            }
        }
    }

    /**
     * Observes the full day timeline (used in Admin dashboard).
     */
    fun observeTodayTimeline(): Flow<List<SlotWithStatus>> {
        return settingsRepository.settingsFlow.flatMapLatest { settings ->
            val now = timeProvider.nowZoned()
            val today = now.toLocalDate()

            doseEventDao.getEventsForDateFlow(today).map { eventsEntities ->
                val events = eventsEntities.map { it.toDomain() }
                val schedules = medicationRepository.getAllActiveSchedulesSync()
                val medsMap = medicationRepository.getAllActiveMedicationsSync()
                val photos = medicationRepository.getAllPhotosSync()

                val slots = DoseEngine.generateSlotsForDate(today, schedules, medsMap, photos)
                slots.map { slot ->
                    val (status, event) = DoseEngine.deriveSlotStatus(slot, events, now, settings.graceMinutes)
                    SlotWithStatus(slot, status, event)
                }
            }
        }
    }

    suspend fun confirmSlot(slot: Slot) {
        doseLedger.confirmSlot(slot, timeProvider.nowZoned(), RecordedBy.CAREGIVER)
    }

    suspend fun acknowledgeMissed(slot: Slot) {
        doseLedger.acknowledgeMissed(slot, timeProvider.nowZoned())
    }

    suspend fun reconcile() {
        val settings = settingsRepository.getSettings()
        doseLedger.reconcile(timeProvider.nowZoned(), settings.graceMinutes)
    }

    suspend fun resetTodayEvents() {
        doseLedger.resetTodayEvents(timeProvider.today())
    }

    fun getEventsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<DoseEvent>> {
        return doseEventDao.getEventsBetweenFlow(startDate, endDate).map { list ->
            list.map { it.toDomain() }
        }
    }
}
