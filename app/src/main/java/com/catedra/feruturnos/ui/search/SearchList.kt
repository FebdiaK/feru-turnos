package com.catedra.feruturnos.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.osmdroid.util.GeoPoint
import androidx.compose.ui.res.stringResource
import com.catedra.feruturnos.R

@Composable
fun EnclosureRowItem(
    enclosure: EnclosureItem,
    userLocation: GeoPoint?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = enclosure.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = enclosure.address,
                style = MaterialTheme.typography.labelMedium
            )


            if (userLocation != null) {
                val distance = calculateDistance(
                    userLocation,
                    enclosure.location
                )

                Text(
                    text = stringResource(
                        R.string.a_x_km_de_tu_ubicacion,
                        distance
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SelectedEnclosureCard(
    enclosure: EnclosureItem,
    userLocation: GeoPoint?,
    onReserve: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = enclosure.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = "✕",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = enclosure.address,
                style = MaterialTheme.typography.bodySmall
            )

            if (userLocation != null) {
                val distance = calculateDistance(
                    userLocation,
                    enclosure.location
                )

                Text(
                    text = stringResource(
                        R.string.a_x_km_de_tu_ubicacion,
                        distance
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))


            Text(
                text = stringResource(R.string.servicios),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            if (enclosure.amenities.isEmpty()) {
                Text(
                    text = stringResource(R.string.sin_servicios_informados),
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                enclosure.amenities.forEach { amenity ->
                    Text(
                        text = "✓ ${amenity.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.canchas),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            enclosure.fields.forEach { field ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = field.fieldName.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = " - ${field.type.replaceFirstChar { it.uppercase() }} - $${field.price}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onReserve,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.ir_a_reservar))
            }
        }
    }
}

private val previewEnclosure = EnclosureItem(
    id = "1",
    name = "Padel Vida Viva",
    address = "Río Salado 5228, Ezpeleta",
    location = GeoPoint(
        -34.7493861,
        -58.2497394
    ),
    phone = 42348888,
    amenities = listOf(
        "Estacionamiento",
        "Buffet",
        "Vestuarios"
    ),
    fields = listOf(
        FieldItem(
            id = "cancha_1",
            fieldName = "Cancha número 1",
            type = "paddel",
            price = 5000,
            days = listOf(
                "sabado",
                "domingo"
            ),
            timeTable = listOf(
                "10",
                "11",
                "14",
                "15"
            )
        )
    )
)

private val previewUserLocation = GeoPoint(
    -34.82,
    -58.29
)


@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun SearchComponentsPreview() {
    Column {
        EnclosureRowItem(
            enclosure = previewEnclosure,
            userLocation = previewUserLocation,
            onClick = {}
        )

        SelectedEnclosureCard(
            enclosure = previewEnclosure,
            userLocation = previewUserLocation,
            onReserve = {},
            onDismiss = {}
        )
    }
}