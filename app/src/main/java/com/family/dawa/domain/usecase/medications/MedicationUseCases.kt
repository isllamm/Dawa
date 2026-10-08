package com.family.dawa.domain.usecase.medications

import com.family.dawa.domain.model.Medication
import com.family.dawa.domain.model.MedicationPhoto
import com.family.dawa.domain.model.Schedule
import com.family.dawa.domain.repository.IMedicationRepository
import com.family.dawa.domain.scheduler.IAlarmScheduler
import kotlinx.coroutines.flow.Flow

class GetMedicationsUseCase(
    private val medicationRepository: IMedicationRepository
) {
    fun getAll(): Flow<List<Medication>> = medicationRepository.getAllMedicationsFlow()
    fun getActive(): Flow<List<Medication>> = medicationRepository.getAllActiveMedicationsFlow()
}

class GetMedicationByIdUseCase(
    private val medicationRepository: IMedicationRepository
) {
    suspend operator fun invoke(id: Long): Medication? = medicationRepository.getMedicationById(id)
    suspend fun getPhotos(id: Long): List<MedicationPhoto> = medicationRepository.getPhotosForMedicationSync(id)
    suspend fun getSchedules(id: Long): List<Schedule> = medicationRepository.getSchedulesForMedicationSync(id)
}

class SaveMedicationUseCase(
    private val medicationRepository: IMedicationRepository,
    private val alarmScheduler: IAlarmScheduler
) {
    suspend operator fun invoke(
        medication: Medication,
        photos: List<MedicationPhoto>,
        schedules: List<Schedule>
    ): Long {
        val id = medicationRepository.saveMedication(medication, photos, schedules)
        alarmScheduler.resync()
        return id
    }
}

class ArchiveMedicationUseCase(
    private val medicationRepository: IMedicationRepository,
    private val alarmScheduler: IAlarmScheduler
) {
    suspend operator fun invoke(id: Long, activate: Boolean = false) {
        if (!activate) {
            medicationRepository.archiveMedication(id)
        } else {
            val med = medicationRepository.getMedicationById(id) ?: return
            val photos = medicationRepository.getPhotosForMedicationSync(id)
            val schedules = medicationRepository.getSchedulesForMedicationSync(id)
            medicationRepository.saveMedication(med.copy(active = true), photos, schedules)
        }
        alarmScheduler.resync()
    }
}
