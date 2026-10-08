package com.family.dawa.data.repo

import com.family.dawa.data.db.*
import com.family.dawa.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val photoDao: MedicationPhotoDao,
    private val scheduleDao: ScheduleDao
) {

    fun getAllActiveMedicationsFlow(): Flow<List<Medication>> =
        medicationDao.getAllActiveFlow().map { list -> list.map { it.toDomain() } }

    fun getAllMedicationsFlow(): Flow<List<Medication>> =
        medicationDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    suspend fun getMedicationById(id: Long): Medication? =
        medicationDao.getById(id)?.toDomain()

    suspend fun saveMedication(
        medication: Medication,
        photos: List<MedicationPhoto>,
        schedules: List<Schedule>
    ): Long {
        val entity = MedicationEntity.fromDomain(medication)
        val medId = if (medication.id == 0L) {
            medicationDao.insert(entity)
        } else {
            medicationDao.update(entity)
            medication.id
        }

        // Replace photos
        photoDao.deleteAllForMedication(medId)
        val photoEntities = photos.map {
            MedicationPhotoEntity.fromDomain(it.copy(medicationId = medId))
        }
        photoDao.insertAll(photoEntities)

        // Replace schedules
        scheduleDao.deleteAllForMedication(medId)
        val scheduleEntities = schedules.map {
            ScheduleEntity.fromDomain(it.copy(medicationId = medId))
        }
        scheduleDao.insertAll(scheduleEntities)

        return medId
    }

    suspend fun archiveMedication(id: Long) {
        medicationDao.archive(id)
    }

    suspend fun deleteMedication(id: Long) {
        photoDao.deleteAllForMedication(id)
        scheduleDao.deleteAllForMedication(id)
        medicationDao.delete(id)
    }

    fun getPhotosForMedicationFlow(medicationId: Long): Flow<List<MedicationPhoto>> =
        photoDao.getPhotosForMedicationFlow(medicationId).map { list -> list.map { it.toDomain() } }

    suspend fun getPhotosForMedicationSync(medicationId: Long): List<MedicationPhoto> =
        photoDao.getPhotosForMedicationSync(medicationId).map { it.toDomain() }

    fun getSchedulesForMedicationFlow(medicationId: Long): Flow<List<Schedule>> =
        scheduleDao.getSchedulesForMedicationFlow(medicationId).map { list -> list.map { it.toDomain() } }

    suspend fun getSchedulesForMedicationSync(medicationId: Long): List<Schedule> =
        scheduleDao.getSchedulesForMedicationSync(medicationId).map { it.toDomain() }

    suspend fun getAllActiveSchedulesSync(): List<Schedule> =
        scheduleDao.getAllActiveSync().map { it.toDomain() }

    suspend fun getAllActiveMedicationsSync(): Map<Long, Medication> =
        medicationDao.getAllActiveSync().associate { it.id to it.toDomain() }

    suspend fun getAllPhotosSync(): Map<Long, List<MedicationPhoto>> =
        photoDao.getAllPhotosSync().groupBy { it.medicationId }
            .mapValues { entry -> entry.value.map { it.toDomain() } }
}
