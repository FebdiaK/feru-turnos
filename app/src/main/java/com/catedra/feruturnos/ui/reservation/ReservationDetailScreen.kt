package com.catedra.feruturnos.ui.reservation

import android.content.Context
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
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import com.utsman.osmandcompose.rememberCameraState
import com.utsman.osmandcompose.rememberMarkerState
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color

@Composable
fun ReservationDetailScreen(
    reservationId: String
) {
    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var addressText by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        }
    }

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

    val r = reservation

    if (r == null) {
        Text(
            text = "No se encontró la reserva",
            modifier = Modifier.padding(36.dp)
        )
        return
    }

    val mapPoint = r.placeAddress?.let {
        GeoPoint(it.latitude, it.longitude)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (mapPoint != null) {
            FixedReservationMap(
                mapPoint = mapPoint,
                title = r.placeName,
                snippet = r.placeFieldType,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }

        LaunchedEffect(mapPoint) {
            if (mapPoint != null) {
                addressText = withContext(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val results = geocoder.getFromLocation(
                            mapPoint.latitude,
                            mapPoint.longitude,
                            1
                        )

                        results?.firstOrNull()?.getAddressLine(0) ?: ""
                    } catch (e: Exception) {
                        ""
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = r.placeName,
                    color = Color.White,
                    modifier = Modifier.weight(2f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ir",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = if (addressText.isNotBlank()) {
                        "Dirección: $addressText"
                    } else {
                        "Dirección: cargando..."
                    }
                )

                Text("Cancha: ${r.placeFieldType}")
                Text("Día: ${r.reservationDay}")
                Text("Hora: ${r.reservationHour} hs")
                Text("Precio: $${r.placeFieldPrice}")

                Text("Creador de reserva")
                Text("${r.reservationCreatorName}")
                Text("${r.reservationCreatorPhone}")
            }
        }
    }
}

@Composable
fun FixedReservationMap(
    mapPoint: GeoPoint,
    title: String,
    snippet: String,
    modifier: Modifier = Modifier
) {
    val cameraState = rememberCameraState {
        geoPoint = mapPoint
        zoom = 16.0
    }

    val markerState = rememberMarkerState(
        geoPoint = mapPoint
    )

    Box(
        modifier = modifier
            .clipToBounds()
    ) {
        OpenStreetMap(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState,
            properties = DefaultMapProperties.copy(
                isTilesScaledToDpi = true,
                isMultiTouchControls = false,
                zoomButtonVisibility = ZoomButtonVisibility.NEVER
            )
        ) {
            Marker(
                state = markerState,
                title = title,
                snippet = snippet,
                onClick = { true }
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
        )
    }
}