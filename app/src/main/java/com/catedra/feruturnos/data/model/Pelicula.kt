package com.catedra.feruturnos.data.model

data class Pelicula(
    val id: String,
    val titulo: String,
    val anio: Int,
    val genero: String,
    val descripcion: String,
    val director: String,
    val duracionMinutos: Int
)
