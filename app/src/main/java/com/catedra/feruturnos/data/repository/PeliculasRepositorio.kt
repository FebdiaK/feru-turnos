package com.catedra.feruturnos.data.repository

import com.catedra.feruturnos.data.model.Pelicula
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PeliculasRepositorio {

    private val db = Firebase.firestore
    fun obtenerPeliculas(): Flow<List<Pelicula>> = callbackFlow {
        val listener = db.collection("peliculas")
            .orderBy("titulo")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val peliculas = snapshot?.documents?.mapNotNull { doc ->
                    doc.toPelicula()
                } ?: emptyList()
                trySend(peliculas)
            }
        awaitClose { listener.remove() }
    }

    suspend fun obtenerPelicula(id: String): Pelicula? {
        val doc = db.collection("peliculas").document(id).get().await()
        return doc.toPelicula()
    }

    private fun DocumentSnapshot.toPelicula(): Pelicula? {
        return Pelicula(
            id = id,
            titulo = getString("titulo") ?: return null,
            anio = getLong("anio")?.toInt() ?: 0,
            genero = getString("genero") ?: "",
            descripcion = getString("descripcion") ?: "",
            director = getString("director") ?: "",
            duracionMinutos = getLong("duracionMinutos")?.toInt() ?: 0
        )
    }
}

