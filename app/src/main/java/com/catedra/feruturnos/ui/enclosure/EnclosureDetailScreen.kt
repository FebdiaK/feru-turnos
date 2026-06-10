package com.catedra.feruturnos.ui.enclosure

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.catedra.feruturnos.R
import com.catedra.feruturnos.ui.reservation.ReservationRepository
import com.catedra.feruturnos.ui.search.EnclosureItem
import com.catedra.feruturnos.ui.search.FieldItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint

private val CardShape       = RoundedCornerShape(16.dp)
private val ButtonShape     = RoundedCornerShape(14.dp)
private val ButtonHeight    = 52.dp
private val ElevationLow    = 2.dp
private val ElevationMedium = 4.dp

@Composable
fun EnclosureDetailScreen(
    enclosureId: String,
    onReservationCreated: (reservationId: String) -> Unit = {}
) {
    var enclosure by remember { mutableStateOf<EnclosureItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val isPreview = LocalInspectionMode.current

    LaunchedEffect(enclosureId) {
        if (isPreview) {
            isLoading = false
            return@LaunchedEffect
        }

        try {
            val doc = Firebase.firestore
                .collection("enclosures")
                .document(enclosureId)
                .get()
                .await()

            val location = doc.getGeoPoint("location")

            if (location != null) {
                val fields =
                    (doc.get("fields") as? List<Map<String, Any>>)
                        ?.map { field ->
                            FieldItem(
                                id = field["id"] as? String ?: "",
                                fieldName = field["fieldName"] as? String ?: "",
                                type = field["type"] as? String ?: "",
                                price = field["price"] as? Long ?: 0,
                                days = field["days"] as? List<String> ?: emptyList(),
                                timeTable = field["timeTable"] as? List<String> ?: emptyList(),
                                description = field["description"] as? String ?: ""
                            )
                        }
                        ?: emptyList()

                enclosure = EnclosureItem(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    address = doc.getString("address") ?: "",
                    phone = doc.getLong("phone") ?: 0,
                    amenities = doc.get("amenities") as? List<String> ?: emptyList(),
                    fields = fields,
                    location = GeoPoint(
                        location.latitude,
                        location.longitude
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        enclosure == null -> {
            Text(
                text = stringResource(R.string.no_se_encontro_el_predio),
                modifier = Modifier.padding(24.dp)
            )
        }

        else -> {
            EnclosureDetailContent(
                enclosure = enclosure!!,
                onReservationCreated = onReservationCreated
            )
        }
    }
}

@Composable
fun EnclosureDetailContent(
    enclosure: EnclosureItem,
    onReservationCreated: (reservationId: String) -> Unit = {}
) {
    val repository = remember { ReservationRepository() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedField by remember { mutableStateOf(enclosure.fields.firstOrNull()) }
    var selectedDay by remember { mutableStateOf<String?>(null) }
    var selectedHour by remember { mutableStateOf<String?>(null) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val isPreview = LocalInspectionMode.current
    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    val context = androidx.compose.ui.platform.LocalContext.current

    val userFallbackText = stringResource(R.string.usuario)
    val reservationOfText = stringResource(R.string.reserva_de)
    val reservationCreatedText = stringResource(R.string.reserva_creada_correctamente)
    val createReservationErrorText = stringResource(R.string.error_crear_reserva)
    val errorText = stringResource(R.string.error, createReservationErrorText)
    var reservedSlots by remember { mutableStateOf<Set<String>>(emptySet()) }
    var reservationName by remember {
        mutableStateOf("$reservationOfText: ...")
    }
    val errorTemplate = stringResource(R.string.error)

    LaunchedEffect(Unit) {
        val uid = auth?.currentUser?.uid
        if (uid == null) return@LaunchedEffect

        try {
            val userQuery = Firebase.firestore
                .collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .await()

            val name = userQuery.documents.firstOrNull()?.getString("name")
                ?: userFallbackText

            reservationName = "$reservationOfText: $name"
        } catch (e: Exception) {
            reservationName = "$reservationOfText: $userFallbackText"
        }
    }

    LaunchedEffect(enclosure.id) {
        if (isPreview) return@LaunchedEffect

        try {
            val snapshot = Firebase.firestore
                .collection("reservations")
                .whereEqualTo("enclosureId", enclosure.id)
                .get()
                .await()

            reservedSlots = snapshot.documents.mapNotNull { doc ->
                val fieldId = doc.getString("fieldId") ?: return@mapNotNull null
                val day = doc.getString("reservationDay") ?: return@mapNotNull null
                val hour = doc.getString("reservationHour")
                    ?: doc.getString("reservationTime")
                    ?: return@mapNotNull null
                "$fieldId|$day|$hour"
            }.toSet()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (showConfirmDialog && selectedField != null && selectedDay != null && selectedHour != null) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showConfirmDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = stringResource(R.string.confirmar_reserva),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryRow(
                        label = stringResource(R.string.cancha),
                        value = "${selectedField!!.fieldName} — ${selectedField!!.type}"
                    )
                    SummaryRow(
                        label = stringResource(R.string.dia),
                        value = selectedDay!!.replaceFirstChar { it.uppercase() }
                    )
                    SummaryRow(
                        label = stringResource(R.string.horario),
                        value = "$selectedHour:00 hs"
                    )
                    SummaryRow(
                        label = stringResource(R.string.total),
                        value = "$${selectedField!!.price}"
                    )

                    HorizontalDivider()

                    OutlinedTextField(
                        value = reservationName,
                        onValueChange = { reservationName = it },
                        label = { Text(stringResource(R.string.nombre_reserva)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isLoading,
                    shape = ButtonShape,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val id = repository.createReservation(
                                    context = context,
                                    enclosure = enclosure,
                                    field = selectedField!!,
                                    selectedDay = selectedDay!!,
                                    selectedHour = selectedHour!!,
                                    reservationName = reservationName
                                )
                                mostrarNotificacionReservaCreada(context, id)
                                showConfirmDialog = false
                                snackbarHostState.showSnackbar(reservationCreatedText)
                                onReservationCreated(id)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    e.message?.let { msg -> errorTemplate.format(msg) } ?: errorText
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.confirmar))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isLoading,
                    onClick = { showConfirmDialog = false }
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                elevation = cardElevation(ElevationMedium)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = enclosure.name,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f)
                        )

                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = "📞 ${enclosure.phone}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "📍 ${enclosure.address}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (enclosure.amenities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            enclosure.amenities.forEach { amenity ->
                                AssistChip(
                                    onClick = {},
                                    enabled = false,
                                    label = { Text(amenity) }
                                )
                            }
                        }
                    }
                }
            }

                Text(
                text = stringResource(R.string.elegi_una_cancha),
                style = MaterialTheme.typography.titleMedium
            )

            val fieldCardHeight = 90.dp
            val gridRows = Math.ceil(enclosure.fields.size / 2.0).toInt()

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fieldCardHeight * gridRows + 8.dp * (gridRows - 1)),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(enclosure.fields) { field ->
                    val isSelected = selectedField?.id == field.id

                    Card(
                        onClick = {
                            selectedField = field
                            selectedDay = null
                            selectedHour = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(fieldCardHeight),
                        shape = CardShape,
                        border = if (isSelected) {
                            CardDefaults.outlinedCardBorder().copy(width = 2.dp)
                        } else {
                            null
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        elevation = cardElevation(
                            if (isSelected) 0.dp else 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = field.fieldName,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = field.type,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "$${field.price}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            selectedField?.let { field ->
                Text(
                    text = stringResource(R.string.dia),
                    style = MaterialTheme.typography.titleMedium
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.5.dp)
                ) {
                    field.days.forEach { day ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            label = {
                                Text(
                                    text = day.replaceFirstChar { it.uppercase() },
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.horario),
                    style = MaterialTheme.typography.titleMedium
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.5.dp)
                ) {
                    field.timeTable.forEach { hour ->
                        val slotKey = "${field.id}|$selectedDay|$hour"
                        val isReserved = selectedDay != null && reservedSlots.contains(slotKey)

                        FilterChip(
                            selected = selectedHour == hour,
                            enabled = !isReserved,
                            onClick = { if (!isReserved) selectedHour = hour },
                            label = {
                                Text(
                                    text = if (isReserved) "$hour:00 - Reservado" else "$hour:00",
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                if (selectedDay != null && selectedHour != null) {
                             Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        elevation = cardElevation(ElevationMedium)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.resumen_reserva),
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SummaryRow(
                                label = stringResource(R.string.cancha),
                                value = "${field.fieldName} — ${field.type}"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            SummaryRow(
                                label = stringResource(R.string.dia),
                                value = selectedDay!!.replaceFirstChar { it.uppercase() }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            SummaryRow(
                                label = stringResource(R.string.horario),
                                value = "$selectedHour:00 hs"
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            SummaryRow(
                                label = stringResource(R.string.total),
                                value = "$${field.price}"
                            )
                        }
                    }

                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ButtonHeight),
                        shape = ButtonShape
                    ) {
                        Text(stringResource(R.string.confirmar_reserva))
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun mostrarNotificacionReservaCreada(
    context: android.content.Context,
    reservationId: String
) {
    val channelId = "feru_reservas"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = android.app.NotificationChannel(
            channelId,
            context.getString(R.string.reservas),
            android.app.NotificationManager.IMPORTANCE_HIGH
        )
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    val intent = android.content.Intent(
        context,
        com.catedra.feruturnos.MainActivity::class.java
    ).apply {
        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("openNotifications", true)
    }

    val pendingIntent = android.app.PendingIntent.getActivity(
        context,
        reservationId.hashCode(),
        intent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                android.app.PendingIntent.FLAG_IMMUTABLE
    )

    val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.logo_medium)
        .setContentTitle(context.getString(R.string.reserva_creada_con_exito))
        .setContentText(context.getString(R.string.toca_para_ver_notificaciones))
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    val hasNotificationPermission =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

    if (hasNotificationPermission) {
        androidx.core.app.NotificationManagerCompat.from(context)
            .notify(reservationId.hashCode(), notification)
    }
}

private val previewEnclosure = EnclosureItem(
    id = "1",
    name = "Padel Vida Viva",
    address = "Río Salado 5228, Ezpeleta",
    phone = 42348888,
    location = GeoPoint(-34.7493861, -58.2497394),
    amenities = listOf("Buffet", "Estacionamiento", "Vestuarios"),
    fields = listOf(
        FieldItem(
            id = "cancha_1",
            fieldName = "Cancha número 1",
            type = "Paddel",
            price = 5000,
            days = listOf("lunes", "martes", "miercoles", "sábado", "domingo"),
            timeTable = listOf("10", "11", "14", "15", "16", "17"),
            description = "Cancha techada con iluminación LED"
        )
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EnclosureDetailContentPreview() {
    EnclosureDetailContent(
        enclosure = previewEnclosure,
        onReservationCreated = {}
    )
}