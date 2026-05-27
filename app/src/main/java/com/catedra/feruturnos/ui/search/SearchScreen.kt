package com.catedra.feruturnos.ui.search

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catedra.feruturnos.data.model.Court
import com.catedra.feruturnos.data.model.hardcodedCourts
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
import kotlin.math.*

// Función auxiliar para calcular distancia en kilómetros (Fórmula de Haversine)
fun calculateDistance(p1: GeoPoint, p2: GeoPoint): Double {
    val r = 6371 // Radio de la Tierra en km
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLon = Math.toRadians(p2.longitude - p1.longitude)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

@SuppressLint("MissingPermission")
@Composable
fun SearchScreen(
    onNavigateToReservation: (Court) -> Unit,
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCourt by remember { mutableStateOf<Court?>(null) }

    // userLocation: la única fuente de verdad para el mapa y las distancias
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val currentUserMarkerState = rememberMarkerState()

    val courts = remember { hardcodedCourts }

    val userIconDrawable = remember {
        androidx.core.content.ContextCompat.getDrawable(
            context,
            com.catedra.feruturnos.R.drawable.ic_user_location
        )
    }

    // Inicializar OSMDroid una sola vez en esta pantalla
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        }
    }

    // Obtener ubicación real (Caché rápido + fix fresco del GPS)
    LaunchedEffect(isPermissionGranted) {
        if (!isPermissionGranted) return@LaunchedEffect
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)

            // Intento rápido con caché
            val cached = fusedClient.lastLocation.await()
            if (cached != null) {
                userLocation = GeoPoint(cached.latitude, cached.longitude)
            }

            // Fix fresco del GPS — sin caché (0 update age)
            val request = com.google.android.gms.location.CurrentLocationRequest.Builder()
                .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(5_000L)
                .setMaxUpdateAgeMillis(0L)
                .build()
            val fresh = fusedClient.getCurrentLocation(request, null).await()
            if (fresh != null) {
                userLocation = GeoPoint(fresh.latitude, fresh.longitude)
            }
        } catch (e: Exception) {
            // Queda null si falla el GPS
        }
    }

    // key(userLocation) fuerza el centrado cuando llega el fix real del GPS
    val cameraState = key(userLocation) {
        rememberCameraState {
            geoPoint = userLocation ?: GeoPoint(-34.6037, -58.3816) // Default BsAs si tarda el GPS
            zoom = if (userLocation != null) 14.0 else 4.0
        }
    }

    // --- Filtrar y ordenar de forma reactiva ---
    val processedCourts = remember(searchQuery, userLocation) {
        var list = courts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.sport.contains(searchQuery, ignoreCase = true)
        }

        // Si tenemos fix del GPS, se ordena de la más cercana a la más lejana
        if (userLocation != null) {
            list = list.sortedBy { calculateDistance(userLocation!!, it.location) }
        }
        list
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Contenedor tipo Box: Todo lo que esté acá adentro se puede superponer en capas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Mantiene el 50% de la pantalla vertical
        ) {

            // A. CAPA INFERIOR: El Mapa (se dibuja primero)
            when {
                !isPermissionGranted -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                userLocation == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Obteniendo tu ubicación...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
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
                            processedCourts.forEach { court ->
                                val courtMarkerState = rememberMarkerState(key = court.name, geoPoint = court.location)
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

                            currentUserMarkerState.geoPoint = userLocation!!
                            Marker(
                                state = currentUserMarkerState,
                                title = "Tu ubicación",
                                snippet = "Estás acá",
                                icon = userIconDrawable,
                                onClick = { true }
                            )
                        }

                        // Badge indicativo de ubicación sobre el mapa
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
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF1565C0)))
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

            // B. CAPA SUPERIOR: La barra de búsqueda flotando arriba (se dibuja al final)
            // Usamos un Card blanco con elevación para forzar al SurfaceView a respetarlo
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter) // Lo alinea arriba al centro del mapa
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp) // Sombra fuerte anti-capas
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar predio o deporte...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }

        // 3. Mitad inferior: Lista ordenada e interactiva
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // El otro 50% de la pantalla
                .background(Color(0xFFF7F7F7))
        ) {
            Text(
                text = if (userLocation != null) "Canchas más cercanas a vos" else "Resultados de búsqueda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp) // Evita que la Card tape al último elemento
            ) {
                items(processedCourts) { court ->
                    CourtRowItem(
                        court = court,
                        userLocation = userLocation,
                        onClick = {
                            selectedCourt = court
                            // Desplaza el mapa hacia la cancha seleccionada en la lista
                            cameraState.geoPoint = court.location
                        }
                    )
                }
            }
        }
    }

    // 4. Card flotante / Modal del elemento seleccionado
    selectedCourt?.let { court ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            SelectedCourtCard(
                court = court,
                onReserve = { onNavigateToReservation(court) },
                onDismiss = { selectedCourt = null }
            )
        }
    }
}

@Composable
fun CourtRowItem(court: Court, userLocation: GeoPoint?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(court.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(court.sport, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                if (userLocation != null) {
                    val distance = calculateDistance(userLocation, court.location)
                    Text(
                        text = "A ${String.format("%.1f", distance)} km de tu ubicación",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = "$${court.pricePerHour}/h",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SelectedCourtCard(
    court: Court,
    onReserve: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(court.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) {
                    Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            }
            Text(court.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(onClick = {}, label = { Text(court.sport) })
                AssistChip(onClick = {}, label = { Text("$${court.pricePerHour}/h") })
                if (court.rating > 0) {
                    AssistChip(onClick = {}, label = { Text("⭐ ${court.rating}") })
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onReserve, modifier = Modifier.fillMaxWidth()) {
                Text("Ir a reservar")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchScreenPreview() {
    // Proveemos estados falsos fijos para que el entorno de diseño pueda renderizar la estructura
    SearchScreen(
        onNavigateToReservation = { _ -> },
        isPermissionGranted = true, // Simulamos que el permiso está aceptado
        onRequestPermission = {}
    )
}