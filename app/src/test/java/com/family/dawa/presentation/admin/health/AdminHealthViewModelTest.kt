package com.family.dawa.presentation.admin.health

import android.content.Context
import com.family.dawa.core.permissions.HealthCheckItem
import com.family.dawa.core.permissions.PermissionChecker
import com.family.dawa.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AdminHealthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val appContext: Context = mockk()
    private val screenContext: Context = mockk()
    private val notificationsOk = HealthCheckItem(id = "notifications", title = "t", description = "d", isOk = true)
    private val batteryBad = HealthCheckItem(id = "battery", title = "t", description = "d", isOk = false)

    @Before
    fun setUp() {
        mockkObject(PermissionChecker)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInit_loadsHealthItemsFromAppContext() {
        every { PermissionChecker.checkHealth(appContext) } returns listOf(notificationsOk)

        val vm = AdminHealthViewModel(appContext)

        assertEquals(listOf(notificationsOk), vm.state.value.items)
    }

    @Test
    fun testRefresh_reloadsItemsWithGivenContext() {
        every { PermissionChecker.checkHealth(appContext) } returns listOf(notificationsOk)
        every { PermissionChecker.checkHealth(screenContext) } returns listOf(notificationsOk, batteryBad)
        val vm = AdminHealthViewModel(appContext)

        vm.sendIntent(AdminHealthIntent.Refresh(screenContext))

        assertEquals(listOf(notificationsOk, batteryBad), vm.state.value.items)
    }
}
