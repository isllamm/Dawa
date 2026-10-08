package com.family.dawa.data.repo

import com.family.dawa.data.db.DoseEventDao
import com.family.dawa.data.settings.SettingsRepository
import com.family.dawa.domain.model.AppSettings
import com.family.dawa.domain.model.HomeState
import com.family.dawa.domain.model.Medication
import com.family.dawa.domain.model.Schedule
import com.family.dawa.domain.model.SlotStatus
import com.family.dawa.domain.model.SlotWithStatus
import com.family.dawa.testutil.FixedTimeProvider
import com.family.dawa.testutil.TEST_DATE
import com.family.dawa.testutil.at
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * The home screen must change by itself as time passes (UPCOMING -> DUE -> MISSED, and a new
 * day at midnight), not only when the database changes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DoseRepositoryTest {

    private val medicationRepository: MedicationRepository = mockk()
    private val contactRepository: ContactRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val doseEventDao: DoseEventDao = mockk()
    private val clock = FixedTimeProvider(at(7, 59).plusSeconds(30))

    private val med = Medication(id = 1, name = "بانادول")
    private val schedule = Schedule(id = 11, medicationId = 1, timeOfDayMinutes = 480) // 08:00 daily

    @Before
    fun setUp() {
        every { medicationRepository.getAllActiveMedicationsFlow() } returns MutableStateFlow(listOf(med))
        coEvery { medicationRepository.getAllActiveSchedulesSync() } returns listOf(schedule)
        coEvery { medicationRepository.getAllActiveMedicationsSync() } returns mapOf(1L to med)
        coEvery { medicationRepository.getAllPhotosSync() } returns emptyMap()
        every { contactRepository.primaryContactFlow } returns MutableStateFlow(null)
        every { settingsRepository.settingsFlow } returns MutableStateFlow(AppSettings(graceMinutes = 60))
        every { doseEventDao.getEventsForDateFlow(any()) } returns flowOf(emptyList())
    }

    private fun repository() = DoseRepository(
        medicationRepository, doseEventDao, contactRepository, settingsRepository, clock, doseLedger = mockk()
    )

    private fun TestScope.collectHomeStates(): List<HomeState> {
        val states = mutableListOf<HomeState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository().observeHomeState().toList(states)
        }
        return states
    }

    @Test
    fun testObserveHomeState_becomesDueAtSlotTimeWithoutDatabaseChange() = runTest {
        val states = collectHomeStates()
        assertTrue(states.last() is HomeState.Idle)

        clock.now = at(8, 0).plusSeconds(3)
        advanceTimeBy(5_001)

        val due = states.last()
        assertTrue("expected Due but was $due", due is HomeState.Due)
        assertEquals(480, (due as HomeState.Due).slot.timeMinutes)
    }

    @Test
    fun testObserveHomeState_becomesMissedWhenGraceEnds() = runTest {
        clock.now = at(8, 30)
        val states = collectHomeStates()
        assertTrue(states.last() is HomeState.Due)

        clock.now = at(9, 0).plusSeconds(1)
        advanceTimeBy(5_001)

        assertTrue("expected Missed but was ${states.last()}", states.last() is HomeState.Missed)
    }

    @Test
    fun testObserveHomeState_rollsOverToNewDayAtMidnight() = runTest {
        clock.now = at(23, 59)
        val states = collectHomeStates()
        // Today's 08:00 dose was never given, so it shows as missed until the day ends
        assertTrue(states.last() is HomeState.Missed)

        clock.now = at(0, 0, date = TEST_DATE.plusDays(1)).plusSeconds(2)
        advanceTimeBy(5_001)

        val idle = states.last()
        assertTrue("expected Idle but was $idle", idle is HomeState.Idle)
        assertEquals(TEST_DATE.plusDays(1), (idle as HomeState.Idle).nextSlot?.date)
        verify { doseEventDao.getEventsForDateFlow(TEST_DATE.plusDays(1)) }
    }

    @Test
    fun testObserveHomeState_doesNotRepeatSameStateEveryMinute() = runTest {
        clock.now = at(6, 0)
        val states = collectHomeStates()

        clock.now = at(6, 1)
        advanceTimeBy(5_001)
        clock.now = at(6, 2)
        advanceTimeBy(5_001)

        assertEquals(1, states.size)
    }

    @Test
    fun testObserveTodayTimeline_statusFollowsTheClock() = runTest {
        val timelines = mutableListOf<List<SlotWithStatus>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository().observeTodayTimeline().toList(timelines)
        }
        assertEquals(SlotStatus.UPCOMING, timelines.last().single().status)

        clock.now = at(8, 0).plusSeconds(3)
        advanceTimeBy(5_001)

        assertEquals(SlotStatus.DUE, timelines.last().single().status)
    }
}
