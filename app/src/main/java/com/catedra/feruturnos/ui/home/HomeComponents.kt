package com.catedra.feruturnos.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip

@Composable
fun CurrentReservationSection(
    title: String,
    reservations: List<Reservation>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = Color.Black
            ),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            reservations.forEach { reservation ->
                ReservationsItem(reservation)
            }
        }
    }
}

@Composable
fun ReservationsItem(reservation: Reservation) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black,
                spotColor = MaterialTheme.colorScheme.primary
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
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

@Composable
fun SearchBannerSection(
    title: String,
    onExploreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = Color.Black
            ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier
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
                text = "Explorá las canchas más cercanas y reservá tu turno al instante.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onExploreClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buscar")
            }
        }
    }
}

@Composable
fun ConnectPeopleSection(
    title: String,
    btnText: String
) {

    var contactId by remember { mutableStateOf("") }

    var usuarioEncontrado by remember {
        mutableStateOf<ContactUser?>(null)
    }

    var isLoading by remember { mutableStateOf(false) }

    val db = Firebase.firestore

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = Color.Black
            ),
        shape = RoundedCornerShape(0.dp)
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

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = contactId,
                onValueChange = { contactId = it },
                label = { Text("Ingrese el id de contacto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {

                    isLoading = true

                    db.collection("users")
                        .whereEqualTo("contactId", contactId)
                        .get()
                        .addOnSuccessListener { result ->

                            val document = result.documents.firstOrNull()

                            usuarioEncontrado =
                                if (document != null) {
                                    ContactUser(
                                        name = document.getString("name") ?: "",
                                        contactId = document.getString("contactId") ?: "",
                                        photo = document.getString("photo") ?: ""
                                    )
                                } else {
                                    null
                                }

                            isLoading = false
                        }
                },
                enabled = contactId.length == 6,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(btnText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            usuarioEncontrado?.let { usuario ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    AsyncImage(
                        model = usuario.photo,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = usuario.name,
                            fontWeight = FontWeight.Bold
                        )

                        Text("#${usuario.contactId}")
                    }

                    Button(
                        onClick = {},
                        modifier = Modifier.width(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("+")
                    }
                }
            }
        }
    }
}