package com.catedra.feruturnos.ui.reservation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.catedra.feruturnos.R
import com.catedra.feruturnos.ui.contacts.ContactUser
import com.catedra.feruturnos.ui.home.Reservation
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import com.utsman.osmandcompose.rememberCameraState
import com.utsman.osmandcompose.rememberMarkerState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint

@Composable
fun ReservationDetailScreen(
    reservationId: String,
    onReservationCancelled: (String) -> Unit,
    onNavigateToContacts: (String) -> Unit
) {
    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var participants by remember { mutableStateOf<List<ContactUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isCancelling by remember { mutableStateOf(false) }
    var mostrarDialogoCancelarReserva by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            load(
                context,
                context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)
            )
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
                        photo = userDoc.getString("photo") ?: "",
                        celphone = userDoc.getString("celphone") ?: ""
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
            text = stringResource(R.string.no_se_encontro_la_reserva),
            modifier = Modifier.padding(36.dp)
        )
        return
    }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isCreator = currentUserId == r.creatorId
    val isCurrentUserParticipant = r.participantsId.contains(currentUserId)

    val mapPoint = r.placeLocation?.let {
        GeoPoint(it.latitude, it.longitude)
    }

    val titleCancelled = stringResource(R.string.reserva_cancelada)
    val messageCancelled = stringResource(
        R.string.mensaje_reserva_cancelada,
        r.placeFieldType,
        r.placeName
    )

    val reservationDeletedSuccessfully =
        stringResource(R.string.reserva_eliminada_correctamente)

    val leaveSuccessfully =
        stringResource(R.string.baja_ejecutada_correctamente)

    val cancelReservationText =
        stringResource(R.string.cancelar_reserva)

    val unsubscribeText =
        stringResource(R.string.darse_de_baja)

    val tituloDialogoCancelar =
        if (isCreator) cancelReservationText else unsubscribeText

    val mensajeDialogoCancelar =
        if (isCreator) {
            stringResource(R.string.seguro_que_queres_cancelar_reserva)
        } else {
            stringResource(R.string.seguro_que_queres_darte_de_baja)
        }

    if (mostrarDialogoCancelarReserva) {
        AlertDialog(
            onDismissRequest = {
                if (!isCancelling) mostrarDialogoCancelarReserva = false
            },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = tituloDialogoCancelar,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = mensajeDialogoCancelar,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoCancelarReserva = false },
                    enabled = !isCancelling
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isCancelling = true

                                if (isCreator) {
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
                                                "title" to titleCancelled,
                                                "message" to messageCancelled,
                                                "reservationId" to r.id,
                                                "relatedUsers" to relatedUsers,
                                                "createdAt" to FieldValue.serverTimestamp()
                                            )
                                        )
                                        .await()

                                    Firebase.firestore
                                        .collection("reservations")
                                        .document(reservationId)
                                        .delete()
                                        .await()

                                    mostrarDialogoCancelarReserva = false
                                    onReservationCancelled(reservationDeletedSuccessfully)
                                } else {
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid

                                    if (uid == null) {
                                        isCancelling = false
                                        return@launch
                                    }

                                    Firebase.firestore
                                        .collection("reservations")
                                        .document(reservationId)
                                        .update(
                                            mapOf(
                                                "participantsId" to FieldValue.arrayRemove(uid)
                                            )
                                        )
                                        .await()

                                    mostrarDialogoCancelarReserva = false
                                    onReservationCancelled(leaveSuccessfully)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                isCancelling = false
                            }
                        }
                    },
                    enabled = !isCancelling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCreator) Color.Red else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.confirmar))
                    }
                }
            }
        )
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
                    .height(230.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 16.dp
                        )
                ) {
                    Text(
                        text = r.placeName,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = r.placeAddress,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        title = stringResource(R.string.cancha),
                        value = r.placeFieldType
                    )

                    DetailRow(
                        icon = Icons.Default.AttachMoney,
                        title = stringResource(R.string.precio_total),
                        value = "$${r.placeFieldPrice}"
                    )

                    DetailRow(
                        icon = Icons.Default.CalendarMonth,
                        title = stringResource(R.string.fecha_y_hora),
                        value = "${r.reservationDay} - ${r.reservationHour} hs"
                    )

                    DetailRow(
                        icon = Icons.Default.Person,
                        title = stringResource(R.string.creador),
                        value = "${r.reservationCreatorName} · Tel: ${r.reservationCreatorPhone}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.participantes),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (participants.isEmpty()) {
                Text(
                    text = stringResource(R.string.todavia_no_hay_participantes_invitados),
                    color = Color.Gray
                )
            } else {
                participants.forEach { participant ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = participant.photo,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = participant.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "#${participant.contactId}",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                if (participant.celphone.isNotBlank()) {
                                    Text(
                                        text = participant.celphone,
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onNavigateToContacts(reservationId) },
                enabled = isCreator || isCurrentUserParticipant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(stringResource(R.string.invitar_contactos))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    mostrarDialogoCancelarReserva = true
                },
                enabled = !isCancelling && (isCreator || isCurrentUserParticipant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                val deleteTextBtn =
                    if (isCreator) cancelReservationText else unsubscribeText

                if (isCancelling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(deleteTextBtn)
                }
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
        modifier = modifier.clipToBounds()
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

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}