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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

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
        modifier = Modifier.fillMaxSize()) {
        item {
            CurrentReservationSection(
                title = "Reservas actuales",
                reservations = reservations
            )
        }

        item {
            NewReservationSection(
                title = "Generar una nueva reserva",
                btnText = "Explorar"
            )
        }

        item {
            NewReservationSection(
                title = "Conectar con personas",
                btnText = "Conectar"
            )
        }

    }
}

@Composable
fun CurrentReservationSection(
    title: String,
    reservations: List<Reservation>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        reservations.forEach { reservation ->
            ReservationsItem(reservation)
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun NewReservationSection(
    title: String,
    btnText: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Contenido de la sección")
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { },
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) { Text(btnText) }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun ReservationsItem(
    reservation: Reservation
) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black,
                spotColor = MaterialTheme.colorScheme.primary
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
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
            .height(4.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}