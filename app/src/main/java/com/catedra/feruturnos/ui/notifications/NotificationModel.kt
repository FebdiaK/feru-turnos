package com.catedra.feruturnos.ui.notifications

import com.google.firebase.Timestamp

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val reservationId: String = "",
    val relatedUsers: List<RelatedNotificationUser> = emptyList(),
    val createdAt: Timestamp? = null
)

data class RelatedNotificationUser(
    val userId: String = "",
    val read: Boolean = false
)