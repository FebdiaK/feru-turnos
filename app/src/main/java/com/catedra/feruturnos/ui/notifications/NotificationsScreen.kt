package com.catedra.feruturnos.ui.notifications

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.items

data class Notificacion(
    val sport: String,
    val startDate: String,
    val time: String,
    val place: String,
    val read: Boolean
)

@Composable
fun NotificationsScreen() {
    val notificaciones = listOf(
        Notificacion("FUTBOL", "Jueves 21 de Mayo", "20:30 hs", "Los manzanos", false),
        Notificacion("FUTBOL", "Jueves 14 de Mayo", "20:30 hs", "Los manzanos", true),
        Notificacion("FUTBOL", "Jueves 7 de Mayo", "20:30 hs", "Los manzanos", true),
        Notificacion("FUTBOL", "Jueves 14 de Mayo", "20:30 hs", "Los manzanos", true),
        Notificacion("FUTBOL", "Jueves 7 de Mayo", "20:30 hs", "Los manzanos", true),
        Notificacion("FUTBOL", "Jueves 14 de Mayo", "20:30 hs", "Los manzanos", true),
        Notificacion("FUTBOL", "Jueves 7 de Mayo", "20:30 hs", "Los manzanos", true),
        Notificacion("FUTBOL", "Jueves 14 de Mayo", "20:30 hs", "Los manzanos", true),
        Notificacion("FUTBOL", "Jueves 7 de Mayo", "20:30 hs", "Los manzanos", true)
    )

    LazyColumn( modifier = Modifier.fillMaxSize() ) {
        items(notificaciones) { noti ->
            NotificacionItem(noti)
        }
    }
}

@Composable
fun NotificacionItem(notificacion: Notificacion) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 4.dp,
                color = if (!notificacion.read) MaterialTheme.colorScheme.secondary
                else Color.White
            )
            .background(
                if (notificacion.read) Color.White
                else MaterialTheme.colorScheme.tertiary
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Usted ha sido añadido a una reserva de ${notificacion.sport}. " +
                    "Datos de la reserva: ${notificacion.startDate}, ${notificacion.time}. " +
                    "en ${notificacion.place}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (notificacion.read) FontWeight.Normal else FontWeight.Medium
        )
    }

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color(0xFFF2F2F2))
    )
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen()
}