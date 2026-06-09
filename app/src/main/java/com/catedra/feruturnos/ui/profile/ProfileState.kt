package com.catedra.feruturnos.ui.profile

import com.catedra.feruturnos.ui.contacts.ContactUser

data class ProfileState(
    val uid: String = "",
    val contactId: String = "",
    val name: String = "",
    val email: String = "",
    val address: String = "",
    val photo: String = "",
    val celphone: String = "",
    val stars: List<Int> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val friends: List<ContactUser> = emptyList()
)