package com.family.dawa.domain.usecase.contact

import com.family.dawa.domain.model.Contact
import com.family.dawa.domain.repository.IContactRepository
import kotlinx.coroutines.flow.Flow

class GetPrimaryContactUseCase(
    private val contactRepository: IContactRepository
) {
    operator fun invoke(): Flow<Contact?> = contactRepository.primaryContactFlow
    suspend fun get(): Contact? = contactRepository.getPrimaryContact()
}

class SavePrimaryContactUseCase(
    private val contactRepository: IContactRepository
) {
    suspend operator fun invoke(contact: Contact) {
        contactRepository.savePrimaryContact(contact)
    }
}
