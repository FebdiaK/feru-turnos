package com.catedra.feruturnos.ui.enclosure

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import com.catedra.feruturnos.ui.search.FieldItem
import com.catedra.feruturnos.ui.search.EnclosureItem
import org.osmdroid.util.GeoPoint
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AssistChip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import com.catedra.feruturnos.ui.reservation.ReservationRepository
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
                                id = (field["id"] as? Long)?.toInt() ?: 0,
                                fieldName = field["fieldName"] as? String ?: "",
                                type = field["type"] as? String ?: "",
                                price = field["price"] as? Long ?: 0,
                                days = field["days"] as? List<String>
                                    ?: emptyList(),
                                timeTable = field["timeTable"] as? List<String>
                                    ?: emptyList(),
                                description = field["description"] as? String ?: ""
                            )
                        }
                        ?: emptyList()

                enclosure = EnclosureItem(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    address = doc.getString("address") ?: "",
                    phone = doc.getLong("phone") ?: 0,
                    amenities = doc.get("amenities") as? List<String>
                        ?: emptyList(),
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
                text = "No se encontró el predio",
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
    var selectedDay   by remember { mutableStateOf<String?>(null) }
    var selectedHour  by remember { mutableStateOf<String?>(null) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var isLoading         by remember { mutableStateOf(false) }

    val isPreview = LocalInspectionMode.current
    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    var reservationName by remember { mutableStateOf("Reserva de: ...") }

    LaunchedEffect(Unit) {
        val uid = auth?.currentUser?.uid
        android.util.Log.d("ENCLOSURE_DEBUG", "uid en composable: $uid")
        if (uid == null) return@LaunchedEffect
        try {
            val userQuery = Firebase.firestore
                .collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .await()
            android.util.Log.d("ENCLOSURE_DEBUG", "docs encontrados: ${userQuery.documents.size}")
            android.util.Log.d("ENCLOSURE_DEBUG", "name: ${userQuery.documents.firstOrNull()?.getString("name")}")
            val name = userQuery.documents.firstOrNull()?.getString("name") ?: "usuario"
            reservationName = "Reserva de: $name"
        } catch (e: Exception) {
            android.util.Log.e("ENCLOSURE_DEBUG", "error: ${e.message}")
            reservationName = "Reserva de: usuario"
        }
    }

    LaunchedEffect(Unit) {
        val uid = auth?.currentUser?.uid
        android.util.Log.d("ENCLOSURE_DEBUG", "uid en composable: $uid")
        if (uid == null) return@LaunchedEffect
        try {
            // Traé TODOS los docs de users y buscá a mano
            val allUsers = Firebase.firestore
                .collection("users")
                .get()
                .await()
            android.util.Log.d("ENCLOSURE_DEBUG", "total users: ${allUsers.documents.size}")
            allUsers.documents.forEach { doc ->
                android.util.Log.d("ENCLOSURE_DEBUG", "doc id: ${doc.id} | uid field: ${doc.getString("uid")} | name: ${doc.getString("name")}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ENCLOSURE_DEBUG", "error: ${e.message}")
        }
    }


    // Dialog de confirmación
    if (showConfirmDialog && selectedField != null && selectedDay != null && selectedHour != null) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showConfirmDialog = false },
            title = { Text("Confirmar reserva") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryRow("Cancha",  "${selectedField!!.fieldName} — ${selectedField!!.type}")
                    SummaryRow("Día",     selectedDay!!.replaceFirstChar { it.uppercase() })
                    SummaryRow("Horario", "$selectedHour:00 hs")
                    SummaryRow("Total",   "$${selectedField!!.price}")
                    HorizontalDivider()
                    OutlinedTextField(
                        value = reservationName,
                        onValueChange = { reservationName = it },
                        label = { Text("Nombre de la reserva") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val id = repository.createReservation(
                                    enclosure       = enclosure,
                                    field           = selectedField!!,
                                    selectedDay     = selectedDay!!,
                                    selectedHour    = selectedHour!!,
                                    reservationName = reservationName
                                )
                                showConfirmDialog = false
                                snackbarHostState.showSnackbar("¡Reserva creada correctamente!")
                                onReservationCreated(id)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    "Error: ${e.message ?: "No se pudo crear la reserva"}"
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
                        Text("Confirmar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isLoading,
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("Cancelar")
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
            // ── Header card ──────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
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
                                AssistChip(onClick = {}, enabled = false, label = { Text(amenity) })
                            }
                        }
                    }
                }
            }

            // ── Selección de cancha ──────────────────────────────
            Text("Elegí una cancha", style = MaterialTheme.typography.titleMedium)

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
                            selectedDay   = null
                            selectedHour  = null
                        },
                        modifier = Modifier.fillMaxWidth().height(fieldCardHeight),
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(width = 2.dp) else null,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(if (isSelected) 0.dp else 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(field.fieldName, style = MaterialTheme.typography.labelLarge)
                                Text(
                                    field.type,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "$${field.price}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // ── Días, horarios y resumen ─────────────────────────
            selectedField?.let { field ->
                Text("Día", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.5.dp)
                ) {
                    field.days.forEach { day ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick  = { selectedDay = day },
                            label    = {
                                Text(
                                    day.replaceFirstChar { it.uppercase() },
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                Text("Horario", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.5.dp)
                ) {
                    field.timeTable.forEach { hour ->
                        FilterChip(
                            selected = selectedHour == hour,
                            onClick  = { selectedHour = hour },
                            label    = {
                                Text(
                                    "$hour:00",
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
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resumen de reserva", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            SummaryRow("Cancha",  "${field.fieldName} — ${field.type}")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            SummaryRow("Día",     selectedDay!!.replaceFirstChar { it.uppercase() })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            SummaryRow("Horario", "$selectedHour:00 hs")
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "$${field.price}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showConfirmDialog = true },  // ← abre el dialog
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirmar reserva")
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
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


private val previewEnclosure = EnclosureItem(
    id = "1",
    name = "Padel Vida Viva",
    address = "Río Salado 5228, Ezpeleta",
    phone = 42348888,
    location = GeoPoint(
        -34.7493861,
        -58.2497394
    ),
    amenities = listOf(
        "Buffet",
        "Estacionamiento",
        "Vestuarios"
    ),
    fields = listOf(
        FieldItem(
            id = 0,
            fieldName = "Cancha número 1",
            type = "Paddel",
            price = 5000,
            days = listOf(
                "lunes",
                "martes",
                "miercoles",
                "sábado",
                "domingo"
            ),
            timeTable = listOf(
                "10",
                "11",
                "14",
                "15",
                "16",
                "17"
            ),
            description = "Cancha techada con iluminación LED"
        )
    )
)

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun EnclosureDetailContentPreview() {
    EnclosureDetailContent(
        enclosure = previewEnclosure,
        onReservationCreated = {}
    )
}

