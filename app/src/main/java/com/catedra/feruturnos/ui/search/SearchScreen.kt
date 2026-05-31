package com.catedra.feruturnos.ui.search

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import androidx.compose.foundation.lazy.items

@SuppressLint("MissingPermission")
@Composable
fun SearchScreen(
    onNavigateToReservation: (EnclosureItem) -> Unit,
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedEnclosure by remember { mutableStateOf<EnclosureItem?>(null) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var enclosures by remember { mutableStateOf<List<EnclosureItem>>(emptyList()) }
    var isLoadingEnclosures by remember { mutableStateOf(true) }

    var focusedEnclosure by remember {
        mutableStateOf<EnclosureItem?>(null)
    }

    val userIconDrawable = remember {
        androidx.core.content.ContextCompat.getDrawable(
            context,
            com.catedra.feruturnos.R.drawable.ic_user_location
        )
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            load(
                context,
                context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)
            )
        }
    }

    LaunchedEffect(Unit) {
        try {
            val result = Firebase.firestore
                .collection("enclosures")
                .get()
                .await()

            enclosures = result.documents.mapNotNull { doc ->
                val address = doc.getGeoPoint("address")

                if (address != null) {
                    EnclosureItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "Predio sin nombre",
                        location = GeoPoint(address.latitude, address.longitude)
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingEnclosures = false
        }
    }

    LaunchedEffect(isPermissionGranted) {
        if (!isPermissionGranted) return@LaunchedEffect

        try {
            val fusedClient =
                LocationServices.getFusedLocationProviderClient(context)

            val cached = fusedClient.lastLocation.await()

            if (cached != null) {
                userLocation = GeoPoint(
                    cached.latitude,
                    cached.longitude
                )
            }

            val request =
                com.google.android.gms.location.CurrentLocationRequest.Builder()
                    .setPriority(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
                    )
                    .setDurationMillis(5_000L)
                    .setMaxUpdateAgeMillis(0L)
                    .build()

            val fresh =
                fusedClient.getCurrentLocation(request, null).await()

            if (fresh != null) {
                userLocation = GeoPoint(
                    fresh.latitude,
                    fresh.longitude
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val nearestEnclosures = remember(
        enclosures,
        userLocation,
        searchQuery
    ) {
        val filtered = enclosures.filter { enclosure ->
            enclosure.name.contains(searchQuery, ignoreCase = true)
        }

        if (userLocation != null) {
            filtered
                .sortedBy { enclosure ->
                    calculateDistance(userLocation!!, enclosure.location)
                }
                .take(5)
        } else {
            filtered.take(5)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                !isPermissionGranted -> {
                    RequestLocationPermissionBox(
                        onRequestPermission = onRequestPermission
                    )
                }

                userLocation == null -> {
                    LoadingLocationBox()
                }

                else -> {
                    SearchMap(
                        userLocation = userLocation!!,
                        enclosures = nearestEnclosures,
                        selectedEnclosure = selectedEnclosure,
                        focusedEnclosure = focusedEnclosure,
                        onSelectedEnclosureChange = { enclosure ->
                            selectedEnclosure = enclosure
                        },
                        userIconDrawable = userIconDrawable
                    )
                }
            }

            SearchTextFieldCard(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )
        }

        SearchResultsSection(
            modifier = Modifier.weight(1f),
            userLocation = userLocation,
            nearestEnclosures = nearestEnclosures,
            isLoadingEnclosures = isLoadingEnclosures,
            onEnclosureClick = { enclosure ->
                selectedEnclosure = enclosure
                focusedEnclosure = enclosure
            }
        )
    }

    selectedEnclosure?.let { enclosure ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            SelectedEnclosureCard(
                enclosure = enclosure,
                userLocation = userLocation,
                onReserve = {
                    onNavigateToReservation(enclosure)
                },
                onDismiss = {
                    selectedEnclosure = null
                }
            )
        }
    }
}

@Composable
fun RequestLocationPermissionBox(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📍",
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Para ver los predios más cercanos necesitás habilitar el acceso a tu ubicación.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRequestPermission
            ) {
                Text("Habilitar ubicación")
            }
        }
    }
}

@Composable
fun LoadingLocationBox() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Obteniendo tu ubicación...",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SearchTextFieldCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text("Buscar predio...")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@Composable
fun SearchResultsSection(
    modifier: Modifier = Modifier,
    userLocation: GeoPoint?,
    nearestEnclosures: List<EnclosureItem>,
    isLoadingEnclosures: Boolean,
    onEnclosureClick: (EnclosureItem) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7))
    ) {
        Text(
            text = if (userLocation != null) {
                "5 predios más cercanos a vos"
            } else {
                "Predios disponibles"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 12.dp
            )
        )

        when {
            isLoadingEnclosures -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            nearestEnclosures.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontraron predios")
                }
            }

            else -> {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(nearestEnclosures) { enclosure ->
                        EnclosureRowItem(
                            enclosure = enclosure,
                            userLocation = userLocation,
                            onClick = {
                                onEnclosureClick(enclosure)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen(
        onNavigateToReservation = { _ -> },
        isPermissionGranted = true,
        onRequestPermission = {}
    )
}