package com.family.dawa.presentation.admin.contact

import app.cash.turbine.test
import com.family.dawa.domain.model.Contact
import com.family.dawa.domain.usecase.contact.GetPrimaryContactUseCase
import com.family.dawa.domain.usecase.contact.SavePrimaryContactUseCase
import com.family.dawa.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AdminContactViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPrimaryContact: GetPrimaryContactUseCase = mockk()
    private val savePrimaryContact: SavePrimaryContactUseCase = mockk(relaxed = true)

    private fun createViewModel() = AdminContactViewModel(getPrimaryContact, savePrimaryContact)

    @Test
    fun testInit_fillsFormFromSavedContact() {
        coEvery { getPrimaryContact.get() } returns Contact(id = 1, name = "ماما", phone = "0100")

        val vm = createViewModel()

        assertEquals(AdminContactState(name = "ماما", phone = "0100"), vm.state.value)
    }

    @Test
    fun testInit_noSavedContactLeavesFormEmpty() {
        coEvery { getPrimaryContact.get() } returns null

        val vm = createViewModel()

        assertEquals(AdminContactState(), vm.state.value)
    }

    @Test
    fun testNameAndPhoneChanges_updateState() {
        coEvery { getPrimaryContact.get() } returns null
        val vm = createViewModel()

        vm.sendIntent(AdminContactIntent.NameChanged("بابا"))
        vm.sendIntent(AdminContactIntent.PhoneChanged("0111"))

        assertEquals(AdminContactState(name = "بابا", phone = "0111"), vm.state.value)
    }

    @Test
    fun testSave_savesTrimmedContactAndNavigatesBack() = runTest {
        coEvery { getPrimaryContact.get() } returns null
        val vm = createViewModel()
        vm.sendIntent(AdminContactIntent.NameChanged(" بابا "))
        vm.sendIntent(AdminContactIntent.PhoneChanged(" 0111 "))

        vm.effect.test {
            vm.sendIntent(AdminContactIntent.Save)
            assertEquals(AdminContactEffect.NavigateBack, awaitItem())
        }
        coVerify { savePrimaryContact(Contact(name = "بابا", phone = "0111")) }
    }

    @Test
    fun testSave_blankNameOrPhoneDoesNothing() = runTest {
        coEvery { getPrimaryContact.get() } returns null
        val vm = createViewModel()

        vm.effect.test {
            vm.sendIntent(AdminContactIntent.NameChanged("بابا"))
            vm.sendIntent(AdminContactIntent.PhoneChanged("  "))
            vm.sendIntent(AdminContactIntent.Save)

            vm.sendIntent(AdminContactIntent.NameChanged(""))
            vm.sendIntent(AdminContactIntent.PhoneChanged("0111"))
            vm.sendIntent(AdminContactIntent.Save)

            expectNoEvents()
        }
        coVerify(exactly = 0) { savePrimaryContact(any()) }
    }
}
