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
fun HomeScreen(onNavigateToReservation: (Court) -> Unit = {}) {
    if (LocalInspectionMode.current) {
        HomeScreenContent(
            onNavigateToReservation = onNavigateToReservation,
            isPermissionGranted = false,
            onRequestPermission = {}
        )
    } else {
        val locationPermissionState = rememberPermissionState(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

//        LaunchedEffect(Unit) {
//            if (!locationPermissionState.status.isGranted) {
//                locationPermissionState.launchPermissionRequest()
//            }
//        }

        HomeScreenContent(
            onNavigateToReservation = onNavigateToReservation,
            isPermissionGranted = locationPermissionState.status.isGranted,
            onRequestPermission = { locationPermissionState.launchPermissionRequest() }
        )
    }
}
@Composable
fun HomeScreenContent(
    onNavigateToReservation: (Court) -> Unit,
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit
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
            OsmMapReservationSection(
                title = "Generar una nueva reserva",
                courtsList = courts,
                isPermissionGranted = isPermissionGranted,
                onRequestPermission = onRequestPermission,
                onNavigateToReservation = onNavigateToReservation
            )
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
    Spacer(modifier = Modifier.height(16.dp))
}

@SuppressLint("MissingPermission")
@Composable
fun OsmMapReservationSection(
    title: String,
    courtsList: List<Court>,
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onNavigateToReservation: (Court) -> Unit
) {
    val context = LocalContext.current
    var selectedCourt by remember { mutableStateOf<Court?>(null) }

    // userLocation: null hasta que el GPS responda.
    // Es la única fuente de verdad para centrar el mapa — sin defaultCenter hardcodeado.
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }

    val currentUserMarkerState = rememberMarkerState()

    val userIconDrawable = remember {
        androidx.core.content.ContextCompat.getDrawable(
            context,
            com.catedra.feruturnos.R.drawable.ic_user_location
        )
    }

    // Inicializar OSMDroid una sola vez
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        }
    }

    // Obtener ubicación real en cuanto el permiso esté concedido.
    // setMaxUpdateAgeMillis(0) rechaza cualquier caché: solo acepta un fix fresco.
    LaunchedEffect(isPermissionGranted) {
        if (!isPermissionGranted) return@LaunchedEffect
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)

            // Intento rápido con caché (puede ser null)
            val cached = fusedClient.lastLocation.await()
            if (cached != null) {
                userLocation = GeoPoint(cached.latitude, cached.longitude)
            }

            // Fix fresco del GPS — sobreescribe el caché
            val request = com.google.android.gms.location.CurrentLocationRequest.Builder()
                .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(5_000L)
                .setMaxUpdateAgeMillis(0L) // 0 = solo fix nuevo, sin caché
                .build()
            val fresh = fusedClient.getCurrentLocation(request, null).await()
            if (fresh != null) {
                userLocation = GeoPoint(fresh.latitude, fresh.longitude)
            }
        } catch (e: Exception) {
            // userLocation queda null → el Card muestra spinner de espera
        }
    }

    // key(userLocation) fuerza que rememberCameraState se recree cuando llega
    // la ubicación real, evitando que quede pegado en la posición inicial.
    val cameraState = key(userLocation) {
        rememberCameraState {
            geoPoint = userLocation ?: GeoPoint(0.0, 0.0)
            zoom = if (userLocation != null) 14.0 else 2.0
        }
    }

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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Canchas cercanas a vos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            when {
                // 1. Sin permiso → pedir habilitarlo
                !isPermissionGranted -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📍", style = MaterialTheme.typography.displaySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Para ver los predios más cercanos necesitás habilitar el acceso a tu ubicación.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRequestPermission) {
                                Text("Habilitar ubicación")
                            }
                        }
                    }
                }

                // 2. Permiso dado pero GPS todavía no respondió → spinner
                userLocation == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            androidx.compose.material3.CircularProgressIndicator()
                            Text(
                                text = "Obteniendo tu ubicación...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // 3. Tenemos ubicación real → mostrar el mapa
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OpenStreetMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraState = cameraState,
                            properties = DefaultMapProperties.copy(
                                isTilesScaledToDpi = true,
                                isMultiTouchControls = true,
                                zoomButtonVisibility = ZoomButtonVisibility.SHOW_AND_FADEOUT
                            )
                        ) {
                            // Marcadores de canchas
                            courtsList.forEach { court ->
                                val courtMarkerState = rememberMarkerState(
                                    key = court.name, // O court.id si tuviera uno
                                    geoPoint = court.location
                                )

                                Marker(
                                    state = courtMarkerState,
                                    title = court.name,
                                    snippet = "${court.sport} · $${court.pricePerHour}/h",
                                    onClick = {
                                        selectedCourt = court
                                        true
                                    }
                                )
                            }
                            if (userLocation != null) {
                                currentUserMarkerState.geoPoint = userLocation!!
                                Marker(
                                    state = currentUserMarkerState,
                                    title = "Tu ubicación",
                                    snippet = "Estás acá",
                                    icon = userIconDrawable,
                                    onClick = { true }
                                )
                            }
                        }

                        // Puntito azul: badge sobre el mapa indicando la ubicación del usuario
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1565C0))
                                )
                                Text(
                                    text = "Tu ubicación",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card de cancha seleccionada: aparece al tocar un marcador
        selectedCourt?.let { court ->
            Spacer(modifier = Modifier.height(12.dp))
            SelectedCourtCard(
                court = court,
                onReserve = { onNavigateToReservation(court) },
                onDismiss = { selectedCourt = null }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

// Card de cancha seleccionada

@Composable
fun SelectedCourtCard(
    court: Court,
    onReserve: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = court.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onDismiss) {
                    Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = court.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(onClick = {}, label = { Text(court.sport) })
                AssistChip(onClick = {}, label = { Text("$${court.pricePerHour}/h") })
                AssistChip(onClick = {}, label = { Text("⭐ ${court.rating}") })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onReserve,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir a reservar")
            }
        }
    }
}

@Composable
fun ConnectPeopleSection(title: String, btnText: String) {
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
    Spacer(modifier = Modifier.height(16.dp))
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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}