package com.catedra.feruturnos.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import com.catedra.feruturnos.ui.navigation.Rutas
import com.catedra.feruturnos.ui.notifications.Notificacion
import com.catedra.feruturnos.ui.notifications.NotificacionItem

data class Reservation(
    val reservationName: String,
    val startDate: String,
    val time: String,
    val place: String
)

@Composable
fun HomeScreen() {

    val reservations = listOf(
        Reservation("Jueves de fulbo con los pibes", "Jueves 21 de Mayo", "20:30 hs", "Los manzanos"),
        Reservation("Los del club de Bera", "Viernes 22 de Mayo", "21:30 hs", "Los manzanos"),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
        item {
            CurrentReservationSection(
                titulo = "Reservas actuales",
                reservations = reservations
            )
        }

        item {
            NewReservationSection(
                titulo = "Generar una nueva reserva"
            )
        }

        item {
            NewReservationSection(
                titulo = "Conectar con personas"
            )
        }

    }
}

@Composable
fun CurrentReservationSection(
    titulo: String,
    reservations: List<Reservation>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        reservations.forEach { reservation ->
            ReservationsItem(reservation)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun NewReservationSection(
    titulo: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Contenido de la sección")
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun ReservationsItem(
    reservation: Reservation
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 4.dp,
                color = MaterialTheme.colorScheme.secondary
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "${reservation.reservationName} ",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text("${reservation.startDate} - ${reservation.time}")
        Text("${reservation.place}")
    }

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}