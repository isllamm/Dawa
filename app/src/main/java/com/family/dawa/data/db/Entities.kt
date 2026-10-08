package com.family.dawa.data.db

import androidx.room.*
import com.family.dawa.domain.model.*
import java.time.LocalDate

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val strength: String = "",
    val notes: String = "",
    val audioPath: String? = null,
    val colorTag: Int = 0,
    val shapeTag: String = "CIRCLE",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Medication(
        id = id,
        name = name,
        strength = strength,
        notes = notes,
        audioPath = audioPath,
        colorTag = colorTag,
        shapeTag = shapeTag,
        active = active,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(m: Medication) = MedicationEntity(
            id = m.id,
            name = m.name,
            strength = m.strength,
            notes = m.notes,
            audioPath = m.audioPath,
            colorTag = m.colorTag,
            shapeTag = m.shapeTag,
            active = m.active,
            createdAt = m.createdAt
        )
    }
}

@Entity(
    tableName = "medication_photos",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicationId"])]
)
data class MedicationPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val path: String,
    val kind: PhotoKind = PhotoKind.BOX,
    val isPrimary: Boolean = false,
    val sortOrder: Int = 0
) {
    fun toDomain() = MedicationPhoto(
        id = id,
        medicationId = medicationId,
        path = path,
        kind = kind,
        isPrimary = isPrimary,
        sortOrder = sortOrder
    )

    companion object {
        fun fromDomain(p: MedicationPhoto) = MedicationPhotoEntity(
            id = p.id,
            medicationId = p.medicationId,
            path = p.path,
            kind = p.kind,
            isPrimary = p.isPrimary,
            sortOrder = p.sortOrder
        )
    }
}

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicationId"])]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val timeOfDayMinutes: Int,
    val daysOfWeekMask: Int = 127,
    val mealRelation: MealRelation = MealRelation.NONE,
    val quantityHalves: Int = 2,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val enabled: Boolean = true
) {
    fun toDomain() = Schedule(
        id = id,
        medicationId = medicationId,
        timeOfDayMinutes = timeOfDayMinutes,
        daysOfWeekMask = daysOfWeekMask,
        mealRelation = mealRelation,
        quantityHalves = quantityHalves,
        startDate = startDate,
        endDate = endDate,
        enabled = enabled
    )

    companion object {
        fun fromDomain(s: Schedule) = ScheduleEntity(
            id = s.id,
            medicationId = s.medicationId,
            timeOfDayMinutes = s.timeOfDayMinutes,
            daysOfWeekMask = s.daysOfWeekMask,
            mealRelation = s.mealRelation,
            quantityHalves = s.quantityHalves,
            startDate = s.startDate,
            endDate = s.endDate,
            enabled = s.enabled
        )
    }
}

@Entity(
    tableName = "dose_events",
    indices = [
        Index(value = ["scheduleId", "slotDate"], unique = true),
        Index(value = ["slotDate"])
    ]
)
data class DoseEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduleId: Long,
    val medicationId: Long,
    val slotDate: LocalDate,
    val slotTimeMinutes: Int,
    val status: EventStatus,
    val recordedAt: Long = System.currentTimeMillis(),
    val recordedBy: RecordedBy = RecordedBy.CAREGIVER,
    val acknowledgedAt: Long? = null,
    val medNameSnapshot: String = "",
    val quantityHalvesSnapshot: Int = 2
) {
    fun toDomain() = DoseEvent(
        id = id,
        scheduleId = scheduleId,
        medicationId = medicationId,
        slotDate = slotDate,
        slotTimeMinutes = slotTimeMinutes,
        status = status,
        recordedAt = recordedAt,
        recordedBy = recordedBy,
        acknowledgedAt = acknowledgedAt,
        medNameSnapshot = medNameSnapshot,
        quantityHalvesSnapshot = quantityHalvesSnapshot
    )

    companion object {
        fun fromDomain(e: DoseEvent) = DoseEventEntity(
            id = e.id,
            scheduleId = e.scheduleId,
            medicationId = e.medicationId,
            slotDate = e.slotDate,
            slotTimeMinutes = e.slotTimeMinutes,
            status = e.status,
            recordedAt = e.recordedAt,
            recordedBy = e.recordedBy,
            acknowledgedAt = e.acknowledgedAt,
            medNameSnapshot = e.medNameSnapshot,
            quantityHalvesSnapshot = e.quantityHalvesSnapshot
        )
    }
}

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val photoPath: String? = null
) {
    fun toDomain() = Contact(
        id = id,
        name = name,
        phone = phone,
        photoPath = photoPath
    )

    companion object {
        fun fromDomain(c: Contact) = ContactEntity(
            id = c.id,
            name = c.name,
            phone = c.phone,
            photoPath = c.photoPath
        )
    }
}
