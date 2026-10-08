package com.family.dawa.core.time

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Pluggable time source allowing deterministic unit tests and developer debug fast-forward.
 */
interface TimeProvider {
    fun nowZoned(): ZonedDateTime
    fun nowEpochMillis(): Long = nowZoned().toInstant().toEpochMilli()
    fun today(): LocalDate = nowZoned().toLocalDate()
    fun currentTime(): LocalTime = nowZoned().toLocalTime()
    fun zoneId(): ZoneId = nowZoned().zone
}

class SystemTimeProvider(
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : TimeProvider {
    override fun nowZoned(): ZonedDateTime = ZonedDateTime.now(zoneId)
}

class DebugTimeProvider(
    private val baseProvider: TimeProvider = SystemTimeProvider(),
    @Volatile var offsetMillis: Long = 0L
) : TimeProvider {
    override fun nowZoned(): ZonedDateTime {
        val base = baseProvider.nowZoned()
        return if (offsetMillis == 0L) base else base.plusSeconds(offsetMillis / 1000)
    }

    override fun nowEpochMillis(): Long {
        return baseProvider.nowEpochMillis() + offsetMillis
    }
}
