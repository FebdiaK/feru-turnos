package com.catedra.feruturnos.ui.profile

data class ProfileState(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val address: String = "",
    val photo: String = "",
    val celphone: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)