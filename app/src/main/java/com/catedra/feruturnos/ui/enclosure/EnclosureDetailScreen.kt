package com.catedra.feruturnos.ui.enclosure

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

data class EnclosureDetail(
    val id: String = "",
    val name: String = "",
    val address: GeoPoint? = null
)

@Composable
fun EnclosureDetailScreen(
    enclosureId: String
) {
    var enclosure by remember { mutableStateOf<EnclosureDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(enclosureId) {
        try {
            val doc = Firebase.firestore
                .collection("enclosures")
                .document(enclosureId)
                .get()
                .await()

            enclosure = EnclosureDetail(
                id = doc.id,
                name = doc.getString("name") ?: "Predio sin nombre",
                address = doc.getGeoPoint("address")
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

    val data = enclosure

    if (data == null) {
        Text(
            text = "No se encontró el predio",
            modifier = Modifier.padding(24.dp)
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = data.name,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("ID: ${data.id}")

        if (data.address != null) {
            Text("Latitud: ${data.address.latitude}")
            Text("Longitud: ${data.address.longitude}")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Próximo paso: crear reserva
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reservar")
        }
    }
}