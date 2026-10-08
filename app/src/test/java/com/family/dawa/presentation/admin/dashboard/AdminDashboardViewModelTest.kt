package com.family.dawa.presentation.admin.dashboard

import android.content.Context
import com.family.dawa.core.permissions.HealthCheckItem
import com.family.dawa.core.permissions.PermissionChecker
import com.family.dawa.domain.model.SlotStatus
import com.family.dawa.domain.model.SlotWithStatus
import com.family.dawa.domain.usecase.history.GetTodayTimelineUseCase
import com.family.dawa.testutil.MainDispatcherRule
import com.family.dawa.testutil.slot
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AdminDashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val timeline = MutableSharedFlow<List<SlotWithStatus>>(replay = 1)
    private val getTodayTimeline: GetTodayTimelineUseCase = mockk()
    private val context: Context = mockk()

    @Before
    fun setUp() {
        every { getTodayTimeline() } returns timeline
        mockkObject(PermissionChecker)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun item(isOk: Boolean) = HealthCheckItem(id = "x", title = "t", description = "d", isOk = isOk)

    @Test
    fun testInit_isLoadingUntilTimelineArrives() {
        val vm = createViewModel()
        assertTrue(vm.state.value.isLoading)

        val slots = listOf(SlotWithStatus(slot(), SlotStatus.UPCOMING))
        timeline.tryEmit(slots)

        assertEquals(AdminDashboardState(timeline = slots, isLoading = false), vm.state.value)
    }

    @Test
    fun testCheckPermissions_anyFailingItemShowsIssues() {
        every { PermissionChecker.checkHealth(context) } returns listOf(item(isOk = true), item(isOk = false))
        val vm = createViewModel()

        vm.sendIntent(AdminDashboardIntent.CheckPermissions(context))

        assertTrue(vm.state.value.hasPermissionIssues)
    }

    @Test
    fun testCheckPermissions_allOkShowsNoIssues() {
        every { PermissionChecker.checkHealth(context) } returns listOf(item(isOk = true), item(isOk = true))
        val vm = createViewModel()

        vm.sendIntent(AdminDashboardIntent.CheckPermissions(context))

        assertFalse(vm.state.value.hasPermissionIssues)
    }

    private fun createViewModel() = AdminDashboardViewModel(getTodayTimeline)
}
