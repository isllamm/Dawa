package com.family.dawa.domain.usecase.contact

import app.cash.turbine.test
import com.family.dawa.domain.model.Contact
import com.family.dawa.domain.repository.IContactRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContactUseCasesTest {

    private val contactRepository: IContactRepository = mockk(relaxed = true)
    private val contact = Contact(id = 1, name = "ماما", phone = "0100")

    @Test
    fun testGetPrimaryContact_flowComesFromRepository() = runTest {
        every { contactRepository.primaryContactFlow } returns flowOf(null, contact)

        GetPrimaryContactUseCase(contactRepository)().test {
            assertNull(awaitItem())
            assertEquals(contact, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testGetPrimaryContact_getReturnsCurrentContact() = runTest {
        coEvery { contactRepository.getPrimaryContact() } returns contact

        assertEquals(contact, GetPrimaryContactUseCase(contactRepository).get())
    }

    @Test
    fun testSavePrimaryContact_savesToRepository() = runTest {
        SavePrimaryContactUseCase(contactRepository)(contact)

        coVerify { contactRepository.savePrimaryContact(contact) }
    }
}
