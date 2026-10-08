package com.family.dawa.core.time

import com.family.dawa.testutil.FixedTimeProvider
import com.family.dawa.testutil.at
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class MinuteTicksTest {

    @Test
    fun testMinuteTicks_emitsCurrentMinuteStraightAway() = runTest {
        val clock = FixedTimeProvider(at(7, 59).plusSeconds(30))
        val ticks = mutableListOf<ZonedDateTime>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { clock.minuteTicks().toList(ticks) }

        assertEquals(listOf(at(7, 59)), ticks)
    }

    @Test
    fun testMinuteTicks_emitsOnlyWhenMinuteChanges() = runTest {
        val clock = FixedTimeProvider(at(7, 59).plusSeconds(30))
        val ticks = mutableListOf<ZonedDateTime>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { clock.minuteTicks().toList(ticks) }

        clock.now = at(7, 59).plusSeconds(50)
        advanceTimeBy(5_001)
        assertEquals("same minute, no new tick", listOf(at(7, 59)), ticks)

        clock.now = at(8, 0).plusSeconds(2)
        advanceTimeBy(5_001)
        assertEquals(listOf(at(7, 59), at(8, 0)), ticks)
    }

    @Test
    fun testMinuteTicks_catchesUpAfterAJump() = runTest {
        // e.g. the phone slept for a long time, or the debug offset moved the clock
        val clock = FixedTimeProvider(at(7, 0))
        val ticks = mutableListOf<ZonedDateTime>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { clock.minuteTicks().toList(ticks) }

        clock.now = at(9, 30).plusSeconds(10)
        advanceTimeBy(5_001)

        assertEquals(listOf(at(7, 0), at(9, 30)), ticks)
    }
}
