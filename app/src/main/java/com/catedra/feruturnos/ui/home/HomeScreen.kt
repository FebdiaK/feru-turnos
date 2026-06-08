package com.catedra.feruturnos.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.catedra.feruturnos.R

@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToReservationDetail: (String) -> Unit = {}
) {
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val uid = Firebase.auth.currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            try {
                val result = Firebase.firestore
                    .collection("reservations")
                    .whereArrayContains("participantsId", uid)
                    .get()
                    .await()

                reservations = result.documents.mapNotNull { doc ->
                    doc.toObject(Reservation::class.java)?.copy(
                        id = doc.id
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        } else {
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

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CurrentReservationSection(
                title = stringResource(R.string.reservas_actuales),
                reservations = reservations,
                onReservationClick = { reservation ->
                    onNavigateToReservationDetail(reservation.id)
                }
            )
        }

        item {
            SearchBannerSection(
                title = stringResource(R.string.buscando_donde_jugar),
                onExploreClick = onNavigateToSearch
            )
        }

        item {
            ConnectPeopleSection(
                title = stringResource(R.string.conectar_con_personas),
                btnText = stringResource(R.string.buscar)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}