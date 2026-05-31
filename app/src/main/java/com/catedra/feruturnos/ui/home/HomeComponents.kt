package com.catedra.feruturnos.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

@Composable
fun CurrentReservationSection(
    title: String,
    reservations: List<Reservation>,
    onReservationClick: (Reservation) -> Unit
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

            if (reservations.isEmpty()) {
                Text("Sin reservas actuales")
            } else {
                reservations.forEach { reservation ->
                    ReservationsItem(
                        reservation = reservation,
                        onClick = {
                            onReservationClick(reservation)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReservationsItem(reservation: Reservation, onClick: () -> Unit) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
    )

    Column(
        modifier = Modifier
            .clickable { onClick() }
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

        Text("${reservation.reservationDay} a las ${reservation.reservationHour} hs.")
        Text(reservation.placeName)
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

            Text(text = "Explorá las canchas más cercanas y reservá tu turno al instante.")

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
    var noEncontrado by remember { mutableStateOf(false) }

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    var friends by remember {
        mutableStateOf<List<ContactUser>>(emptyList())
    }

    LaunchedEffect(currentUid) {
        if (currentUid != null) {
            val doc = db.collection("users")
                .document(currentUid)
                .get()
                .await()

            val friendsRaw = doc.get("friends") as? List<Map<String, Any>> ?: emptyList()

            friends = friendsRaw.map {
                ContactUser(
                    uid = it["uid"] as? String ?: "",
                    name = it["name"] as? String ?: "",
                    contactId = it["contactId"] as? String ?: "",
                    photo = it["photo"] as? String ?: ""
                )
            }
        }
    }

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
                onValueChange = {
                    contactId = it
                    noEncontrado = false
                    usuarioEncontrado = null
                },
                label = { Text("Ingrese el id de contacto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            val currentUid = FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid

            Button(
                onClick = {

                    isLoading = true

                    db.collection("users")
                        .whereEqualTo("contactId", contactId)
                        .get()
                        .addOnSuccessListener { result ->

                            val document = result.documents.firstOrNull {
                                it.id != currentUid
                            }

                            usuarioEncontrado =
                                if (document != null) {
                                    noEncontrado = false

                                    ContactUser(
                                        uid = document.getString("uid") ?: document.id,
                                        name = document.getString("name") ?: "",
                                        contactId = document.getString("contactId") ?: "",
                                        photo = document.getString("photo") ?: ""
                                    )
                                } else {
                                    noEncontrado = true
                                    null
                                }

                            isLoading = false
                        }
                        .addOnFailureListener {
                            noEncontrado = true
                            usuarioEncontrado = null
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

                    val yaEsAmigo = friends.any { it.uid == usuario.uid }

                    Button(
                        onClick = {
                            currentUid?.let { uid ->

                                val friendData = mapOf(
                                    "uid" to usuario.uid,
                                    "name" to usuario.name,
                                    "contactId" to usuario.contactId,
                                    "photo" to usuario.photo
                                )

                                if (yaEsAmigo) {
                                    db.collection("users")
                                        .document(uid)
                                        .update("friends", FieldValue.arrayRemove(friendData))

                                    friends = friends.filter { it.uid != usuario.uid }
                                } else {
                                    db.collection("users")
                                        .document(uid)
                                        .update("friends", FieldValue.arrayUnion(friendData))

                                    friends = friends + usuario
                                }
                            }
                        },
                        modifier = Modifier.width(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (yaEsAmigo) Color.Red else Color.Green
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (yaEsAmigo) "×" else "+")
                    }
                }
            }
            if (noEncontrado) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "No se ha encontrado un contacto con ese ID",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}