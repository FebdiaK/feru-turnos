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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.stringResource
import com.catedra.feruturnos.ui.settings.SettingsScreen
import com.catedra.feruturnos.R
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

object Rutas {
    const val HOME = "Inicio"
    const val SEARCH = "Búsqueda"
    const val PROFILE = "Perfil"
    const val SETTINGS = "Configuración"
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
    val snackbarHostState = remember { SnackbarHostState() }
    val profileViewModel: ProfileViewModel = viewModel()
    val profileState by profileViewModel.profileState.collectAsState()

    var unreadNotificationsCount by remember { mutableStateOf(0) }

    LaunchedEffect (Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        Firebase.firestore
            .collection("notifications")
            .addSnapshotListener { snapshot, _ ->

                val count = snapshot?.documents?.count { document ->
                    val relatedUsers = document.get("relatedUsers") as? List<Map<String, Any>>
                        ?: emptyList()

                    relatedUsers.any { user ->
                        user["userId"] == uid && user["read"] == false
                    }
                } ?: 0

                unreadNotificationsCount = count
            }
    }

    val tituloActual = when (rutaActual) {
        Rutas.HOME -> stringResource(R.string.inicio)
        Rutas.SEARCH -> stringResource(R.string.busqueda)
        Rutas.ENCLOSURE_DETAIL -> stringResource(R.string.predio)
        Rutas.PROFILE -> stringResource(R.string.perfil)
        Rutas.SETTINGS -> stringResource(R.string.configuracion)
        Rutas.CONTACTS -> stringResource(R.string.contactos)
        Rutas.NOTIFICATIONS -> stringResource(R.string.notificaciones)
        Rutas.RESERVATION_DETAIL -> stringResource(R.string.reserva)
        else -> ""
    }

    // Estado del permiso de ubicación para pasárselo a la SearchScreen
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
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
                                contentDescription = stringResource(R.string.volver)
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
                                    contentDescription = stringResource(R.string.cerrar_sesion)
                                )
                            }

                            IconButton(
                                onClick = {
                                    navController.navigate(Rutas.SETTINGS)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.configuracion),
                                    tint = Color.White
                                )
                            }
                        }
                    }


                    IconButton(onClick = { navController.navigate(Rutas.NOTIFICATIONS) }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge {
                                        Text(
                                            text = if (unreadNotificationsCount > 99) {
                                                "99+"
                                            } else {
                                                unreadNotificationsCount.toString()
                                            }
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                modifier = if (rutaActual == Rutas.NOTIFICATIONS) {
                                    Modifier.shadow(
                                        elevation = 12.dp,
                                        shape = CircleShape,
                                        spotColor = Color.Black
                                    )
                                } else {
                                    Modifier
                                },
                                imageVector = Icons.Default.Notifications,
                                contentDescription = stringResource(R.string.notificaciones),
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (rutaActual != Rutas.PROFILE) {
                                navController.navigate(Rutas.PROFILE)
                            }
                        }
                    ) {

                        if (profileState.photo.isNotBlank()) {

                            AsyncImage(
                                model = profileState.photo,
                                contentDescription = stringResource(R.string.perfil),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                            )

                        } else {

                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = stringResource(R.string.perfil),
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                NavigationBarItem(
                    selected = rutaActual == Rutas.HOME,
                    onClick = {
                        if (rutaActual != Rutas.HOME) {
                            navController.navigate(Rutas.HOME)
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Home, contentDescription = null)
                    },
                    label = { Text(stringResource(R.string.inicio)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = Color.White.copy(alpha = 0.75f),
                        unselectedIconColor = Color.White.copy(alpha = 0.75f),
                        unselectedTextColor = Color.White.copy(alpha = 0.75f),
                        indicatorColor = Color.White
                    )
                )

                NavigationBarItem(
                    selected = rutaActual == Rutas.SEARCH,
                    onClick = {
                        if (rutaActual != Rutas.SEARCH) {
                            navController.navigate(Rutas.SEARCH)
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    label = { Text(stringResource(R.string.buscar)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = Color.White.copy(alpha = 0.75f),
                        unselectedIconColor = Color.White.copy(alpha = 0.75f),
                        unselectedTextColor = Color.White.copy(alpha = 0.75f),
                        indicatorColor = Color.White
                    )
                )

                NavigationBarItem(
                    selected = rutaActual == Rutas.PROFILE,
                    onClick = {
                        if (rutaActual != Rutas.PROFILE) {
                            navController.navigate(Rutas.PROFILE)
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    label = { Text(stringResource(R.string.perfil)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = Color.White.copy(alpha = 0.75f),
                        unselectedIconColor = Color.White.copy(alpha = 0.75f),
                        unselectedTextColor = Color.White.copy(alpha = 0.75f),
                        indicatorColor = Color.White
                    )
                )
            }
        }
    ) { padding ->

        if (mostrarDialogoCerrarSesion) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoCerrarSesion = false },
                shape = RoundedCornerShape(28.dp),
                title = {
                    Text(
                        text = stringResource(R.string.cerrar_sesion),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.seguro_que_queres_cerrar_sesion),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = { mostrarDialogoCerrarSesion = false }
                    ) {
                        Text(stringResource(R.string.cancelar))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogoCerrarSesion = false
                            onCerrarSesion()
                        }
                    ) {
                        Text(text = stringResource(R.string.confirmar))
                    }
                }
            )
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
                    onReservationCancelled = { message ->

                        navController.popBackStack()

                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
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

            composable(Rutas.SETTINGS) {
                SettingsScreen()
            }

            composable(Rutas.CONTACTS) { backStackEntry ->

                val source = backStackEntry.arguments?.getString("source") ?: "profile"
                val reservationId = backStackEntry.arguments?.getString("reservationId") ?: "none"

                val profileViewModel: ProfileViewModel = viewModel()
                val profileState by profileViewModel.profileState.collectAsState()

                val nuevaInvitacion = stringResource(R.string.nueva_invitacion)
                val invitadoReserva = stringResource(R.string.fuiste_invitado_a_una_reserva)

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
                                            "title" to nuevaInvitacion,
                                            "message" to invitadoReserva,
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