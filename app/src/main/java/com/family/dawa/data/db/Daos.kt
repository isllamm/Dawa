package com.family.dawa.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Query("UPDATE medications SET active = 0 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Long): MedicationEntity?

    @Query("SELECT * FROM medications WHERE active = 1 ORDER BY name ASC")
    fun getAllActiveFlow(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY active DESC, name ASC")
    fun getAllFlow(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE active = 1")
    suspend fun getAllActiveSync(): List<MedicationEntity>

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface MedicationPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: MedicationPhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<MedicationPhotoEntity>)

    @Query("SELECT * FROM medication_photos WHERE medicationId = :medicationId ORDER BY isPrimary DESC, sortOrder ASC")
    fun getPhotosForMedicationFlow(medicationId: Long): Flow<List<MedicationPhotoEntity>>

    @Query("SELECT * FROM medication_photos WHERE medicationId = :medicationId ORDER BY isPrimary DESC, sortOrder ASC")
    suspend fun getPhotosForMedicationSync(medicationId: Long): List<MedicationPhotoEntity>

    @Query("SELECT * FROM medication_photos")
    suspend fun getAllPhotosSync(): List<MedicationPhotoEntity>

    @Query("SELECT * FROM medication_photos")
    fun getAllPhotosFlow(): Flow<List<MedicationPhotoEntity>>

    @Query("DELETE FROM medication_photos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM medication_photos WHERE medicationId = :medicationId")
    suspend fun deleteAllForMedication(medicationId: Long)
}

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<ScheduleEntity>)

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Query("SELECT * FROM schedules WHERE medicationId = :medicationId")
    fun getSchedulesForMedicationFlow(medicationId: Long): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE medicationId = :medicationId")
    suspend fun getSchedulesForMedicationSync(medicationId: Long): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE enabled = 1")
    fun getAllActiveFlow(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE enabled = 1")
    suspend fun getAllActiveSync(): List<ScheduleEntity>

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM schedules WHERE medicationId = :medicationId")
    suspend fun deleteAllForMedication(medicationId: Long)
}

@Dao
interface DoseEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(event: DoseEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(event: DoseEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllOrIgnore(events: List<DoseEventEntity>)

    @Update
    suspend fun update(event: DoseEventEntity)

    @Query("SELECT * FROM dose_events WHERE slotDate = :date")
    fun getEventsForDateFlow(date: LocalDate): Flow<List<DoseEventEntity>>

    @Query("SELECT * FROM dose_events WHERE slotDate = :date")
    suspend fun getEventsForDateSync(date: LocalDate): List<DoseEventEntity>

    @Query("SELECT * FROM dose_events WHERE slotDate BETWEEN :startDate AND :endDate ORDER BY slotDate DESC, slotTimeMinutes DESC")
    fun getEventsBetweenFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<DoseEventEntity>>

    @Query("SELECT * FROM dose_events WHERE slotDate BETWEEN :startDate AND :endDate ORDER BY slotDate DESC, slotTimeMinutes DESC")
    suspend fun getEventsBetweenSync(startDate: LocalDate, endDate: LocalDate): List<DoseEventEntity>

    @Query("UPDATE dose_events SET acknowledgedAt = :acknowledgedAt WHERE slotDate = :slotDate AND slotTimeMinutes = :slotTimeMinutes")
    suspend fun acknowledgeSlot(slotDate: LocalDate, slotTimeMinutes: Int, acknowledgedAt: Long)

    @Query("DELETE FROM dose_events WHERE slotDate = :date")
    suspend fun deleteEventsForDate(date: LocalDate)
}

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity): Long

    @Update
    suspend fun update(contact: ContactEntity)

    @Query("SELECT * FROM contacts LIMIT 1")
    fun getPrimaryContactFlow(): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts LIMIT 1")
    suspend fun getPrimaryContactSync(): ContactEntity?

    @Query("SELECT * FROM contacts")
    fun getAllContactsFlow(): Flow<List<ContactEntity>>
}
