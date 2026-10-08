package com.family.dawa.domain.repository

import com.family.dawa.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface IContactRepository {
    val primaryContactFlow: Flow<Contact?>
    suspend fun getPrimaryContact(): Contact?
    suspend fun savePrimaryContact(contact: Contact)
}
