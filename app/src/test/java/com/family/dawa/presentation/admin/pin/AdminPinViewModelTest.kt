package com.family.dawa.presentation.admin.pin

import app.cash.turbine.test
import com.family.dawa.domain.usecase.settings.VerifyAdminPinUseCase
import com.family.dawa.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AdminPinViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val verifyPin: VerifyAdminPinUseCase = mockk()

    private fun createViewModel() = AdminPinViewModel(verifyPin)

    private fun AdminPinViewModel.type(digits: String) =
        digits.forEach { sendIntent(AdminPinIntent.DigitEntered(it.toString())) }

    @Test
    fun testDigitEntered_appendsDigits() {
        val vm = createViewModel()

        vm.type("12")

        assertEquals(AdminPinState(enteredPin = "12"), vm.state.value)
    }

    @Test
    fun testCorrectPin_verifiesOnceAndNavigatesToDashboard() = runTest {
        coEvery { verifyPin("1234") } returns true
        val vm = createViewModel()

        vm.effect.test {
            vm.type("1234")
            assertEquals(AdminPinEffect.NavigateToDashboard, awaitItem())
        }
        coVerify(exactly = 1) { verifyPin("1234") }
        assertTrue(vm.state.value.isLoading)
    }

    @Test
    fun testWrongPin_showsErrorClearsPinAndDoesNotNavigate() = runTest {
        coEvery { verifyPin(any()) } returns false
        val vm = createViewModel()

        vm.effect.test {
            vm.type("0000")
            expectNoEvents()
        }
        assertEquals(AdminPinState(enteredPin = "", isError = true, isLoading = false), vm.state.value)
    }

    @Test
    fun testTypingAfterError_clearsError() {
        coEvery { verifyPin(any()) } returns false
        val vm = createViewModel()
        vm.type("0000")

        vm.type("5")

        assertEquals(AdminPinState(enteredPin = "5", isError = false), vm.state.value)
    }

    @Test
    fun testExtraDigitsWhileVerifying_areIgnored() {
        val pending = CompletableDeferred<Boolean>()
        coEvery { verifyPin(any()) } coAnswers { pending.await() }
        val vm = createViewModel()

        vm.type("12345")

        assertEquals("1234", vm.state.value.enteredPin)
        coVerify(exactly = 1) { verifyPin("1234") }
        pending.complete(false)
    }

    @Test
    fun testBackspace_removesLastDigitAndClearsError() {
        val vm = createViewModel()
        vm.type("123")

        vm.sendIntent(AdminPinIntent.Backspace)

        assertEquals(AdminPinState(enteredPin = "12"), vm.state.value)
    }

    @Test
    fun testBackspace_onEmptyPinDoesNothing() {
        val vm = createViewModel()

        vm.sendIntent(AdminPinIntent.Backspace)

        assertEquals(AdminPinState(), vm.state.value)
    }

    @Test
    fun testClear_resetsPinAndError() {
        coEvery { verifyPin(any()) } returns false
        val vm = createViewModel()
        vm.type("0000")
        vm.type("12")

        vm.sendIntent(AdminPinIntent.Clear)

        assertEquals(AdminPinState(), vm.state.value)
    }
}
