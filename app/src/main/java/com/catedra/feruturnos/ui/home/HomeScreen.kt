package com.catedra.feruturnos.ui.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catedra.feruturnos.data.model.Court
import com.catedra.feruturnos.data.model.hardcodedCourts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import com.utsman.osmandcompose.rememberCameraState
import com.utsman.osmandcompose.rememberMarkerState
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint

data class Reservation(
    val reservationName: String,
    val startDate: String,
    val time: String,
    val place: String
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit = {}, // Acción para ir a la nueva pantalla de búsqueda
    onNavigateToReservation: (Court) -> Unit = {}
) {
    HomeScreenContent(
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToReservation = onNavigateToReservation
    )
}

@Composable
fun HomeScreenContent(
    onNavigateToSearch: () -> Unit, // Nueva acción de navegación
    onNavigateToReservation: (Court) -> Unit
) {
    val reservations = listOf(
        Reservation("Jueves de fulbo con los pibes", "Jueves 21 de Mayo", "20:30 hs", "Los manzanos"),
        Reservation("Los del club de Bera", "Viernes 22 de Mayo", "21:30 hs", "Los manzanos"),
    )
    val courts = remember { hardcodedCourts }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            CurrentReservationSection(
                title = "Reservas actuales",
                reservations = reservations
            )
        }
        item {
            // Un banner atractivo en lugar del mapa completo
            SearchBannerSection(onExploreClick = onNavigateToSearch)
        }
        item {
            ConnectPeopleSection(
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
            reservations.forEach { ReservationsItem(it) }
        }
//        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ReservationsItem(reservation: Reservation) {
    Spacer(modifier = Modifier.fillMaxWidth().height(4.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black,
                spotColor = MaterialTheme.colorScheme.primary
            )
            .background(color = Color.White, shape = RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = Color.White, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = reservation.reservationName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text("${reservation.startDate} - ${reservation.time}")
        Text(reservation.place)
    }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
    )
}

@Composable
fun SearchBannerSection(onExploreClick : () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)) {
            Text("📍 ¿Buscando dónde jugar?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Explorá las canchas más cercanas en el mapa y reservá tu turno al instante.", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onExploreClick, modifier = Modifier.fillMaxWidth()) {
                Text("Buscar Canchas")
            }
        }
    }
}

@Composable
fun ConnectPeopleSection(title: String, btnText: String) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) { Text(btnText) }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}