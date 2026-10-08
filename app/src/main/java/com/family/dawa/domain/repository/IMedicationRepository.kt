package com.family.dawa.domain.repository

import com.family.dawa.domain.model.Medication
import com.family.dawa.domain.model.MedicationPhoto
import com.family.dawa.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

interface IMedicationRepository {
    fun getAllActiveMedicationsFlow(): Flow<List<Medication>>
    fun getAllMedicationsFlow(): Flow<List<Medication>>
    suspend fun getMedicationById(id: Long): Medication?
    suspend fun saveMedication(
        medication: Medication,
        photos: List<MedicationPhoto>,
        schedules: List<Schedule>
    ): Long
    suspend fun archiveMedication(id: Long)
    suspend fun deleteMedication(id: Long)
    fun getPhotosForMedicationFlow(medicationId: Long): Flow<List<MedicationPhoto>>
    suspend fun getPhotosForMedicationSync(medicationId: Long): List<MedicationPhoto>
    fun getSchedulesForMedicationFlow(medicationId: Long): Flow<List<Schedule>>
    suspend fun getSchedulesForMedicationSync(medicationId: Long): List<Schedule>
    suspend fun getAllActiveSchedulesSync(): List<Schedule>
    suspend fun getAllActiveMedicationsSync(): Map<Long, Medication>
    suspend fun getAllPhotosSync(): Map<Long, List<MedicationPhoto>>
}
