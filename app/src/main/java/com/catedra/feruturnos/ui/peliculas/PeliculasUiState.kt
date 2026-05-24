package com.catedra.feruturnos.ui.peliculas

import com.catedra.feruturnos.data.model.Pelicula

/**
 * Estado completo de la pantalla de listado de películas.
 *
 * Comparación con el Lab 2A:
 * En el ViewModel del Lab 2A tenías tres propiedades separadas (peliculas, cargando, error)
 * y una cuarta (peliculasFiltradas) que actualizabas manualmente en dos funciones distintas.
 * Acá esas cuatro piezas viven juntas en una única data class, y peliculasFiltradas
 * se calcula automáticamente — siempre es consistente con peliculas y consulta
 * sin ningún código de sincronización adicional.
 */
data class PeliculasUiState(
    val peliculas: List<Pelicula> = emptyList(),
    val consulta: String = "",
    val cargando: Boolean = true,
    val error: String? = null
) {

    val peliculasFiltradas: List<Pelicula>
        get() = if (consulta.isBlank()) {
            peliculas
        } else {
            peliculas.filter { pelicula ->
                pelicula.titulo.contains(consulta, ignoreCase = true)
            }
        }
}