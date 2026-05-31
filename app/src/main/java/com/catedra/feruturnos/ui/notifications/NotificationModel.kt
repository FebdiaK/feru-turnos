package com.catedra.feruturnos.ui.notifications

import com.google.firebase.Timestamp

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val reservationId: String = "",
    val read: Boolean = false,
    val createdAt: Timestamp? = null
)