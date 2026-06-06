package com.catedra.feruturnos.ui.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.google.firebase.messaging.FirebaseMessaging

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val client = OkHttpClient()

    private val cloudName = "dmde9k4fp"
    private val uploadPreset = "feru_profile_upload"

    private val _authState = MutableStateFlow<AuthState>(
        if (auth.currentUser != null) AuthState.Autenticado
        else AuthState.NoAutenticado
    )

    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun iniciarSesion(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()

                val uid = auth.currentUser?.uid

                if (uid != null) {
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        db.collection("users")
                            .document(uid)
                            .update("fcmToken", token)
                    }
                }

                _authState.value = AuthState.Autenticado
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun registrar(
        context: Context,
        email: String,
        password: String,
        name: String,
        celphone: Int,
        photoUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                val resultado = auth
                    .createUserWithEmailAndPassword(email, password)
                    .await()

                val uid = resultado.user?.uid
                    ?: throw Exception("No se pudo obtener el ID del usuario")

                val photoUrl = if (photoUri != null) {
                    subirImagenACloudinary(context, photoUri)
                } else {
                    ""
                }
                val contactId = generarContactIdUnico()

                val usuario = hashMapOf(
                    "uid" to uid,
                    "contactId" to contactId,
                    "email" to email,
                    "name" to name,
                    "celphone" to celphone,
                    "photo" to photoUrl,
                    "friends" to emptyList<Map<String, Any>>(),
                    "stars" to emptyList<Int>(),
                    "fechaRegistro" to FieldValue.serverTimestamp()
                )

                db.collection("users")
                    .document(uid)
                    .set(usuario)
                    .await()

                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    db.collection("users")
                        .document(uid)
                        .update("fcmToken", token)
                }

                _authState.value = AuthState.Autenticado

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al crear la cuenta")
            }
        }
    }

    private suspend fun subirImagenACloudinary(
        context: Context,
        photoUri: Uri
    ): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(photoUri)
            ?: throw Exception("No se pudo leer la imagen")

        val imageBytes = inputStream.readBytes()

        val imageBody = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "profile.jpg",
                imageBody
            )
            .addFormDataPart("upload_preset", uploadPreset)
            .addFormDataPart("folder", "profile_images")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("Cloudinary no respondió")

        if (!response.isSuccessful) {
            throw Exception("Error Cloudinary: $responseBody")
        }

        val json = JSONObject(responseBody)
        json.getString("secure_url")
    }

    private suspend fun generarContactIdUnico(): String {
        repeat(10) {
            val contactId = (100000..999999).random().toString()

            val existe = db.collection("users")
                .whereEqualTo("contactId", contactId)
                .get()
                .await()
                .documents
                .isNotEmpty()

            if (!existe) {
                return contactId
            }
        }

        throw Exception("No se pudo generar un ID único")
    }

    fun limpiarError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.NoAutenticado
        }
    }

    fun cerrarSesion() {
        auth.signOut()
        _authState.value = AuthState.NoAutenticado
    }
}