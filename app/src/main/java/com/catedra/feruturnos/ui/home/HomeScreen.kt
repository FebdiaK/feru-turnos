package com.catedra.feruturnos.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.catedra.feruturnos.data.model.Court

@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToReservation: (Court) -> Unit = {}
) {
    val reservations = listOf(
        Reservation("Jueves de fulbo con los pibes", "Jueves 21 de Mayo", "20:30 hs", "Los manzanos"),
        Reservation("Los del club de Bera", "Viernes 22 de Mayo", "21:30 hs", "Los manzanos"),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CurrentReservationSection(
                title = "Reservas actuales",
                reservations = reservations
            )
        }

        item {
            SearchBannerSection(
                title = "¿Buscando dónde jugar?",
                onExploreClick = onNavigateToSearch
            )
        }

        item {
            ConnectPeopleSection(
                title = "Conectar con personas",
                btnText = "Buscar"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}