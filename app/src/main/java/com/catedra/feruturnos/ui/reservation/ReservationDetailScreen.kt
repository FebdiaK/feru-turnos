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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FieldValue
import androidx.compose.material3.HorizontalDivider
import com.catedra.feruturnos.ui.home.ContactUser
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun ReservationDetailScreen(
    reservationId: String
) {
    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var participants by remember {
        mutableStateOf<List<ContactUser>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
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

            val reservationData = reservation

            if (reservationData != null) {

                val users = reservationData.participantsId.mapNotNull { uid ->

                    val userDoc = Firebase.firestore
                        .collection("users")
                        .document(uid)
                        .get()
                        .await()

                    ContactUser(
                        uid = uid,
                        name = userDoc.getString("name") ?: "",
                        contactId = userDoc.getString("contactId") ?: "",
                        photo = userDoc.getString("photo") ?: ""
                    )
                }

                participants = users
            }
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

    val mapPoint = r.placeLocation?.let {
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
/**
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
        }*/

            Column(
                modifier = Modifier.padding(16.dp)
            ) {


                Text("Dirección: ${r.placeAddress}")
                Text("Cancha: ${r.placeFieldType}")
                Text("Precio total: $${r.placeFieldPrice}")
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = 1.dp,
                    color = Color.LightGray
                )
                Text("Creador de reserva")
                Text("${r.reservationCreatorName} - Tel: ${r.reservationCreatorPhone}")
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.LightGray
                )
                Text("Día: ${r.reservationDay}")
                Text("Hora: ${r.reservationHour} hs")
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.LightGray
                )
                Text("Participantes")
                Spacer(modifier = Modifier.height(8.dp))

                participants.forEach { participant ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        AsyncImage(
                            model = participant.photo,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(participant.name)
                            Text(
                                "#${participant.contactId}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Invitar contactos")
                }

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                ) {
                    Text("Abrir convocatoria")
                }

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                ),
                ) {
                    Text("Cancelar reserva")
                }

                /**
                Button(
                    onClick = {
                        scope.launch {
                            val relatedUsers = r.participantsId.map { participantId ->
                                mapOf(
                                    "userId" to participantId,
                                    "read" to false
                                )
                            }

                            Firebase.firestore
                                .collection("notifications")
                                .add(
                                    hashMapOf(
                                        "title" to "Nueva reserva",
                                        "message" to "Fuiste agregado a una reserva de ${r.placeFieldType} en ${r.placeName}",
                                        "reservationId" to r.id,
                                        "relatedUsers" to relatedUsers,
                                        "createdAt" to FieldValue.serverTimestamp()
                                    )
                                )
                                .await()
                        }
                    }
                ) {
                    Text("Generar notificación")
                }
                */

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
