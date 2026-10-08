package com.family.dawa.testutil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.family.dawa.core.time.TimeProvider
import com.family.dawa.domain.model.DoseItem
import com.family.dawa.domain.model.Slot
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

val TEST_ZONE: ZoneId = ZoneId.of("Africa/Cairo")
val TEST_DATE: LocalDate = LocalDate.of(2026, 10, 8) // Thursday

/** Returns [date] at [hour]:[minute] in [TEST_ZONE]. */
fun at(hour: Int, minute: Int = 0, date: LocalDate = TEST_DATE): ZonedDateTime =
    date.atTime(hour, minute).atZone(TEST_ZONE)

class FixedTimeProvider(var now: ZonedDateTime = at(9)) : TimeProvider {
    override fun nowZoned(): ZonedDateTime = now
}

fun doseItem(
    scheduleId: Long = 1L,
    medicationId: Long = 1L,
    name: String = "بانادول",
    quantityHalves: Int = 2
) = DoseItem(
    scheduleId = scheduleId,
    medicationId = medicationId,
    medicationName = name,
    quantityHalves = quantityHalves
)

fun slot(
    timeMinutes: Int = 480,
    date: LocalDate = TEST_DATE,
    items: List<DoseItem> = listOf(doseItem())
) = Slot(date = date, timeMinutes = timeMinutes, items = items)

/** Clears the ViewModel the same way the framework does, so onCleared() runs. */
fun <T : ViewModel> T.clearForTest() {
    val viewModel = this
    val store = ViewModelStore()
    ViewModelProvider(store, object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = viewModel as VM
    })[javaClass]
    store.clear()
}
