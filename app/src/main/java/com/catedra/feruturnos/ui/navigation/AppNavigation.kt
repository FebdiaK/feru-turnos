package com.catedra.feruturnos.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.catedra.feruturnos.ui.home.HomeScreen
import com.catedra.feruturnos.ui.search.SearchScreen
import com.catedra.feruturnos.ui.profile.ProfileScreen
import com.catedra.feruturnos.ui.notifications.NotificationsScreen
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catedra.feruturnos.ui.contacts.ContactsScreen
import com.catedra.feruturnos.ui.reservation.ReservationDetailScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.catedra.feruturnos.ui.enclosure.EnclosureDetailScreen
import com.catedra.feruturnos.ui.profile.ProfileViewModel
import androidx.compose.runtime.collectAsState
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch

object Rutas {
    const val HOME = "Inicio"
    const val SEARCH = "Búsqueda"
    const val PROFILE = "Perfil"
    const val NOTIFICATIONS = "Notificaciones"
    const val RESERVATION_DETAIL = "reservation/{reservationId}"
    fun reservationDetail(id: String) = "reservation/$id"
    const val ENCLOSURE_DETAIL = "enclosure/{enclosureId}"
    fun enclosureDetail(id: String) = "enclosure/$id"
    const val CONTACTS = "contacts/{source}/{reservationId}"

    fun contactsFromProfile() = "contacts/profile/none"

    fun contactsFromReservation(reservationId: String) =
        "contacts/reservation/$reservationId"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AppNavigation(
    startDestination: String = Rutas.HOME,
    onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController()
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route
    val mostrarVolver = rutaActual != Rutas.HOME
    val scope = rememberCoroutineScope()

    val tituloActual = when (rutaActual) {
        Rutas.HOME -> "Inicio"
        Rutas.SEARCH -> "Búsqueda"
        Rutas.ENCLOSURE_DETAIL -> "Predio"
        Rutas.PROFILE -> "Perfil"
        Rutas.CONTACTS -> "Contactos"
        Rutas.NOTIFICATIONS -> "Notificaciones"
        Rutas.RESERVATION_DETAIL -> "Reserva"
        else -> ""
    }

    // Estado del permiso de ubicación para pasárselo a la SearchScreen
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloActual) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    if (mostrarVolver) {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                },

                actions = {
                    when (rutaActual) {
                        Rutas.PROFILE -> {
                            IconButton( onClick = { mostrarDialogoCerrarSesion = true} ) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = "Cerrar sesión"
                                )
                            }
                        }
                    }

                    IconButton(onClick = { navController.navigate(Rutas.NOTIFICATIONS) }) {
                        Icon(
                            modifier = if (rutaActual == Rutas.NOTIFICATIONS) {
                                Modifier.shadow(
                                    elevation = 12.dp,
                                    shape = CircleShape,
                                    spotColor = Color.Black
                                )
                            } else { Modifier },
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = {
                        if (rutaActual != Rutas.PROFILE) navController.navigate(Rutas.PROFILE)
                    }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Perfil"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar (containerColor = MaterialTheme.colorScheme.secondary){
                NavigationBarItem(
                    selected = rutaActual == Rutas.HOME,
                    onClick = {
                        navController.navigate(Rutas.HOME)
                    },
                    icon = { Icon( Icons.Default.Home, contentDescription = null,) },
                    label = { Text("Inicio") },
                    modifier = if (rutaActual == Rutas.HOME) {
                        Modifier.shadow(
                            elevation = 1.dp,
                            shape = CircleShape,
                            spotColor = Color.Black
                        )
                    } else {
                        Modifier
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color.Transparent
                    )
                )

                NavigationBarItem(
                    selected = rutaActual == Rutas.SEARCH,
                    onClick = {
                        navController.navigate(Rutas.SEARCH)
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Búsqueda") },
                    modifier = if (rutaActual == Rutas.SEARCH) {
                        Modifier.shadow(
                            elevation = 1.dp,
                            shape = CircleShape,
                            spotColor = Color.Black
                        )
                    } else {
                        Modifier
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color.Transparent
                    )
                )

                NavigationBarItem(
                    selected = rutaActual == Rutas.PROFILE,
                    onClick = {
                        navController.navigate(Rutas.PROFILE)
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil") },
                    modifier = if (rutaActual == Rutas.PROFILE) {
                        Modifier.shadow(
                            elevation = 1.dp,
                            shape = CircleShape,
                            spotColor = Color.Black
                        )
                    } else {
                        Modifier
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->

        if (mostrarDialogoCerrarSesion) {
            Dialog(
                onDismissRequest = { mostrarDialogoCerrarSesion = false }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp),
                    shape = RoundedCornerShape(0.dp),
                    color = Color.White
                ) {
                    Column (
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cerrar sesión",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.align(Alignment.Center)
                            )

                            IconButton(
                                onClick = { mostrarDialogoCerrarSesion = false },
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar"
                                )
                            }
                        }

                        Text(
                            text = "¿Seguro que querés cerrar sesión?",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 16.dp, bottom = 24.dp)
                        )

                        Button (
                            onClick = {
                                mostrarDialogoCerrarSesion = false
                                onCerrarSesion()
                            },
                                    modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Rutas.HOME) {
                HomeScreen(
                    onNavigateToSearch = {
                        navController.navigate(Rutas.SEARCH)
                    },
                    onNavigateToReservationDetail = { reservationId ->
                        navController.navigate(Rutas.reservationDetail(reservationId))
                    }
                )
            }

            composable(
                route = Rutas.RESERVATION_DETAIL
            ) { backStackEntry ->

                val reservationId =
                    backStackEntry.arguments?.getString("reservationId") ?: ""

                ReservationDetailScreen(
                    reservationId = reservationId,
                    onReservationCancelled = {
                        navController.popBackStack()
                    },
                    onNavigateToContacts = { id ->
                        navController.navigate(Rutas.contactsFromReservation(id))
                    }
                )
            }

            composable(Rutas.SEARCH) {
                SearchScreen(
                    onNavigateToReservation = { enclosure ->
                        navController.navigate(
                            Rutas.enclosureDetail(enclosure.id)
                        )
                    },
                    isPermissionGranted = locationPermissionState.status.isGranted,
                    onRequestPermission = { locationPermissionState.launchPermissionRequest() }
                )
            }

            composable(
                route = Rutas.ENCLOSURE_DETAIL
            ) { backStackEntry ->

                val enclosureId =
                    backStackEntry.arguments?.getString("enclosureId") ?: ""

                EnclosureDetailScreen(
                    enclosureId = enclosureId,
                    onReservationCreated = { reservationId ->
                        navController.navigate(Rutas.reservationDetail(reservationId)) {
                            popUpTo(Rutas.ENCLOSURE_DETAIL) { inclusive = true }
                        }
                    }

                )
            }

            composable(Rutas.PROFILE) {
                ProfileScreen(
                    onNavigateToContacts = {
                        navController.navigate(Rutas.contactsFromProfile())
                    }
                )
            }

            composable(Rutas.CONTACTS) { backStackEntry ->

                val source = backStackEntry.arguments?.getString("source") ?: "profile"
                val reservationId = backStackEntry.arguments?.getString("reservationId") ?: "none"

                val profileViewModel: ProfileViewModel = viewModel()
                val profileState by profileViewModel.profileState.collectAsState()

                ContactsScreen(
                    contacts = profileState.friends,
                    isInviting = source == "reservation",
                    onInviteClick = { selectedContacts ->

                        if (source == "reservation" && reservationId != "none") {

                            scope.launch {
                                val participantIds = selectedContacts.map { it.uid }

                                Firebase.firestore
                                    .collection("reservations")
                                    .document(reservationId)
                                    .update(
                                        "participantsId",
                                        FieldValue.arrayUnion(*participantIds.toTypedArray())
                                    )
                                    .await()

                                val relatedUsers = selectedContacts.map { contact ->
                                    hashMapOf(
                                        "userId" to contact.uid,
                                        "read" to false
                                    )
                                }

                                Firebase.firestore
                                    .collection("notifications")
                                    .add(
                                        hashMapOf(
                                            "title" to "Nueva invitación",
                                            "message" to "Fuiste invitado a una reserva.",
                                            "reservationId" to reservationId,
                                            "relatedUsers" to relatedUsers,
                                            "createdAt" to FieldValue.serverTimestamp()
                                        )
                                    )
                                    .await()

                                navController.popBackStack()
                            }
                        }
                    }
                )
            }

            composable(Rutas.NOTIFICATIONS) {
                NotificationsScreen(
                    onNotificationClick = { reservationId ->
                        navController.navigate(
                            Rutas.reservationDetail(reservationId)
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    AppNavigation(
        onCerrarSesion = {}
    )
}