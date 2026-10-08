package com.family.dawa.domain.model

import java.time.LocalDate

data class DoseItem(
    val scheduleId: Long,
    val medicationId: Long,
    val medicationName: String,
    val strength: String = "",
    val quantityHalves: Int = 2,
    val primaryPhotoPath: String? = null,
    val audioPath: String? = null,
    val mealRelation: MealRelation = MealRelation.NONE,
    val colorTag: Int = 0,
    val shapeTag: String = "CIRCLE"
)

data class Slot(
    val date: LocalDate,
    val timeMinutes: Int,
    val items: List<DoseItem>
) {
    val key: String get() = "${date}_$timeMinutes"
}

enum class SlotStatus(val arabicLabel: String) {
    UPCOMING("قادم"),
    DUE("حان وقته الآن"),
    COMPLETED("تم أخذه"),
    MISSED("فات موعده"),
    SKIPPED("تم تخطيه")
}

data class SlotWithStatus(
    val slot: Slot,
    val status: SlotStatus,
    val event: DoseEvent? = null
)

sealed interface HomeState {
    data class Idle(
        val nextSlot: Slot?,
        val todaySlots: List<SlotWithStatus>
    ) : HomeState

    data class Due(
        val slot: Slot,
        val items: List<DoseItem>,
        val todaySlots: List<SlotWithStatus>
    ) : HomeState

    data class Done(
        val slot: Slot,
        val nextSlot: Slot?
    ) : HomeState

    data class Missed(
        val slot: Slot,
        val contact: Contact?,
        val todaySlots: List<SlotWithStatus>
    ) : HomeState
}
