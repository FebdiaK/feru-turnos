package com.catedra.feruturnos.ui.reservation

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.catedra.feruturnos.ui.search.EnclosureItem
import com.catedra.feruturnos.ui.search.FieldItem
import kotlinx.coroutines.tasks.await
import android.content.Context
import com.catedra.feruturnos.R

class ReservationRepository {

    private val db by lazy { Firebase.firestore }
    private val auth by lazy { FirebaseAuth.getInstance() }

    suspend fun createReservation(
        context: Context,
        enclosure: EnclosureItem,
        field: FieldItem,
        selectedDay: String,
        selectedHour: String,
        reservationName: String
    ): String {
        val user = auth.currentUser
            ?: throw IllegalStateException(
                context.getString(R.string.no_hay_sesion_activa)
            )

        val userQuery = db.collection("users")
            .whereEqualTo("uid", user.uid)
            .get()
            .await()

        val userDoc = userQuery.documents.firstOrNull()

        val creatorName = userDoc?.getString("name")
            ?: context.getString(R.string.usuario)
        val creatorPhone = userDoc?.getLong("celphone")  ?: 0L

        val data = hashMapOf(
            "createdAt"               to Timestamp.now(),
            "creatorId"               to user.uid,
            "enclosuseId"             to enclosure.id,
            "fieldId"                 to field.id.toString(),
            "open"                    to false,
            "participantsId"          to listOf(user.uid),
            "placeAddress"            to enclosure.address,
            "placeFieldPrice"         to field.price,
            "placeFieldType"          to field.type,
            "placeLocation"           to GeoPoint(
                enclosure.location.latitude,
                enclosure.location.longitude
            ),
            "placeName"               to enclosure.name,
            "reservationCreatorName"  to creatorName,
            "reservationCreatorPhone" to creatorPhone,
            "reservationDay"          to selectedDay,
            "reservationHour"         to selectedHour,
            "reservationName"         to reservationName
        )

        val docRef = db.collection("reservations").add(data).await()

        db.collection("notifications")
            .add(
                hashMapOf(
                    "title" to context.getString(R.string.reserva_creada_con_exito),
                    "message" to context.getString(
                        R.string.mensaje_reserva_creada,
                        enclosure.name,
                        field.type,
                        selectedDay,
                        selectedHour
                    ),
                    "reservationId" to docRef.id,
                    "relatedUsers" to listOf(
                        hashMapOf(
                            "userId" to user.uid,
                            "read" to false
                        )
                    ),
                    "createdAt" to Timestamp.now()
                )
            )
            .await()

        return docRef.id
    }
}