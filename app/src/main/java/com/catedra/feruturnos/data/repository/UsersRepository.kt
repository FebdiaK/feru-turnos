package com.catedra.feruturnos.data.repository

import com.catedra.feruturnos.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.String

class UsersRepository {

    private val db = Firebase.firestore
    fun obtenerUsers(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users")
            .orderBy("titulo")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toUser()
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    suspend fun obtenerUser(id: String): User? {
        val doc = db.collection("users").document(id).get().await()
        return doc.toUser()
    }

    private fun DocumentSnapshot.toUser(): User? {
        return User(
            id = id,
            name = getString("name") ?: return null,
            email = getString("email") ?: return null,
            address= getString("address") ?: return null,
            photo= getString("photo") ?: return null,
            celphone = getLong("celphone")?.toInt() ?: 0
        )
    }
}

