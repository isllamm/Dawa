package com.family.dawa.domain.model

data class Contact(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val photoPath: String? = null
)
