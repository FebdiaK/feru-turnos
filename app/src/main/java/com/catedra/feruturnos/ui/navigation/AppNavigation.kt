package com.catedra.feruturnos.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.catedra.feruturnos.ui.detalle.DetalleScreen
import com.catedra.feruturnos.ui.home.HomeScreen
import com.catedra.feruturnos.ui.search.SearchScreen
import com.catedra.feruturnos.ui.profile.ProfileScreen
import com.catedra.feruturnos.ui.peliculas.PeliculasScreen
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * Definición de rutas de navegación.
 * Cada ruta es un string que identifica un destino de forma única.
 * No modificar este objeto.
 */
object Rutas {

    const val HOME = "Inicio"
    const val SEARCH = "Búsqueda"
    const val PROFILE = "Perfil"
    const val PELICULAS = "peliculas"
    const val DETALLE   = "detalle/{peliculaId}"

    /** Construye la ruta de detalle con el id de la película incluido. */
    fun detalle(id: String) = "detalle/$id"
}

/**
 * Grafo de navegación de la aplicación.
 *
 * ETAPA 4 DEL LAB: completar los dos bloques TODO.
 *
 * Comparación con el Lab 2A:
 * En el modelo tradicional necesitabas tres pasos para navegar al detalle:
 * instanciar el Fragment, crear la transacción con FragmentManager y commitear.
 * Acá toda la navegación se reduce a navController.navigate(ruta).
 * El back stack lo gestiona el NavController automáticamente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route
    val mostrarVolver = rutaActual != Rutas.HOME

    val tituloActual = when (rutaActual) {
        Rutas.HOME -> "Inicio"
        Rutas.SEARCH -> "Búsqueda"
        Rutas.PROFILE -> "Perfil"
        Rutas.PELICULAS -> "Películas"
        Rutas.DETALLE -> "Detalle"
        else -> "FERU Turnos"
    }

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
                            IconButton( onClick = onCerrarSesion ) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = "Cerrar sesión"
                                )
                            }
                        }
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificaciones"
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Perfil"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar (containerColor = MaterialTheme.colorScheme.tertiary){
                NavigationBarItem(
                    selected = rutaActual == Rutas.HOME,
                    onClick = {
                        navController.navigate(Rutas.HOME)
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
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
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
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
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Rutas.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Rutas.HOME) {
                HomeScreen()
            }

            composable(Rutas.SEARCH) {
                SearchScreen()
            }

            composable(Rutas.PROFILE) {
                ProfileScreen()
            }


            /**
            composable(Rutas.PELICULAS) {
                PeliculasScreen(
                    onNavegar = { id ->
                        navController.navigate(Rutas.detalle(id))
                    },
                    onCerrarSesion = onCerrarSesion
                )
            }

            composable(
                route = Rutas.DETALLE,
                arguments = listOf(
                    navArgument("peliculaId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val peliculaId = backStackEntry.arguments?.getString("peliculaId") ?: ""
                DetalleScreen(
                    peliculaId = peliculaId,
                    onVolver = { navController.popBackStack() }
                )
            }
            **/
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