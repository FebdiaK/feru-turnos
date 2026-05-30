package com.catedra.feruturnos.ui.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catedra.feruturnos.ui.home.Reservation
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

@Composable
fun ReservationDetailScreen(
    reservationId: String
) {
    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(reservationId) {
        try {
            val doc = Firebase.firestore
                .collection("reservations")
                .document(reservationId)
                .get()
                .await()

            reservation = doc.toObject(Reservation::class.java)?.copy(
                id = doc.id
            )
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }

    if (reservation == null) {
        Text(
            text = "No se encontró la reserva",
            modifier = Modifier.padding(24.dp)
        )
        return
    }

    val r = reservation!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = r.reservationName,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Lugar: ${r.placeName}")
        Text("Cancha: ${r.placeFieldType}")
        Text("Día: ${r.reservationDay}")
        Text("Hora: ${r.reservationHour}")
        Text("Creador: ${r.reservationCreatorName}")
        Text("Teléfono: ${r.reservationCreatorPhone}")
        Text("Precio: $${r.placeFieldPrice}")
    }
}