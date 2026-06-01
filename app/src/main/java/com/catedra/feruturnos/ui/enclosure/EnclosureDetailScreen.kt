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
import androidx.compose.material3.SuggestionChip
import androidx.compose.ui.text.style.TextAlign

@Composable
fun EnclosureDetailScreen(
    enclosureId: String
) {
    var enclosure by remember { mutableStateOf<EnclosureItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(enclosureId) {
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
                enclosure = enclosure!!
            )
        }
    }
}
@Composable
fun EnclosureDetailContent(
    enclosure: EnclosureItem
) {
    var selectedField by remember { mutableStateOf(enclosure.fields.firstOrNull()) }
    var selectedDay by remember { mutableStateOf<String?>(null) }
    var selectedHour by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Header card ──────────────────────────────────────────
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

        // ── Selección de cancha ──────────────────────────────────
        Text(
            text = "Elegí una cancha",
            style = MaterialTheme.typography.titleMedium
        )

        // Grilla de 2 columnas — altura fija para que LazyVerticalGrid
        // conviva bien dentro del Column + verticalScroll
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
                    border = if (isSelected)
                        CardDefaults.outlinedCardBorder().copy(
                            width = 2.dp
                        )
                    else null,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(if (isSelected) 0.dp else 2.dp)
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

        // ── Días y horarios (solo si hay cancha seleccionada) ────
        selectedField?.let { field ->

            // Días — grilla de 3 columnas
            Text(
                text = "Día",
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

            //Horario
            Text(
                text = "Horario",
                style = MaterialTheme.typography.titleMedium
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(0.5.dp)
            ) {
                field.timeTable.forEach { hour ->
                    FilterChip(
                        selected = selectedHour == hour,
                        onClick = { selectedHour = hour },
                        label = {
                            Text(
                                text = "$hour:00",
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            // Resumen + confirmar
            if (selectedDay != null && selectedHour != null) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = "Resumen de reserva",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SummaryRow(label = "Cancha", value = "${field.fieldName} — ${field.type}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        SummaryRow(
                            label = "Día",
                            value = selectedDay!!.replaceFirstChar { it.uppercase() }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        SummaryRow(label = "Horario", value = "$selectedHour:00 hs")

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$${field.price}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Button(
                    onClick = { /* Crear reserva */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar reserva")
                }
            }
        }
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
        enclosure = previewEnclosure
    )
}

