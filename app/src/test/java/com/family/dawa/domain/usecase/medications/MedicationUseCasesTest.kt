package com.family.dawa.domain.usecase.medications

import app.cash.turbine.test
import com.family.dawa.domain.model.Medication
import com.family.dawa.domain.model.MedicationPhoto
import com.family.dawa.domain.model.Schedule
import com.family.dawa.domain.repository.IMedicationRepository
import com.family.dawa.domain.scheduler.IAlarmScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationUseCasesTest {

    private val medicationRepository: IMedicationRepository = mockk(relaxed = true)
    private val alarmScheduler: IAlarmScheduler = mockk(relaxed = true)

    private val active = Medication(id = 1, name = "بانادول", active = true)
    private val archived = Medication(id = 2, name = "قديم", active = false)
    private val photos = listOf(MedicationPhoto(id = 1, medicationId = 2, path = "/p.jpg", isPrimary = true))
    private val schedules = listOf(Schedule(id = 1, medicationId = 2, timeOfDayMinutes = 480))

    @Test
    fun testGetMedications_getAllAndGetActiveUseMatchingRepositoryFlows() = runTest {
        every { medicationRepository.getAllMedicationsFlow() } returns flowOf(listOf(active, archived))
        every { medicationRepository.getAllActiveMedicationsFlow() } returns flowOf(listOf(active))
        val useCase = GetMedicationsUseCase(medicationRepository)

        useCase.getAll().test {
            assertEquals(listOf(active, archived), awaitItem())
            awaitComplete()
        }
        useCase.getActive().test {
            assertEquals(listOf(active), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testGetMedicationById_returnsMedicationPhotosAndSchedules() = runTest {
        coEvery { medicationRepository.getMedicationById(2) } returns archived
        coEvery { medicationRepository.getPhotosForMedicationSync(2) } returns photos
        coEvery { medicationRepository.getSchedulesForMedicationSync(2) } returns schedules
        val useCase = GetMedicationByIdUseCase(medicationRepository)

        assertEquals(archived, useCase(2))
        assertEquals(photos, useCase.getPhotos(2))
        assertEquals(schedules, useCase.getSchedules(2))
    }

    @Test
    fun testSaveMedication_savesResyncsAndReturnsId() = runTest {
        coEvery { medicationRepository.saveMedication(active, photos, schedules) } returns 42L

        val id = SaveMedicationUseCase(medicationRepository, alarmScheduler)(active, photos, schedules)

        assertEquals(42L, id)
        coVerifyOrder {
            medicationRepository.saveMedication(active, photos, schedules)
            alarmScheduler.resync()
        }
    }

    @Test
    fun testArchiveMedication_archivesThenResyncs() = runTest {
        ArchiveMedicationUseCase(medicationRepository, alarmScheduler)(id = 1)

        coVerifyOrder {
            medicationRepository.archiveMedication(1)
            alarmScheduler.resync()
        }
    }

    @Test
    fun testArchiveMedication_activateResavesAsActiveWithSamePhotosAndSchedules() = runTest {
        coEvery { medicationRepository.getMedicationById(2) } returns archived
        coEvery { medicationRepository.getPhotosForMedicationSync(2) } returns photos
        coEvery { medicationRepository.getSchedulesForMedicationSync(2) } returns schedules

        ArchiveMedicationUseCase(medicationRepository, alarmScheduler)(id = 2, activate = true)

        coVerifyOrder {
            medicationRepository.saveMedication(archived.copy(active = true), photos, schedules)
            alarmScheduler.resync()
        }
        coVerify(exactly = 0) { medicationRepository.archiveMedication(any()) }
    }

    @Test
    fun testArchiveMedication_activateUnknownMedicationDoesNothing() = runTest {
        coEvery { medicationRepository.getMedicationById(99) } returns null

        ArchiveMedicationUseCase(medicationRepository, alarmScheduler)(id = 99, activate = true)

        coVerify(exactly = 0) { medicationRepository.saveMedication(any(), any(), any()) }
        coVerify(exactly = 0) { alarmScheduler.resync() }
    }
}
