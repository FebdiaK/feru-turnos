package com.catedra.feruturnos.ui.home

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Reservation(
    val createdAt: Timestamp? = null,
    val creatorId: String = "",
    val enclosureId: String = "",
    val fieldId: String = "",
    val participantsId: List<String> = emptyList(),
    val placeAddress: GeoPoint? = null,
    val placeFieldPrice: Int = 0,
    val placeFieldType: String = "",
    val placeName: String = "",
    val reservationCreatorName: String = "",
    val reservationCreatorPhone: Int = 0,
    val reservationDay: String = "",
    val reservationHour: String = "",
    val reservationName: String = ""
)