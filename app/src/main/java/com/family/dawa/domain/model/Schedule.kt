package com.family.dawa.domain.model

import java.time.LocalDate

data class Schedule(
    val id: Long = 0,
    val medicationId: Long = 0,
    val timeOfDayMinutes: Int, // 0..1439 (minutes from midnight)
    val daysOfWeekMask: Int = 127, // 7-bit mask: bit0 = Mon ... bit6 = Sun. 127 = every day
    val mealRelation: MealRelation = MealRelation.NONE,
    val quantityHalves: Int = 2, // 2 = 1 full pill, 1 = half pill, 4 = 2 pills
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val enabled: Boolean = true
) {
    fun isDueOnDay(dayOfWeekValue: Int): Boolean {
        // DayOfWeek: 1 = Mon .. 7 = Sun
        val bit = 1 shl (dayOfWeekValue - 1)
        return (daysOfWeekMask and bit) != 0
    }
}
