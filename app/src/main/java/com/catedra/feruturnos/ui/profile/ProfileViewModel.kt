package com.catedra.feruturnos.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid

                if (uid == null) {
                    _profileState.value = ProfileState(
                        error = "No hay usuario autenticado"
                    )
                    return@launch
                }

                _profileState.value = _profileState.value.copy(
                    isLoading = true,
                    error = null
                )

                val document = Firebase.firestore
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()

                val stars = document.get("stars") as? List<Long> ?: emptyList()

                _profileState.value = ProfileState(
                    uid = uid,
                    contactId = document.getString("contactId") ?: "",
                    name = document.getString("name") ?: "",
                    email = document.getString("email") ?: "",
                    address = document.getString("address") ?: "",
                    photo = document.getString("photo") ?: "",
                    celphone = document.getLong("celphone")?.toInt() ?: 0,
                    stars = stars.map { it.toInt() }
                )

            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar el perfil"
                )
            }
        }
    }
}