package com.catedra.feruturnos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.catedra.feruturnos.ui.detalle.DetalleScreen
import com.catedra.feruturnos.ui.peliculas.PeliculasScreen

/**
 * Definición de rutas de navegación.
 * Cada ruta es un string que identifica un destino de forma única.
 * No modificar este objeto.
 */
object Rutas {
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
@Composable
fun AppNavigation(
    onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.PELICULAS
    ) {

        // Destino 1: pantalla de listado
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

        // Pista: el patrón es exactamente el mismo que se mostró en la Sección 5.3
        // de la Clase 7.
    }
}
