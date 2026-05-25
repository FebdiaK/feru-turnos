package com.catedra.feruturnos.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.String

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _authState = MutableStateFlow<AuthState>(
        if (auth.currentUser != null) AuthState.Autenticado
        else AuthState.NoAutenticado
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    fun iniciarSesion(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Autenticado
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al iniciar sesion")
            }
        }
    }
    fun registrar(email: String, password: String) {
        viewModelScope.launch {
            try {
                val resultado = auth
                    .createUserWithEmailAndPassword(email, password)
                    .await()

                val uid = resultado.user?.uid
                    ?: throw Exception("No se pudo obtener el ID del usuario")

                val user = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "name" to "userName?",
                    "address" to "unaCalle?",
                    "photo" to "unaUri?",
                    "celphone" to 123456790,
                    "fechaRegistro" to FieldValue.serverTimestamp()
                )

                db.collection("users")
                    .document(uid)
                    .set(user)
                    .await()

                _authState.value = AuthState.Autenticado
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al crear la cuenta")
            }
        }
    }
    fun cerrarSesion() {
        auth.signOut()
        _authState.value = AuthState.NoAutenticado
    }
}