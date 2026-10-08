package com.family.dawa.domain.model

import java.time.LocalDate

data class DoseEvent(
    val id: Long = 0,
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
)

enum class EventStatus(val arabicLabel: String) {
    COMPLETED("تم أخذ الدواء"),
    MISSED("فات موعده"),
    SKIPPED("تم التخطي")
}

enum class RecordedBy {
    CAREGIVER,
    ADMIN,
    SYSTEM
}
