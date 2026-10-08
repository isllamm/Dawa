package com.family.dawa.core.time

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.milliseconds

/**
 * Emits the current time (rounded down to the minute) straight away, then again each time
 * the minute changes. Dose times and grace periods are whole minutes, so this is enough to
 * move a slot from UPCOMING to DUE to MISSED, and to roll over to a new day at midnight.
 *
 * It checks every few seconds rather than sleeping until the next minute, because coroutine
 * delays pause while the phone is in deep sleep; a short check means the screen catches up
 * within seconds of the phone waking.
 */
fun TimeProvider.minuteTicks(checkEveryMillis: Long = 5_000L): Flow<ZonedDateTime> = flow {
    while (true) {
        emit(nowZoned().truncatedTo(ChronoUnit.MINUTES))
        delay(checkEveryMillis.milliseconds)
    }
}.distinctUntilChanged()
