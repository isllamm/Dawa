package com.family.dawa.data.repo

import com.family.dawa.data.db.ContactDao
import com.family.dawa.data.db.ContactEntity
import com.family.dawa.domain.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContactRepository(
    private val contactDao: ContactDao
) {
    val primaryContactFlow: Flow<Contact?> =
        contactDao.getPrimaryContactFlow().map { it?.toDomain() }

    suspend fun getPrimaryContact(): Contact? =
        contactDao.getPrimaryContactSync()?.toDomain()

    suspend fun savePrimaryContact(contact: Contact) {
        val existing = contactDao.getPrimaryContactSync()
        val entity = if (existing != null) {
            ContactEntity.fromDomain(contact.copy(id = existing.id))
        } else {
            ContactEntity.fromDomain(contact)
        }
        contactDao.insert(entity)
    }
}
